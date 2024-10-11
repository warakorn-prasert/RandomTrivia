@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.ui.common.CheckboxWithText
import com.korn.portfolio.randomtrivia.ui.common.FilterSortMenuBar
import com.korn.portfolio.randomtrivia.ui.common.RadioButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.SearchableTopBar
import com.korn.portfolio.randomtrivia.ui.hhmmssFrom
import com.korn.portfolio.randomtrivia.ui.previewdata.getGame
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.HistoryFilter
import com.korn.portfolio.randomtrivia.ui.viewmodel.HistorySort
import com.korn.portfolio.randomtrivia.ui.viewmodel.HistoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

private const val deleteAnimDuration = 500

data class GameDisplay(
    val day: Int,
    val month: String,
    val year: Int,
    val time: String,
    val score: Int,
    val totalQuestions: Int,
    val totalTimeSecond: Int,
    val gameId: UUID
)

fun Game.asDisplay() =
    Calendar.getInstance()
        .apply { timeInMillis = detail.timestamp.time }
        .let { calendar ->
            GameDisplay(
                day = calendar.get(Calendar.DATE),
                month = when(calendar.get(Calendar.MONTH)) {
                    0 -> "Jan"
                    1 -> "Feb"
                    2 -> "Mar"
                    3 -> "Apr"
                    4 -> "May"
                    5 -> "Jun"
                    6 -> "Jul"
                    7 -> "Aug"
                    8 -> "Sep"
                    9 -> "Oct"
                    10 -> "Nov"
                    else -> "Dec"
                },
                year = calendar.get(Calendar.YEAR),
                time = calendar.run  {
                    fun Int.doubleDigit() = if (toString().length == 1) "0$this" else toString()
                    val hh = get(Calendar.HOUR)
                    val mm = get(Calendar.MINUTE).doubleDigit()
                    val ampm = if (get(Calendar.AM_PM) == 0) "am" else "pm"
                    "$hh:$mm $ampm"
                },
                score = questions.fold(0) { score, question ->
                    if (question.answer.answer == question.question.correctAnswer)
                        score + 1
                    else
                        score
                },
                totalQuestions = questions.size,
                totalTimeSecond = detail.totalTimeSecond,
                gameId = detail.gameId
            )
        }

@Composable
fun PastGames(
    modifier: Modifier = Modifier,
    onReplay: (Game) -> Unit,
    onInspect: (Game) -> Unit,
    navToAboutScreen: () -> Unit
) {
    val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)

    val filter: HistoryFilter by viewModel.filter.collectAsState()
    val sort: HistorySort by viewModel.sort.collectAsState()
    val reverseSort by viewModel.reverseSort.collectAsState()
    val games by viewModel.games.collectAsState(emptyList())

    PastGames(
        modifier = modifier,
        onReplay = onReplay,
        onInspect = onInspect,
        filter = filter, setFilter = viewModel::setFilter,
        sort = sort, setSort = viewModel::setSort,
        reverseSort = reverseSort, setReverseSort = viewModel::setReverseSort,
        games = games,
        deleteGame = viewModel::deleteGame,
        navToAboutScreen = navToAboutScreen
    )
}

