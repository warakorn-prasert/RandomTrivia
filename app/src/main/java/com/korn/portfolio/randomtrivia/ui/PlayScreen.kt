@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.repository.GameOption
import com.korn.portfolio.randomtrivia.ui.model.NetworkUiState
import kotlinx.coroutines.launch

private const val MAX_AMOUNT = 50
private const val MIN_AMOUNT = 1

private enum class GameStage {
    SETTING,
    PLAYING,
    RESULT;

    val route: String = name.lowercase()
}

private fun List<Pair<Category, QuestionCount>>.nextOption(usedOptions: List<GameOption>): GameOption {
    val category =
        if (!usedOptions.any { it.category == null }) null
        else availableCategories(usedOptions).firstOrNull()
    val difficulty = availableDifficulties(category, usedOptions).firstOrNull()
    return GameOption(
        category = category,
        difficulty = difficulty,
        type = null,
        amount = 1
    )
}

private fun List<Pair<Category, QuestionCount>>.maxOptions(offline: Boolean): Int =
    fold(0) { max, (_, questionCount) ->
        questionCount.run {
            var inc = 0
            if (offline) {
                if (easy > 0) inc++
                if (medium > 0) inc++
                if (hard > 0) inc++
                if (inc > 0) inc++   // random option
            } else {  // remote categories without question count
                inc += 4
            }
            max + inc
        }
    }

private fun List<Pair<Category, QuestionCount>>.availableCategories(filterOptions: List<GameOption>): List<Category?> {
    val categories = this
        .filter { (category, questionCount) ->
            // any match with filterOptions that does not use every difficulty
            filterOptions.count { option ->
                option.category?.id == category.id
            } < 4
            // has questions
            && questionCount.total > 0
        }
        .map { it.first }
    // add null if not in filterOptions
    return if (filterOptions.any { it.category == null }) categories else categories + null
}

private fun List<Pair<Category, QuestionCount>>.availableDifficulties(
    category: Category?,
    filterOptions: List<GameOption>
): List<Difficulty?> {
    val ret = mutableSetOf<Difficulty?>(null)
    val questionCount = firstOrNull { it.first.id == category?.id }
        ?.second
        ?: QuestionCount(0, 0, 0, 0)
    // get from this
    questionCount.run {
        if (easy > 0) ret.add(Difficulty.EASY)
        if (medium > 0) ret.add(Difficulty.MEDIUM)
        if (hard > 0) ret.add(Difficulty.HARD)
    }
    // filter out based on filterOptions
    val filterDifficulties = filterOptions.filter { it.category?.id == category?.id }.map { it.difficulty }
    if (filterDifficulties.contains(Difficulty.EASY) && questionCount.easy > 0)
        ret.remove(Difficulty.EASY)
    if (filterDifficulties.contains(Difficulty.MEDIUM) && questionCount.medium > 0)
        ret.remove(Difficulty.MEDIUM)
    if (filterDifficulties.contains(Difficulty.HARD) && questionCount.hard > 0)
        ret.remove(Difficulty.HARD)
    if (filterDifficulties.contains(null) && questionCount.total > 0)
        ret.remove(null)
    return ret.toList()
}

private fun List<Pair<Category, QuestionCount>>.maxAmount(
    category: Category?,
    difficulty: Difficulty?
): Int {
    val questionCount = firstOrNull { it.first.id == category?.id }
        ?.second
        ?: QuestionCount(0, 0, 0, 0)
    val maxAmount =
        if (category == null) MAX_AMOUNT
        else if (difficulty == null) questionCount.total
        else when (difficulty) {
            Difficulty.EASY -> questionCount.easy
            Difficulty.MEDIUM -> questionCount.medium
            Difficulty.HARD -> questionCount.hard
        }
    return maxAmount.coerceAtLeast(MIN_AMOUNT)
}

