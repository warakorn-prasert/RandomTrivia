@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TooltipState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.ui.common.FetchStatus
import com.korn.portfolio.randomtrivia.ui.common.FetchStatusBar
import com.korn.portfolio.randomtrivia.ui.common.IconButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.OutlinedDropdown
import com.korn.portfolio.randomtrivia.ui.common.PaddedDialog
import com.korn.portfolio.randomtrivia.ui.common.displayName
import com.korn.portfolio.randomtrivia.ui.previewdata.BooleanDataProvider
import com.korn.portfolio.randomtrivia.ui.previewdata.GameSettingDataProvider
import com.korn.portfolio.randomtrivia.ui.previewdata.getCategory
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting.Companion.MAX_AMOUNT
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting.Companion.MIN_AMOUNT
import com.korn.portfolio.randomtrivia.ui.viewmodel.SettingBeforePlayingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DELETE_ANIM_DURATION = 500

@Composable
fun SettingBeforePlaying(
    categoriesFetchStatus: FetchStatus,
    fetchCategories: () -> Unit,
    submit: (onlineMode: Boolean, settings: List<GameSetting>) -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SettingBeforePlayingViewModel = viewModel(factory = SettingBeforePlayingViewModel.Factory)
    val onlineMode by viewModel.onlineMode.collectAsState()

    val catsWithCounts by viewModel.categoriesWithQuestionCounts.collectAsState(emptyList())
    val dialogChoiceGetter = DialogChoiceGetter(catsWithCounts)

    SettingBeforePlaying(
        submit = { settings -> submit(onlineMode, settings) },
        onlineMode = onlineMode,
        onOnlineModeChange = { online ->
            viewModel.changeOnlineMode(online)
            if (online && categoriesFetchStatus is FetchStatus.Error)
                fetchCategories()
        },
        categoriesFetchStatus = categoriesFetchStatus,
        fetchCategories = fetchCategories,
        fetchQuestionCountIfNotAlready = { categoryId, onFetchStatusChange ->
            viewModel.fetchQuestionCountIfNotAlready(categoryId, onFetchStatusChange)
        },
        dialogChoiceGetter = dialogChoiceGetter,
        modifier = modifier
    )
}

@Composable
private fun SettingBeforePlaying(
    submit: (List<GameSetting>) -> Unit,
    onlineMode: Boolean,
    onOnlineModeChange: (Boolean) -> Unit,
    categoriesFetchStatus: FetchStatus,
    fetchCategories: () -> Unit,
    fetchQuestionCountIfNotAlready:
        (categoryId: Int?, onFetchStatusChange: (FetchStatus) -> Unit) -> Unit,
    dialogChoiceGetter: DialogChoiceGetter,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var settings: List<GameSetting> by remember { mutableStateOf(emptyList()) }
    val categories = dialogChoiceGetter.getCategories(settings)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBarWithStartButton(
                enabled = settings.isNotEmpty(),
                onClick = { submit(settings) }
            )
        },
        floatingActionButton = {
            ExtendedFAB(
                enabled = categoriesFetchStatus == FetchStatus.Success
                        && categories.isNotEmpty(),
                onClick = { showDialog = true }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            OnlineModeToggleMenu(
                onlineMode = onlineMode,
                onChange = {
                    settings = emptyList()
                    onOnlineModeChange(it)
                }
            )
            if (showDialog)
                AddGameSettingDialog(
                    onDismissRequest = { showDialog = false },
                    onAddClick = { settings = settings + it },
                    categories = categories,
                    getDifficulties = { category ->
                        dialogChoiceGetter.getDifficulties(category, settings)
                    },
                    getMaxAmount = { category, difficulty ->
                        dialogChoiceGetter.getMaxAmount(category, difficulty)
                    },
                    fetchQuestionCountIfNotAlready = fetchQuestionCountIfNotAlready
                )
            if (onlineMode)
                FetchStatusBar(
                    fetchStatus = categoriesFetchStatus,
                    retry = fetchCategories
                )
            if (settings.isEmpty())
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Add game setting.")
                }
            else
                SettingListItems(
                    settings = settings,
                    remove = { setting ->
                        val idx = settings.indexOfFirst {
                            it.category?.id == setting.category?.id
                                    && it.difficulty == setting.difficulty
                                    && it.amount == setting.amount
                        }
                        settings = settings.run { subList(0, idx) + drop(idx + 1) }
                    }
                )
        }
    }
}

