@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.CategoryWithQuestions
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.entity.GameAnswer
import com.korn.portfolio.randomtrivia.model.GameOption
import com.korn.portfolio.randomtrivia.database.model.GameQuestion
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import com.korn.portfolio.randomtrivia.data.MockData
import com.korn.portfolio.randomtrivia.data.mockGame
import java.util.Date

private fun Date.toFormattedString(): String = String.format("%tY-%<tm-%<td %<tH:%<tM:%<tS", this)

private fun GameOption.sameType(option: GameOption): Boolean =
    category.id == option.category.id && difficulty == option.difficulty

private fun GameOption.sameType(question: Question): Boolean =
    category.id == question.categoryId && difficulty == question.difficulty

private fun List<GameOption>.subtractByType(options: List<GameOption>): List<GameOption> =
    mutableListOf<GameOption>().apply {
        this@subtractByType.forEach { option1 ->
            if (!options.any { option2 -> option1.sameType(option2) }) {
                add(option1.copy())
            }
        }
    }

private fun SnapshotStateList<GameOption>.update(idx: Int, category: Category) {
    this[idx].run {
        update(idx, category, difficulty, amount)
    }
}

private fun SnapshotStateList<GameOption>.update(idx: Int, difficulty: Difficulty) {
    this[idx].run {
        update(idx, category, difficulty, amount)
    }
}

private fun SnapshotStateList<GameOption>.update(idx: Int, amount: Int) {
    this[idx].run {
        update(idx, category, difficulty, amount)
    }
}

private fun SnapshotStateList<GameOption>.update(
    idx: Int,
    category: Category,
    difficulty: Difficulty,
    amount: Int
) {
    this[idx] = this[idx].copy(
        category = category.copy(),
        difficulty = difficulty,
        amount = amount
    )
}

private fun CategoryWithQuestions.toGameOptions(): List<GameOption> {
    val options = mutableListOf<GameOption>()
    // Update options for each question
    for (question in questions) {
        // Find option based on question
        val idx = options.indexOfFirst { it.sameType(question) }
        // If found, add 1 to amount
        if (idx >= 0) options[idx] = options[idx].copy(amount = options[idx].amount + 1)
        // Else, add new option
        else options.add(
            GameOption(
                category = category.copy(),
                difficulty = question.difficulty,
                amount = 1
            )
        )
    }
    return options
}

@Composable
fun GameScreen(paddingValues: PaddingValues) {
    val triviaViewModel: TriviaViewModel = viewModel(factory = TriviaViewModel.Factory)
    val games by triviaViewModel.games.collectAsState(emptyList())
    val categories by triviaViewModel.categoriesWithQuestions.collectAsState(emptyList())
    LazyColumn(Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                val expanded = remember { mutableStateOf(false) }
                if (expanded.value) {
                    GameInsertDialog(expanded, categories, triviaViewModel::insertGame)
                }
                Text("${games.size} games")
                IconButton({ expanded.value = true }) {
                    Icon(Icons.Default.Add, null)
                }
                IconButton(triviaViewModel::deleteAllGames) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }
        items(games, key = { it.detail.gameId }) {
            GameCard(
                game = it,
                modifier = Modifier.padding(bottom = 4.dp),
                updateAction = triviaViewModel::updateAnswer,
                deleteAction = triviaViewModel::deleteGames
            )
        }
    }
}

@Composable
private fun GameCard(
    game: Game,
    modifier: Modifier = Modifier,
    updateAction: (GameAnswer) -> Unit,
    deleteAction: (Game) -> Unit
) {
    Card(modifier) {
        Column(Modifier
            .width(IntrinsicSize.Max)
            .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(game.detail.timestamp.toFormattedString())
                Spacer(Modifier.weight(1f))
                IconButton({ deleteAction(game) }) {
                    Icon(Icons.Default.Delete, null)
                }
            }
            HorizontalDivider()
            game.questions.forEachIndexed { idx, question ->
                GameQuestionEditor(
                    idx = idx,
                    question = question,
                    modifier = Modifier.padding(top = 8.dp),
                    updateAction = updateAction
                )
            }
        }
    }
}

@Preview
@Composable
private fun GameCardPreview() {
    GameCard(mockGame, updateAction = {}, deleteAction = {})
}

