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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
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
import com.korn.portfolio.randomtrivia.ui.previewdata.PreviewWindowSizes
import com.korn.portfolio.randomtrivia.ui.previewdata.getCategory
import com.korn.portfolio.randomtrivia.ui.previewdata.windowSizeForPreview
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting.Companion.MAX_AMOUNT
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting.Companion.MIN_AMOUNT
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DELETE_ANIM_DURATION = 500

// Ref. : https://stackoverflow.com/a/68887484
private val GameSettingListSaver = listSaver<SnapshotStateList<GameSetting>, GameSetting>(
    save = { stateList -> stateList.toList() },
    restore = { it.toMutableStateList() },
)

@Composable
fun SettingBeforePlaying(
    categoriesWithQuestionCounts: List<Pair<Category, QuestionCount>>,
    onlineMode: Boolean,
    onOnlineModeChange: (Boolean) -> Unit,
    categoriesFetchStatus: FetchStatus,
    onFetchCategoriesRequest: () -> Unit,
    onSubmit: (List<GameSetting>) -> Unit,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    var showDialog by remember { mutableStateOf(false) }
    val settings = rememberSaveable(saver = GameSettingListSaver) {
        mutableStateListOf()
    }
    val dialogChoiceGetter = DialogChoiceGetter(categoriesWithQuestionCounts, settings)
    val categories = dialogChoiceGetter.getCategories()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopBarWithStartButton(
                enabled = settings.isNotEmpty(),
                onClick = { onSubmit(settings) }
            )
        },
        floatingActionButton = {
            ExtendedFAB(
                enabled =
                    if (onlineMode)
                        categoriesFetchStatus == FetchStatus.Success && categories.isNotEmpty()
                    else
                        categories.isNotEmpty(),
                onClick = { showDialog = true }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            OnlineModeToggleMenu(
                onlineMode = onlineMode,
                onChange = { online ->
                    settings.clear()
                    onOnlineModeChange(online)
                    if (online && categoriesFetchStatus is FetchStatus.Error)
                        onFetchCategoriesRequest()
                }
            )
            if (showDialog)
                AddGameSettingDialog(
                    onDismissRequest = { showDialog = false },
                    onAdd = { settings.add(it) },
                    dialogChoiceGetter = dialogChoiceGetter
                )
            if (onlineMode)
                FetchStatusBar(
                    fetchStatus = categoriesFetchStatus,
                    onRetry = onFetchCategoriesRequest
                )
            if (settings.isEmpty())
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Add game setting.")
                }
            else
                SettingListItems(
                    settings = settings,
                    onDelete = { setting ->
                        val idx = settings.indexOfFirst {
                            it.category?.id == setting.category?.id
                                    && it.difficulty == setting.difficulty
                                    && it.amount == setting.amount
                        }
                        settings.removeAt(idx)
                    },
                    windowSizeClass = windowSizeClass
                )
        }
    }
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
    onAdd: (GameSetting) -> Unit,
    dialogChoiceGetter: DialogChoiceGetter
) {
    val categories = dialogChoiceGetter.getCategories()
    LaunchedEffect(categories) {
        if (categories.isEmpty()) onDismissRequest()
    }

    var category: Category? by remember(categories) {
        mutableStateOf(categories.firstOrNull())
    }

    var difficulties: List<Difficulty?> by remember(category) {
        mutableStateOf(dialogChoiceGetter.getDifficulties(category))
    }

    var difficulty: Difficulty? by remember(difficulties) {
        mutableStateOf(difficulties.firstOrNull())
    }

    val maxAmount: Int by remember(difficulty) {
        mutableIntStateOf(dialogChoiceGetter.getMaxAmount(category, difficulty))
    }

    var amount: String by remember { mutableStateOf(MIN_AMOUNT.toString()) }
    // when keyboard is dismissed, set amount to correct value
    val interactionSource = remember { MutableInteractionSource() }
    val isFocus by interactionSource.collectIsFocusedAsState()
    // replacing remember(maxAmount) to use previous amount in calculation
    LaunchedEffect(maxAmount, isFocus) {
        amount = (amount.toIntOrNull() ?: MIN_AMOUNT)
            .coerceIn(MIN_AMOUNT..maxAmount.coerceAtLeast(MIN_AMOUNT))
            .toString()
    }

    PaddedDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Game Setting") },
        actions = {
            TextButton(onDismissRequest) { Text("CANCEL") }
            val focusManager = LocalFocusManager.current
            TextButton(
                onClick = {
                    onAdd(GameSetting(category, difficulty, amount.toInt()))
                    // If categories doesn't update, nothing will,
                    // i.e., when there're other options with same category.
                    // Updating difficulties will update difficulty, max amount, and amount.
                    difficulties = dialogChoiceGetter.getDifficulties(category)
                    // dismiss keyboard if shown
                    focusManager.clearFocus()
                },
                enabled = amount.toIntOrNull()
                    ?.let { it in MIN_AMOUNT..maxAmount.coerceAtLeast(MIN_AMOUNT) }
                    ?: false
            ) {
                Text("ADD")
            }
        }
    ) {
        OutlinedDropdown(
            selected = category,
            onSelect = { category = it },
            items = categories,
            toString = { it.displayName },
            label = { Text("Category") },
            itemContent = { Text(it.displayName) }
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedDropdown(
                selected = difficulty,
                onSelect = { difficulty = it },
                items = difficulties,
                toString = { it.displayName },
                label = { Text("Difficulty") },
                itemContent = { Text(it.displayName) }
            )
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

@Composable
private fun SettingListItems(
    settings: List<GameSetting>,
    onDelete: (GameSetting) -> Unit,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    val totalSettings = settings.size
    val gridState = rememberLazyGridState()
    LaunchedEffect(totalSettings) {
        gridState.animateScrollToItem(totalSettings - 1)
    }

    val scope = rememberCoroutineScope()
    val totalColumns = when (windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.EXPANDED -> 3
        WindowWidthSizeClass.MEDIUM -> 2
        else -> 1
    }
    val remainderItems = totalSettings % totalColumns
    LazyVerticalGrid(
        columns = GridCells.Fixed(totalColumns),
        state = gridState,
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 8.dp,
            bottom = 8.dp + (56 + 16).dp  // with FAB
        )
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
                        onDelete = {
                            scope.launch {
                                deleting = true
                                delay(DELETE_ANIM_DURATION.toLong())
                                onDelete(it)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        endPadding = 16.dp
                    )
                    val showDivider = idx < totalSettings - if (remainderItems == 0) totalColumns else remainderItems
                    if (showDivider)
                        HorizontalDivider(Modifier.padding(top = 8.dp, end = 16.dp, bottom = 8.dp))
                }
            }

        }
    }
}

