@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.korn.portfolio.randomtrivia.ui.common.FetchStatus
import com.korn.portfolio.randomtrivia.ui.common.FetchStatusBar
import com.korn.portfolio.randomtrivia.ui.common.IconButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.OutlinedDropdown
import com.korn.portfolio.randomtrivia.ui.common.PaddedDialog
import com.korn.portfolio.randomtrivia.ui.previewdata.BooleanDataProvider
import com.korn.portfolio.randomtrivia.ui.previewdata.GameSettingDataProvider
import com.korn.portfolio.randomtrivia.ui.previewdata.getCategory
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import com.korn.portfolio.randomtrivia.ui.viewmodel.MAX_AMOUNT
import com.korn.portfolio.randomtrivia.ui.viewmodel.MIN_AMOUNT
import com.korn.portfolio.randomtrivia.ui.viewmodel.SettingBeforePlayingViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.displayName
import kotlinx.coroutines.flow.StateFlow

@Composable
fun SettingBeforePlaying(
    modifier: Modifier = Modifier,
    categoriesFetchStatus: StateFlow<FetchStatus>,
    fetchCategories: () -> Unit,
    onSubmit: (onlineMode: Boolean, settings: List<GameSetting>) -> Unit
) {
    val viewModel: SettingBeforePlayingViewModel = viewModel(factory = SettingBeforePlayingViewModel.Factory)

    val canStartGame by viewModel.canStartGame.collectAsState(false)
    val onlineMode by viewModel.onlineMode.collectAsState()

    val canAddMoreSetting by viewModel.canAddMoreSetting(categoriesFetchStatus).collectAsState(false)
    val settings by viewModel.settings.collectAsState()

    val categoriesFetchStatusValue by categoriesFetchStatus.collectAsState()
    val questionCountFetchStatus by viewModel.questionCountFetchStatus.collectAsState()

    val category by viewModel.category.collectAsState()
    val difficulty by viewModel.difficulty.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val categories by viewModel.categories.collectAsState(emptyList())
    val difficulties by viewModel.difficulties.collectAsState(emptyList())
    val maxAmount by viewModel.maxAmount.collectAsState(MAX_AMOUNT)

    SettingBeforePlaying(
        modifier = modifier,
        submitAction = { viewModel.submit(onSubmit) },
        canStartGame = canStartGame,
        canAddMoreSetting = canAddMoreSetting,
        settings = settings,
        removeSetting = viewModel::removeSetting,
        onlineMode = onlineMode,
        changeOnlineMode = {
            viewModel.changeOnlineMode(it, categoriesFetchStatusValue, fetchCategories)
        },
        fetchStatus = categoriesFetchStatusValue,
        fetchCategories = fetchCategories,
        questionCountFetchStatus = questionCountFetchStatus,
        fetchQuestionCount = viewModel::fetchQuestionCountIfNeedTo,
        addSetting = viewModel::addSetting,
        category = category,
        difficulty = difficulty,
        amount = amount,
        categories = categories,
        difficulties = difficulties,
        maxAmount = maxAmount,
        selectCategory = viewModel::selectCategory,
        selectDifficulty = viewModel::selectDifficulty,
        selectAmount = viewModel::selectAmount,
    )
}