@Composable
private fun GameQuestionEditor(
    idx: Int,
    question: GameQuestion,
    modifier: Modifier = Modifier,
    updateAction: (GameAnswer) -> Unit
) {
    Row(modifier, Arrangement.End) {
        Text("${idx + 1}.")
        Column {
            Text("(${question.category?.name}) ${question.question.question}")
            val answer = question.answer.answer
            val correct = answer == question.question.correctAnswer
            Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(if (correct) Icons.Default.Check else Icons.Default.Close, null)
                Spacer(Modifier.weight(1f))
                CustomDropdown(
                    value = answer,
                    onValueChange = { updateAction(question.answer.copy(answer = it)) },
                    options = question.question.incorrectAnswers + question.question.correctAnswer,
                    toString = { it }
                )
            }
        }
    }
}

@Composable
private fun GameInsertDialog(
    expanded: MutableState<Boolean>,
    categories: List<CategoryWithQuestions>,
    insertAction: (List<GameOption>) -> Unit
) {
    val userOptions = remember { mutableStateListOf<GameOption>() }
    val refOptions = categories
        .fold(emptyList<GameOption>()) { acc, it ->
            acc + it.toGameOptions()
        }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    Dialog(onDismissRequest = { expanded.value = false }) {
        Card(Modifier
            .height(IntrinsicSize.Max)
            .heightIn(max = screenHeight * 3 / 4)
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Text(
                    text = "New game",
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.CenterHorizontally),
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider()
                GameEditor(
                    userOptions = userOptions,
                    refOptions = refOptions,
                    modifier = Modifier.weight(1f).padding(8.dp).verticalScroll(rememberScrollState())
                )
                HorizontalDivider()
                Row(Modifier) {
                    IconButton({ expanded.value = false }) {
                        Icon(Icons.Default.Close, null)
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            insertAction(userOptions)
                            expanded.value = false
                        },
                        enabled = userOptions.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Done, null)
                    }
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
private fun GameInsertDialogPreview() {
    GameInsertDialog(
        expanded = mutableStateOf(true),
        categories = MockData().categoriesWithQuestions,
        insertAction = {}
    )
}

@Composable
private fun GameEditor(
    userOptions: SnapshotStateList<GameOption>,
    refOptions: List<GameOption>,
    modifier: Modifier = Modifier
) {
    val otherOptions = refOptions.subtractByType(userOptions)
    Column(modifier) {
        if (userOptions.isEmpty()) {
            Text("Empty", fontStyle = FontStyle.Italic)
        } else {
            repeat(userOptions.size) { idx ->
                GameOptionEditor(
                    currentIdx = idx,
                    userOptions = userOptions,
                    otherOptions = otherOptions,
                    refOptions = refOptions
                )
            }
        }
        IconButton(
            onClick = {
                userOptions.add(otherOptions.first())
            },
            enabled = otherOptions.isNotEmpty()
        ) {
            Icon(Icons.Default.Add, null)
        }
    }
}

@Composable
private fun GameOptionEditor(
    currentIdx: Int,
    userOptions: SnapshotStateList<GameOption>,
    otherOptions: List<GameOption>,
    refOptions: List<GameOption>
) {
    val userOption = userOptions[currentIdx]
    val otherCategories = otherOptions
        .distinctBy { it.category.id }
        .filter { it.category.id != userOption.category.id }
        .map { it.category }

    val otherDifficulties = otherOptions
        .filter { it.category.id == userOption.category.id }
        .map { it.difficulty }

    val maxAmount = refOptions
        .firstOrNull { it.sameType(userOption) }
        ?.amount
        ?: 1

    LaunchedEffect(userOption.category.id) {
        val difficulty = if (otherDifficulties.isNotEmpty()) {
            otherDifficulties.first()
        } else {
            userOption.difficulty
        }
        userOptions.update(currentIdx, difficulty = difficulty)
    }

    LaunchedEffect(userOption.difficulty) {
        userOptions.update(currentIdx, amount = 1)
    }

    Row {
        IconButton({ userOptions.removeAt(currentIdx) }) {
            Icon(Icons.Default.Delete, null)
        }
        Column(Modifier.padding(4.dp)) {
            Row {
                CustomDropdown(
                    value = userOption.category,
                    onValueChange = { userOptions.update(currentIdx, category = it) },
                    options = otherCategories,
                    toString = { it.name }
                )
                Spacer(Modifier.weight(1f))
                CustomDropdown(
                    value = userOption.difficulty,
                    onValueChange = { userOptions.update(currentIdx, difficulty = it) },
                    options = otherDifficulties,
                    toString = { it.name }
                )
            }
            PlainTextField(
                value = userOption.amount.toString(),
                onValueChange = {
                    val amount = it.toIntOrNull()?.coerceIn(1, maxAmount) ?: 1
                    userOptions.update(currentIdx, amount = amount)
                },
                numberOnly = true,
                leadingIcon = { Text("Amount (max ${maxAmount})") }
            )
        }
    }
}