private class DialogChoiceGetter(
    private val catsWithCounts: List<Pair<Category, QuestionCount>>
) {
    private val allDifficulties: List<Difficulty?> = listOf(null) + Difficulty.entries

    private val QuestionCount?.difficultiesWithQuestions: List<Difficulty?>
        get() =
            if (this == null) allDifficulties
            else mutableListOf<Difficulty?>(null).apply {
                if (easy > 0) add(Difficulty.EASY)
                if (medium > 0) add(Difficulty.MEDIUM)
                if (hard > 0) add(Difficulty.HARD)
            }

    private val Pair<Category, QuestionCount>?.difficultiesWithQuestions: List<Difficulty?>
        get() = if (this == null) allDifficulties else second.difficultiesWithQuestions

    fun getCategories(settings: List<GameSetting>): List<Category?> {
        if (catsWithCounts.isEmpty()) return emptyList()
        else {
            // Exclude category in settings whose all difficulties are used.
            val catIdsToExclude: List<Int?> = settings
                .filter { setting ->
                    val usedCombinations = settings.count { it.category?.id == setting.category?.id }
                    val possibleCombinations =
                        // Random category has all difficulties
                        if (setting.category == null) allDifficulties.size
                        else catsWithCounts
                            .first { it.first.id == setting.category.id }
                            .difficultiesWithQuestions
                            .size
                    usedCombinations == possibleCombinations
                }
                .map { it.category?.id }
            // Return random category + starting categories - excluded categories
            return (listOf(null) + catsWithCounts.sortedBy { it.first.name })
                .filter { all ->
                    all?.first?.id !in catIdsToExclude
                }
                .map { it?.first }
        }
    }

    fun getDifficulties(category: Category?, settings: List<GameSetting>): List<Difficulty?>  {
        val allDiffs =
            if (category == null) allDifficulties
            else catsWithCounts
                .first { it.first.id == category.id }
                .difficultiesWithQuestions
        val usedDiffs = settings.filter { it.category?.id == category?.id }.map { it.difficulty }
        return allDiffs - usedDiffs
    }

    fun getMaxAmount(category: Category?, difficulty: Difficulty?): Int =
        if (category == null) MAX_AMOUNT
        else catsWithCounts
            .first { it.first.id == category.id }
            .second
            .run {
                when (difficulty) {
                    Difficulty.EASY -> easy
                    Difficulty.MEDIUM -> medium
                    Difficulty.HARD -> hard
                    else -> total
                }
            }
            .coerceAtMost(MAX_AMOUNT)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarWithStartButton(enabled: Boolean, onClick: () -> Unit) {
    TopAppBar(
        title = { Text("Random Trivia", style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_android),
                contentDescription = "App icon",
                modifier = Modifier.minimumInteractiveComponentSize(),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        actions = {
            IconButtonWithText(
                onClick = onClick,
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Button to start game.",
                text = "Start game",
                enabled = enabled
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnlineModeToggleMenu(
    onlineMode: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Online")
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    Card(
                        modifier = Modifier.padding(16.dp),
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors().copy(
                            containerColor = MaterialTheme.colorScheme.inverseSurface,
                            contentColor = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    ) {
                        Text(
                            text = "If enabled, download questions from the internet. Else, get from past games.",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                },
                state = TooltipState()
            ) {
                Icon(Icons.Default.Info, null)
            }
        }
        Switch(
            checked = onlineMode,
            onCheckedChange = onChange,
            thumbContent = {
                Icon(
                    imageVector = Icons.Default.run {
                        if (onlineMode) Check
                        else Close
                    },
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                    tint = SwitchDefaults.colors().run {
                        if (onlineMode) checkedTrackColor
                        else uncheckedTrackColor
                    }
                )
            }
        )
    }
}

// Custom FAB to have disable status.
@Composable
private fun ExtendedFAB(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .widthIn(min = 80.dp)
            .height(56.dp),
        enabled = enabled,
        shape = FloatingActionButtonDefaults.extendedFabShape,
        color = MaterialTheme.colorScheme.run {
            if (enabled) tertiaryContainer
            else onSurface.copy(alpha = 0.12f)
        },
        contentColor = MaterialTheme.colorScheme.run {
            if (enabled) onTertiaryContainer
            else onSurface.copy(alpha = 0.38f)
        },
        tonalElevation = if (enabled) 1.dp else 0.dp,
        shadowElevation = if (enabled) 1.dp else 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, "Button to add game setting,")
            Text("Setting")
        }
    }
}

@Composable
private fun AddGameSettingDialog(
    onDismissRequest: () -> Unit,
    onAddClick: (GameSetting) -> Unit,
    categories: List<Category?>,
    getDifficulties: (Category?) -> List<Difficulty?>,
    getMaxAmount: (Category?, Difficulty?) -> Int,
    fetchQuestionCountIfNotAlready:
        (categoryId: Int?, onFetchStatusChange: (FetchStatus) -> Unit) -> Unit,
) {
    // new categories -> reset category
    var category: Category? by remember {
        mutableStateOf(categories.firstOrNull())
    }
    LaunchedEffect(categories) {
        if (categories.isEmpty()) onDismissRequest()
        category = categories.firstOrNull()
    }

    // new category -> fetch question count
    var questionCountFetchStatus: FetchStatus by remember { mutableStateOf(FetchStatus.Success) }
    LaunchedEffect(category) {
        fetchQuestionCountIfNotAlready(category?.id) {
            questionCountFetchStatus = it
        }
    }

    // case 1: new category -> fetch question count -> new difficulties
    // case 2: new category (already fetched) -> new difficulties
    // case 3: retry fetch question count -> new difficulties
    var difficulties: List<Difficulty?> by remember {
        mutableStateOf(getDifficulties(category))
    }
    LaunchedEffect(
        category,  // case 2
        questionCountFetchStatus  // case 1 & 3
    ) {
        if (questionCountFetchStatus == FetchStatus.Success)
            difficulties = getDifficulties(category)
    }

    // new difficulties -> new difficulty
    var difficulty: Difficulty? by remember(difficulties) {
        mutableStateOf(difficulties.firstOrNull())
    }

    // new difficulty -> new max amount
    val maxAmount: Int by remember(difficulty) {
        mutableIntStateOf(getMaxAmount(category, difficulty))
    }

    // new max amount -> new amount
    fun getNewAmount(oldAmount: String, newMaxAmount: Int): String =
        (oldAmount.toIntOrNull() ?: MIN_AMOUNT)
            .coerceIn(MIN_AMOUNT..newMaxAmount.coerceAtLeast(MIN_AMOUNT))
            .toString()
    var amount: String by remember { mutableStateOf(MIN_AMOUNT.toString()) }
    LaunchedEffect(maxAmount) {
        amount = getNewAmount(amount, maxAmount)
    }

    val isAmountValid: Boolean by remember {
        derivedStateOf {
            amount.toIntOrNull()
                ?.let { it in MIN_AMOUNT..maxAmount.coerceAtLeast(MIN_AMOUNT) }
                ?: false
        }
    }

    PaddedDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Game Setting") },
        actions = {
            TextButton(onDismissRequest) { Text("CANCEL") }
            val focusManager = LocalFocusManager.current
            TextButton(
                onClick = {
                    onAddClick(GameSetting(category, difficulty, amount.toInt()))
                    // If categories doesn't update, nothing will,
                    // i.e., when there're other options with same category.
                    // Updating difficulties will update difficulty, max amount, and amount.
                    difficulties = getDifficulties(category)
                    // dismiss keyboard if shown
                    focusManager.clearFocus()
                },
                enabled = questionCountFetchStatus == FetchStatus.Success
                        && isAmountValid
            ) {
                Text("ADD")
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (questionCountFetchStatus == FetchStatus.Loading
                || questionCountFetchStatus == FetchStatus.Success)
                OutlinedDropdown(
                    selected = category,
                    onSelect = { category = it },
                    items = categories,
                    toString = { it.displayName },
                    label = { Text("Category") },
                    itemContent = { Text(it.displayName) }
                )
            when (val f = questionCountFetchStatus) {
                is FetchStatus.Error -> {
                    Text(
                        text = f.message,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.error
                    )
                    IconButtonWithText(
                        onClick = {
                            fetchQuestionCountIfNotAlready(category?.id) {
                                questionCountFetchStatus = it
                            }
                        },
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Button to retry fetching category detail",
                        text = "Retry"
                    )
                }

                FetchStatus.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))

                FetchStatus.Success ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedDropdown(
                            selected = difficulty,
                            onSelect = { difficulty = it },
                            items = difficulties,
                            toString = { it.displayName },
                            label = { Text("Difficulty") },
                            itemContent = { Text(it.displayName) }
                        )

                        // when keyboard is dismissed, set amount to correct value
                        val interactionSource = remember { MutableInteractionSource() }
                        val isFocus by interactionSource.collectIsFocusedAsState()
                        LaunchedEffect(isFocus) {
                            if (!isFocus)
                                amount = getNewAmount(amount, maxAmount)
                        }

                        val focusManager = LocalFocusManager.current
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            label = { Text("Amount") },
                            supportingText = { Text("Max $maxAmount") },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { focusManager.clearFocus() }
                            ),
                            interactionSource = interactionSource
                        )
                    }
            }
        }
    }
}