@Composable
fun SettingBeforePlaying(
    modifier: Modifier = Modifier,
    submitAction: () -> Unit,
    canStartGame: Boolean,
    onlineMode: Boolean,
    canAddMoreSetting: Boolean,
    settings: List<GameSetting>,
    removeSetting: (GameSetting) -> Unit,
    changeOnlineMode: (Boolean) -> Unit,
    fetchStatus: FetchStatus,
    fetchCategories: () -> Unit,
    questionCountFetchStatus: FetchStatus,
    fetchQuestionCount: () -> Unit,
    addSetting: () -> Unit,
    category: Category?,
    difficulty: Difficulty?,
    amount: Int,
    categories: List<Category?>,
    difficulties: List<Difficulty?>,
    maxAmount: Int,
    selectCategory: (Category?) -> Unit,
    selectDifficulty: (Difficulty?) -> Unit,
    selectAmount: (Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        topBar = {
            TopBarWithStartButton(
                enabled = canStartGame,
                onClick = submitAction
            )
        },
        floatingActionButton = {
            ExtendedFAB(
                enabled = canAddMoreSetting,
                onClick = { showDialog = true }
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            OnlineModeToggleMenu(
                onlineMode = onlineMode,
                onChange = changeOnlineMode
            )
            AddGameSettingDialog(
                show = showDialog,
                onDismissRequest = { showDialog = false },
                canAddMoreSetting = canAddMoreSetting,
                fetchStatus = questionCountFetchStatus,
                fetchQuestionCount = fetchQuestionCount,
                addSetting = addSetting,
                category = category,
                difficulty = difficulty,
                amount = amount,
                categories = categories,
                difficulties = difficulties,
                maxAmount = maxAmount,
                selectCategory = selectCategory,
                selectDifficulty = selectDifficulty,
                selectAmount = selectAmount
            )
            if (onlineMode)
                FetchStatusBar(
                    fetchStatus = fetchStatus,
                    retryAction = fetchCategories
                )
            if (settings.isEmpty())
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("Add game setting.")
                }
            else
                SettingListItems(
                    settings = settings,
                    removeAction = removeSetting
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
    show: Boolean,
    onDismissRequest: () -> Unit,
    canAddMoreSetting: Boolean,
    fetchStatus: FetchStatus,
    fetchQuestionCount: () -> Unit,
    addSetting: () -> Unit,
    category: Category?,
    difficulty: Difficulty?,
    amount: Int,
    categories: List<Category?>,
    difficulties: List<Difficulty?>,
    maxAmount: Int,
    selectCategory: (Category?) -> Unit,
    selectDifficulty: (Difficulty?) -> Unit,
    selectAmount: (Int) -> Unit,
) {
    LaunchedEffect(canAddMoreSetting) {
        if (!canAddMoreSetting) onDismissRequest()
    }
    var displayAmount by remember { mutableStateOf(amount.toString()) }
    val validDisplayAmount = displayAmount.toIntOrNull()
        ?.let {
            it in MIN_AMOUNT..maxAmount.coerceAtLeast(MIN_AMOUNT)
        }
        ?: false
    PaddedDialog(
        show = show,
        onDismissRequest = onDismissRequest,
        title = { Text("Game Setting") },
        actions = {
            TextButton(onDismissRequest) { Text("CANCEL") }
            val focusManager = LocalFocusManager.current
            TextButton(
                onClick = {
                    addSetting()
                    focusManager.clearFocus()
                },
                enabled = fetchStatus == FetchStatus.Success && validDisplayAmount
            ) {
                Text("ADD")
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (fetchStatus in listOf(FetchStatus.Loading, FetchStatus.Success)) {
                OutlinedDropdown(
                    selected = category,
                    onSelect = selectCategory,
                    items = categories,
                    toString = { it.displayName },
                    label = { Text("Category") },
                    itemContent = { Text(it.displayName) }
                )
            }
            when (fetchStatus) {
                is FetchStatus.Error -> {
                    Text(
                        text = fetchStatus.message,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.error
                    )
                    IconButtonWithText(
                        onClick = fetchQuestionCount,
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
                            onSelect = selectDifficulty,
                            items = difficulties,
                            toString = { it.displayName },
                            label = { Text("Difficulty") },
                            itemContent = { Text(it.displayName) }
                        )
                        val interactionSource = remember { MutableInteractionSource() }
                        val isFocus by interactionSource.collectIsFocusedAsState()
                        LaunchedEffect(isFocus, displayAmount, amount) {
                            if (!isFocus || validDisplayAmount) {
                                val newAmount = (displayAmount.toIntOrNull() ?: MIN_AMOUNT)
                                    .coerceIn(MIN_AMOUNT..maxAmount.coerceAtLeast(MIN_AMOUNT))
                                selectAmount(newAmount)
                                displayAmount = newAmount.toString()
                            }
                        }
                        val focusManager = LocalFocusManager.current
                        OutlinedTextField(
                            value = displayAmount,
                            onValueChange = { displayAmount = it },
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
    removeAction: (GameSetting) -> Unit
) {
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(start = 16.dp, end = 8.dp)
    ) {
        settings.forEachIndexed { idx, setting ->
            SettingListItem(setting, removeAction, Modifier.padding(vertical = 8.dp))
            if (idx < settings.size - 1) HorizontalDivider()
        }
        Spacer(Modifier.height((56 + 16 * 2).dp))  // FAB
    }
}

@Composable
private fun SettingListItem(
    setting: GameSetting,
    removeAction: (GameSetting) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier, Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), Arrangement.spacedBy(8.dp)) {
            Text(setting.category.displayName, fontWeight = FontWeight.Bold)
            Text("${setting.difficulty.displayName}, ${setting.amount}")
        }
        IconButton({ removeAction(setting) }) {
            Icon(Icons.Default.Close, "Button to remove game setting item.")
        }
    }
}

@Preview
@Composable
private fun SettingBeforePlayingPreview() {
    RandomTriviaTheme {
        SettingBeforePlaying(
            submitAction = {},
            canStartGame = true,
            canAddMoreSetting = true,
            settings = listOf(
                GameSetting(getCategory(0), Difficulty.EASY, 10),
                GameSetting(getCategory(1), Difficulty.HARD, 50),
            ),
            removeSetting = {},
            onlineMode = true,
            changeOnlineMode = {},
            fetchStatus = FetchStatus.Success,
            fetchCategories = {},
            questionCountFetchStatus = FetchStatus.Success,
            fetchQuestionCount = {},
            addSetting = {},
            category = getCategory(0),
            difficulty = Difficulty.EASY,
            amount = 1,
            categories = emptyList(),
            difficulties = emptyList(),
            maxAmount = 1,
            selectCategory = {},
            selectDifficulty = {},
            selectAmount = {},
        )
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    RandomTriviaTheme {
        SettingBeforePlaying(
            submitAction = {},
            canStartGame = false,
            canAddMoreSetting = false,
            settings = emptyList(),
            removeSetting = {},
            onlineMode = true,
            changeOnlineMode = {},
            fetchStatus = FetchStatus.Loading,
            fetchCategories = {},
            questionCountFetchStatus = FetchStatus.Success,
            fetchQuestionCount = {},
            addSetting = {},
            category = getCategory(0),
            difficulty = Difficulty.EASY,
            amount = 1,
            categories = emptyList(),
            difficulties = emptyList(),
            maxAmount = 1,
            selectCategory = {},
            selectDifficulty = {},
            selectAmount = {},
        )
    }
}

@Preview
@Composable
private fun ErrorPreview() {
    RandomTriviaTheme {
        SettingBeforePlaying(
            submitAction = {},
            canStartGame = false,
            canAddMoreSetting = false,
            settings = emptyList(),
            removeSetting = {},
            onlineMode = true,
            changeOnlineMode = {},
            fetchStatus = FetchStatus.Error("Some error message"),
            fetchCategories = {},
            questionCountFetchStatus = FetchStatus.Success,
            fetchQuestionCount = {},
            addSetting = {},
            category = getCategory(0),
            difficulty = Difficulty.EASY,
            amount = 1,
            categories = emptyList(),
            difficulties = emptyList(),
            maxAmount = 1,
            selectCategory = {},
            selectDifficulty = {},
            selectAmount = {},
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
            show = true,
            onDismissRequest = {},
            canAddMoreSetting = false,
            fetchStatus = FetchStatus.Success,
            fetchQuestionCount = {},
            addSetting = {},
            category = getCategory(0),
            difficulty = Difficulty.EASY,
            amount = 1,
            categories = List(2) { getCategory(it) },
            difficulties = Difficulty.entries,
            maxAmount = 50,
            selectCategory = {},
            selectDifficulty = {},
            selectAmount = {}
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