@Composable
fun PlayScreen(
    snackbarHostState: SnackbarHostState,
    playing: MutableState<Boolean>
) {
    val navController = rememberNavController()
    val playViewModel: PlayViewModel = viewModel(factory = PlayViewModel.Factory)
    NavHost(
        navController = navController,
        startDestination = GameStage.SETTING.route
    ) {
        composable(GameStage.SETTING.route) {
            val uiState: MutableState<NetworkUiState> = remember { mutableStateOf(NetworkUiState.Success) }
            LaunchedEffect(uiState.value) {
                when (val s = uiState.value) {
                    is NetworkUiState.Error -> snackbarHostState.showSnackbar("Error (${s.error.message})")
                    NetworkUiState.Loading -> {}
                    NetworkUiState.Success -> {
                        if (playViewModel.game.value != null) navController.navigate(GameStage.PLAYING.route)
                    }
                }
            }
            Box {
                val remoteCategories by playViewModel.remoteCategories.observeAsState(emptyList())
                val localCategories by playViewModel.localCategories.collectAsState(emptyList())
                SettingStage(
                    remoteCategories = remoteCategories,
                    localCategories = localCategories,
                    fetchCategories = playViewModel::fetchCategories,
                    fetchQuestionCount = playViewModel::fetchQuestionCount,
                    onDone = { options, offline ->
                        playing.value = true
                        playViewModel.fetchNewGame(options, offline, uiState)
                    }
                )
                if (uiState.value is NetworkUiState.Loading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card { CircularProgressIndicator(Modifier.padding(12.dp)) }
                    }
                }
            }
        }
        composable(GameStage.PLAYING.route) {
            val game by playViewModel.game.observeAsState()
            val scope = rememberCoroutineScope()
            var asking by remember { mutableStateOf(false) }
            fun askToExit() {
                if (!asking) { scope.launch {
                    asking = true
                    val result = snackbarHostState.showSnackbar(
                        message = "Exit game?",
                        actionLabel = "Yes",
                        withDismissAction = true,
                        duration = SnackbarDuration.Indefinite
                    )
                    when (result) {
                        SnackbarResult.Dismissed -> {}
                        SnackbarResult.ActionPerformed -> {
                            playViewModel.exitGame()
                            navController.navigate(GameStage.SETTING.route)
                            playing.value = false
                        }
                    }
                    asking = false
                } }
            }
            BackHandler(enabled = true) { askToExit() }
            game?.let { PlayingStage(it, ::askToExit) }
        }
        composable(GameStage.RESULT.route) {
            ResultStage()
        }
    }
}