@Composable
private fun SettingListItems(
    settings: List<GameSetting>,
    remove: (GameSetting) -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(settings.size) {
        scope.launch {
            listState.animateScrollToItem(settings.size - 1)
        }
    }
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        itemsIndexed(
            items = settings,
            key = { _, setting -> setting.run { "${ category?.id }$difficulty$amount" } }
        ) { idx, setting ->
            Column(Modifier.animateContentSize(tween(DELETE_ANIM_DURATION))) {
                var deleting by remember { mutableStateOf(false) }
                if (!deleting) {
                    SettingListItem(
                        setting = setting,
                        remove = {
                            scope.launch {
                                deleting = true
                                delay(DELETE_ANIM_DURATION.toLong())
                                remove(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (idx < settings.size - 1) {
                        HorizontalDivider(Modifier.padding(vertical = 8.dp))
                    }
                }
            }

        }
        item {
            Spacer(Modifier.height((56 + 16).dp))  // FAB
        }
    }
}

@Composable
private fun SettingListItem(
    setting: GameSetting,
    remove: (GameSetting) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), Arrangement.spacedBy(8.dp)) {
            Text(setting.category.displayName, fontWeight = FontWeight.Bold)
            Text("${setting.difficulty.displayName}, ${setting.amount}")
        }
        IconButton(
            onClick = { remove(setting) },
            modifier = Modifier.offset(x = 12.dp)
        ) {
            Icon(Icons.Default.Close, "Button to remove game setting item.")
        }
    }
}