@Composable
private fun PastGames(
    modifier: Modifier = Modifier,

    onReplay: (Game) -> Unit,
    onInspect: (Game) -> Unit,

    filter: HistoryFilter, setFilter: (HistoryFilter) -> Unit,
    sort: HistorySort, setSort: (HistorySort) -> Unit,
    reverseSort: Boolean, setReverseSort: (Boolean) -> Unit,

    games: List<Game>,
    deleteGame: (gameId: UUID) -> Unit,

    navToAboutScreen: () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            SearchableTopBar(
                searchWord = "",
                onChange = {},
                navToAboutScreen = navToAboutScreen,
                hint = "",
                title = "Random Trivia",
                hideSearchButton = true,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            HistoryFilterSortMenuBar(
                filter,
                setFilter,
                sort,
                setSort,
                reverseSort,
                setReverseSort
            )
            if (games.isEmpty())
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No game played yet.")
                }

            val listState = rememberLazyListState()
            LaunchedEffect(filter, reverseSort, sort) {
                listState.animateScrollToItem(0)
            }
            val itemsInView by remember {
                derivedStateOf {
                    listState.layoutInfo
                        .visibleItemsInfo.map { it.key as UUID }
                }
            }

            val scope = rememberCoroutineScope()
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                itemsIndexed(games, key = { _, game -> game.detail.gameId }) { idx, game ->
                    val isInView = game.detail.gameId in itemsInView
                    val alpha by animateFloatAsState(
                        targetValue = if (isInView) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = 600,
                            delayMillis = 50,
                            easing = LinearOutSlowInEasing
                        )
                    )
                    Column(
                        Modifier
                            .alpha(alpha)
                            .animateContentSize(tween(deleteAnimDuration))
                    ) {
                        var deleting by remember { mutableStateOf(false) }
                        if (!deleting) {
                            GameDisplayItem(
                                game = game,
                                inspectAction = { onInspect(game) },
                                replayAction = { onReplay(game) },
                                deleteAction = {
                                    scope.launch {
                                        deleting = true
                                        delay(deleteAnimDuration.toLong())
                                        deleteGame(game.detail.gameId)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (idx < games.size - 1)
                                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryFilterSortMenuBar(
    filter: HistoryFilter,
    onFilterSelect: (HistoryFilter) -> Unit,
    sort: HistorySort,
    onSortSelect: (HistorySort) -> Unit,
    reverseSort: Boolean,
    onReverseSortChange: (Boolean) -> Unit
) {
    FilterSortMenuBar(
        selectedFilter = filter,
        filters = HistoryFilter.entries,
        onFilterSelect = onFilterSelect,
        filterToString = { it.displayText },
        sortBottomSheetContent = {
            Column(Modifier.height(IntrinsicSize.Min)) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sort By")
                    CheckboxWithText(
                        checked = reverseSort,
                        onCheckedChange = onReverseSortChange,
                        text = "Reversed"
                    )
                }
                HistorySort.entries.forEach {
                    RadioButtonWithText(
                        selected = sort == it,
                        onClick = { onSortSelect(it) },
                        text = it.displayText
                    )
                }
            }
        }
    )
}

@Composable
private fun GameDisplayItem(
    game: Game,
    inspectAction: () -> Unit,
    replayAction: () -> Unit,
    deleteAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        game.asDisplay().run {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("$day / $month / $year, $time", fontWeight = FontWeight.Bold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.sports_score),
                        contentDescription = null
                    )
                    Text("$score / $totalQuestions")
                    Icon(
                        painter = painterResource(R.drawable.ic_timer),
                        contentDescription = null
                    )
                    Text(hhmmssFrom(totalTimeSecond))
                }
            }
        }
        Column {
            var showMenu by remember { mutableStateOf(false) }
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.offset(x = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                )
            }
            if (showMenu) {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Inspect") },
                        onClick = {
                            showMenu = false
                            inspectAction()
                        },
                        leadingIcon = { Icon(Icons.Default.Search, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Replay") },
                        onClick = {
                            showMenu = false
                            replayAction()
                        },
                        leadingIcon = { Icon(Icons.Default.Refresh, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            deleteAction()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Delete, null) }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun PastGamesPreview() {
    RandomTriviaTheme {
        PastGames(
            onReplay = {},
            onInspect = {},
            filter = HistoryFilter.ALL, setFilter = {},
            sort = HistorySort.MOST_RECENT, setSort = {},
            reverseSort = false, setReverseSort = {},
            games = List(2) { getGame(totalQuestions = 10, played = true) },
            deleteGame = {},
            navToAboutScreen = {}
        )
    }
}