@Composable
private fun SettingStage(
    remoteCategories: List<Pair<Category, QuestionCount>>,
    localCategories: List<Pair<Category, QuestionCount>>,
    fetchCategories: (MutableState<NetworkUiState>) -> Unit,
    fetchQuestionCount: (Int, MutableState<NetworkUiState>) -> Unit,
    onDone: (List<GameOption>, Boolean) -> Unit
) {
    var offline by remember { mutableStateOf(true) }
    val categories = if (offline) localCategories else remoteCategories
    val options = remember { mutableStateListOf<GameOption>() }

    Column(Modifier.fillMaxSize().padding(horizontal = horizontalPadding)) {
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Setting", fontWeight = FontWeight.ExtraBold)
            Button(
                onClick = { onDone(options, offline) },
                enabled = options.isNotEmpty()
            ) {
                Text("Done")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = offline, onCheckedChange = { offline = it })
            Text("Offline Mode")
        }
        HorizontalDivider(thickness = 2.dp)
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val uiState: MutableState<NetworkUiState> = remember { mutableStateOf(NetworkUiState.Loading) }
            LaunchedEffect(offline) {
                options.clear()
                if (offline) {
                    uiState.value = NetworkUiState.Success
                } else {
                    if (remoteCategories.isEmpty()) fetchCategories(uiState)
                    else uiState.value = NetworkUiState.Success
                }
            }
            when (val s = uiState.value) {
                is NetworkUiState.Error ->
                    Column(Modifier.padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error (${s.error.message})")
                        IconButton({ fetchCategories(uiState) }) {
                            Icon(Icons.Default.Refresh, null)
                        }
                    }
                NetworkUiState.Loading ->
                    CircularProgressIndicator(
                        Modifier.padding(top = 24.dp).align(Alignment.CenterHorizontally)
                    )
                NetworkUiState.Success -> {
                    repeat(options.size) { idx ->
                        OptionEditor(
                            idx = idx,
                            options = options,
                            sourceCategories = categories,
                            fetchQuestionCount = if (!offline) fetchQuestionCount
                                else { _, uiState2 -> uiState2.value = NetworkUiState.Success}
                        )
                        HorizontalDivider(Modifier.padding(vertical = 6.dp))
                    }
                    Box(Modifier.fillMaxWidth(), Alignment.Center) {
                        IconButton(
                            onClick = { options.add(categories.nextOption(options)) },
                            enabled = categories.maxOptions(offline) > options.size
                        ) {
                            Icon(Icons.Default.AddCircle, null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayingStage(
    game: Game,
    askToExit: () -> Unit
) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Button(askToExit) { Text("Exit") }
        Text("gameId : ${game.detail.gameId}")
        Text("timestamp : ${game.detail.timestamp}")
        game.questions.forEach { (question, _, category) ->
            Card(Modifier.padding(8.dp)) {
                Text("(${question.difficulty}) ${question.question}")
                Text("(${category?.id} ${category?.name})")
                Text("- (correct) ${question.correctAnswer}")
                question.incorrectAnswers.forEach {
                    Text("- $it")
                }
            }
        }
    }
}

@Composable
private fun ResultStage() {

}

@Composable
private fun OptionEditor(
    idx: Int,
    options: SnapshotStateList<GameOption>,
    sourceCategories: List<Pair<Category, QuestionCount>>,
    fetchQuestionCount: (Int, MutableState<NetworkUiState>) -> Unit
) {
    val uiState: MutableState<NetworkUiState> = remember { mutableStateOf(NetworkUiState.Loading) }
    val option = options[idx]
    val otherOptions = options.slice(0..<idx) + options.drop(idx + 1)

    val categories: List<Category?> = sourceCategories.availableCategories(filterOptions = otherOptions)
    val difficulties: List<Difficulty?> =
        if (option.category == null) {
            listOf(null) + Difficulty.entries
        } else {
            sourceCategories.availableDifficulties(category = option.category, filterOptions = otherOptions)
        }
    val maxAmount =
        if (option.difficulty == null) {
            MAX_AMOUNT
        } else {
            sourceCategories.maxAmount(category = option.category, difficulty = option.difficulty)
        }

    // get question count
    LaunchedEffect(option.category?.id) {
        val id = option.category?.id
        if (id == null) {
            uiState.value = NetworkUiState.Success
        } else if (sourceCategories.firstOrNull { it.first.id == id }?.second?.invalid == true) {
            // if un-fetched
            fetchQuestionCount(id, uiState)
        } else {
            // already fetched
            uiState.value = NetworkUiState.Success
        }
    }
    // update difficulties & set difficulty to default
    LaunchedEffect(uiState.value) {
        val defaultDifficulty = when (uiState.value) {
            is NetworkUiState.Error -> null
            NetworkUiState.Loading -> null
            NetworkUiState.Success -> {
                difficulties.firstOrNull()
            }
        }
        options[idx] = option.copy(difficulty = defaultDifficulty, amount = 1)
    }
    // set max amount to default
    LaunchedEffect(option.difficulty) {
        options[idx] = option.copy(amount = 1)
    }

    Row(Modifier.padding(top = 8.dp)) {
        IconButton({ options.removeAt(idx) }) {
            Icon(Icons.Default.Delete, null)
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Category : ")
                DropdownButton<Category?>(
                    selection = option.category,
                    onSelected = { options[idx] = option.copy(category = it) },
                    items = categories,
                    toString = { it?.name ?: "Random" }
                )
            }
            when (val s = uiState.value) {
                is NetworkUiState.Error -> Text("Error (${s.error.message})")
                NetworkUiState.Loading -> CircularProgressIndicator()
                NetworkUiState.Success -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Difficulty : ")
                            DropdownButton<Difficulty?>(
                                selection = option.difficulty,
                                onSelected = { options[idx] = option.copy(difficulty = it) },
                                items = difficulties,
                                toString = { it?.name ?: "Random" }
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Amount : ")
                        SliderMenu(
                            value = option.amount,
                            onValueChange = {
                                options[idx] = option.copy(amount = it)
                            },
                            minValue = MIN_AMOUNT,
                            maxValue = maxAmount.coerceAtMost(MAX_AMOUNT)
                        )
                    }
                }
            }
        }
    }
}