@Preview
@Composable
private fun SettingBeforePlayingPreview() {
    RandomTriviaTheme {
        SettingBeforePlaying(
            submit = {},
            onlineMode = true,
            onOnlineModeChange = {},
            categoriesFetchStatus = FetchStatus.Success,
            fetchCategories = {},
            fetchQuestionCountIfNotAlready = { _, _ -> },
            dialogChoiceGetter = DialogChoiceGetter(emptyList()),
        )
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    RandomTriviaTheme {
        SettingBeforePlaying(
            submit = {},
            onlineMode = true,
            onOnlineModeChange = {},
            categoriesFetchStatus = FetchStatus.Loading,
            fetchCategories = {},
            fetchQuestionCountIfNotAlready = { _, _ -> },
            dialogChoiceGetter = DialogChoiceGetter(emptyList()),
        )
    }
}

@Preview
@Composable
private fun ErrorPreview() {
    RandomTriviaTheme {
        SettingBeforePlaying(
            submit = {},
            onlineMode = true,
            onOnlineModeChange = {},
            categoriesFetchStatus = FetchStatus.Error("Some error message."),
            fetchCategories = {},
            fetchQuestionCountIfNotAlready = { _, _ -> },
            dialogChoiceGetter = DialogChoiceGetter(emptyList()),
        )
    }
}

@Preview
@Composable
private fun TopBarPreview(
    @PreviewParameter(BooleanDataProvider::class) enabled: Boolean
) {
    RandomTriviaTheme {
        TopBarWithStartButton(enabled) {}
    }
}

@Preview
@Composable
private fun OnlineModeToggleMenuPreview(
    @PreviewParameter(BooleanDataProvider::class) onlineMode: Boolean
) {
    RandomTriviaTheme {
        Surface {
            OnlineModeToggleMenu(onlineMode) {}
        }
    }
}

@Preview
@Composable
private fun ExtendedFABPreview(
    @PreviewParameter(BooleanDataProvider::class) enabled: Boolean
) {
    RandomTriviaTheme {
        Surface {
            ExtendedFAB(enabled) {}
        }
    }
}

@Preview
@Composable
private fun AddGameSettingDialogPreview() {
    RandomTriviaTheme {
        AddGameSettingDialog(
            onDismissRequest = {},
            onAddClick = {},
            categories = List(1) { getCategory(it) },
            getDifficulties = { Difficulty.entries },
            getMaxAmount = { _, _ -> 50 },
            fetchQuestionCountIfNotAlready = { _, _ -> }
        )
    }
}

@Preview
@Composable
private fun SettingListItemPreview(
    @PreviewParameter(GameSettingDataProvider::class) setting: GameSetting
) {
    RandomTriviaTheme {
        Surface {
            SettingListItem(setting, {})
        }
    }
}