@Composable
private fun SettingListItem(
    setting: GameSetting,
    onDelete: (GameSetting) -> Unit,
    modifier: Modifier = Modifier,
    endPadding: Dp = 0.dp
) {
    Row(modifier, Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), Arrangement.spacedBy(8.dp)) {
            Text(setting.category.displayName, fontWeight = FontWeight.Bold)
            Text("${setting.difficulty.displayName}, ${setting.amount}")
        }
        IconButton(
            onClick = { onDelete(setting) },
            modifier = Modifier.offset(x = 12.dp - endPadding)
        ) {
            Icon(Icons.Default.Close, "Button to remove game setting item.")
        }
    }
}

private class DialogChoiceGetter(
    private val catsWithCounts: List<Pair<Category, QuestionCount>>,
    private val settings: List<GameSetting>
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

    fun getCategories(): List<Category?> {
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

    fun getDifficulties(category: Category?): List<Difficulty?>  {
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

@PreviewWindowSizes
@Composable
private fun SettingBeforePlayingPreview() {
    RandomTriviaTheme {
        SettingBeforePlaying(
            onSubmit = {},
            onlineMode = true,
            onOnlineModeChange = {},
            categoriesFetchStatus = FetchStatus.Success,
            onFetchCategoriesRequest = {},
            categoriesWithQuestionCounts = emptyList(),
            windowSizeClass = windowSizeForPreview()
        )
    }
}

@PreviewWindowSizes
@Composable
private fun SettingListItemsPreview() {
    RandomTriviaTheme {
        Surface {
            SettingListItems(
                settings = List(5) {
                    GameSetting(
                        category = getCategory(it),
                        difficulty = Difficulty.entries.random(),
                        amount = it * 2
                    )
                },
                onDelete = {},
                windowSizeClass = windowSizeForPreview()
            )
        }
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    RandomTriviaTheme {
        SettingBeforePlaying(
            onSubmit = {},
            onlineMode = true,
            onOnlineModeChange = {},
            categoriesFetchStatus = FetchStatus.Loading,
            onFetchCategoriesRequest = {},
            categoriesWithQuestionCounts = emptyList()
        )
    }
}

@Preview
@Composable
private fun ErrorPreview() {
    RandomTriviaTheme {
        SettingBeforePlaying(
            onSubmit = {},
            onlineMode = true,
            onOnlineModeChange = {},
            categoriesFetchStatus = FetchStatus.Error("Some error message."),
            onFetchCategoriesRequest = {},
            categoriesWithQuestionCounts = emptyList()
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
            onAdd = {},
            dialogChoiceGetter = DialogChoiceGetter(
                List(1) {
                    getCategory(it) to QuestionCount(1, 1, 0, 0)
                },
                emptyList()
            )
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