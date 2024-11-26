@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.ui.common.CheckboxWithText
import com.korn.portfolio.randomtrivia.ui.common.FilterSortMenuBar
import com.korn.portfolio.randomtrivia.ui.common.RadioButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.SearchableTopBar
import com.korn.portfolio.randomtrivia.ui.common.hhmmssFrom
import com.korn.portfolio.randomtrivia.ui.previewdata.getGame
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

private const val DELETE_ANIM_DURATION = 500

private enum class HistoryFilter(
    val displayText: String,
    val invoke: (List<Game>) -> List<Game>
) {
    ALL("All", { it }),
    TODAY("Today", { games ->
        fun Calendar.toTimeString() =
            "${get(Calendar.YEAR)}:${get(Calendar.MONTH)}:${get(Calendar.DAY_OF_MONTH)}"
        val calendar = Calendar.getInstance()
        val today = calendar.toTimeString()
        games.filter { game ->
            calendar.run {
                timeInMillis = game.detail.timestamp.time
                today == toTimeString()
            }
        }
    })
}

private enum class HistorySort(
    val displayText: String,
    val invoke: (List<Game>) -> List<Game>
) {
    MOST_RECENT("Most recent", { games ->
        games.sortedByDescending { it.detail.timestamp }
    })
}

@Composable
fun PastGames(
    pastGames: List<Game>,
    onDelete: (gameId: UUID) -> Unit,
    onReplay: (Game) -> Unit,
    onInspect: (Game) -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filter by rememberSaveable { mutableStateOf(HistoryFilter.ALL) }
    var sort by rememberSaveable { mutableStateOf(HistorySort.MOST_RECENT) }
    var reverseSort by rememberSaveable { mutableStateOf(false) }
    val displayGames = pastGames
        .let { filter.invoke(it) }
        .let { sort.invoke(it) }
        .let { if (reverseSort) it.reversed() else it }
    Scaffold(
        modifier = modifier,
        topBar = {
            SearchableTopBar(
                searchWord = "",
                onChange = {},
                onAboutClick = onAboutClick,
                hint = "",
                title = "Random Trivia",
                hideSearchButton = true,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            HistoryFilterSortMenuBar(
                filter = filter,
                onFilterChange = { filter = it },
                sort = sort,
                onSortChange = { sort = it },
                reverseSort = reverseSort,
                onReverseSortChange = { reverseSort = it }
            )
            if (displayGames.isEmpty())
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
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 8.dp,
                    bottom = 8.dp
                )
            ) {
                itemsIndexed(displayGames, key = { _, game -> game.detail.gameId }) { idx, game ->
                    val alpha by animateFloatAsState(
                        targetValue = if (game.detail.gameId in itemsInView) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = 600,
                            delayMillis = 50,
                            easing = LinearOutSlowInEasing
                        )
                    )
                    Column(
                        Modifier
                            .let {
                                if (LocalInspectionMode.current) it
                                else it.alpha(alpha)
                            }
                            .animateContentSize(tween(DELETE_ANIM_DURATION))
                    ) {
                        var deleting by remember { mutableStateOf(false) }
                        if (!deleting) {
                            GameDisplayItem(
                                game = game,
                                onDelete = {
                                    scope.launch {
                                        deleting = true
                                        delay(DELETE_ANIM_DURATION.toLong())
                                        onDelete(game.detail.gameId)
                                    }
                                },
                                onReplay = { onReplay(game) },
                                onInspect = { onInspect(game) },
                                modifier = Modifier.fillMaxWidth(),
                                endPadding = 16.dp
                            )
                            if (idx < displayGames.size - 1)
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
    onFilterChange: (HistoryFilter) -> Unit,
    sort: HistorySort,
    onSortChange: (HistorySort) -> Unit,
    reverseSort: Boolean,
    onReverseSortChange: (Boolean) -> Unit
) {
    FilterSortMenuBar(
        selectedFilter = filter,
        filters = HistoryFilter.entries,
        onFilterSelect = onFilterChange,
        filterToString = { it.displayText },
        sortBottomSheetContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sort By")
                CheckboxWithText(
                    checked = reverseSort,
                    onChange = onReverseSortChange,
                    text = "Reversed"
                )
            }
            HistorySort.entries.forEach {
                RadioButtonWithText(
                    selected = sort == it,
                    onClick = { onSortChange(it) },
                    text = it.displayText
                )
            }
        }
    )
}

@Composable
private fun GameDisplayItem(
    game: Game,
    onDelete: () -> Unit,
    onReplay: () -> Unit,
    onInspect: () -> Unit,
    modifier: Modifier = Modifier,
    endPadding: Dp = 0.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Get game data to display
        val calendar = Calendar.getInstance().apply {
            timeInMillis = game.detail.timestamp.time
        }
        val day = calendar.get(Calendar.DATE)
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, java.util.Locale.getDefault())
        val year = calendar.get(Calendar.YEAR)
        val time = calendar.run {
            fun Int.doubleDigit() = if (toString().length == 1) "0$this" else toString()
            val hh = get(Calendar.HOUR)
            val mm = get(Calendar.MINUTE).doubleDigit()
            val ampm = if (get(Calendar.AM_PM) == 0) "am" else "pm"
            "$hh:$mm $ampm"
        }
        val score = game.questions.fold(0) { score, question ->
            if (question.answer.answer == question.question.correctAnswer)
                score + 1
            else
                score
        }
        val totalQuestions = game.questions.size
        val totalTimeSecond = game.detail.totalTimeSecond

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
        Column {
            var showMenu by remember { mutableStateOf(false) }
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.offset(x = 12.dp - endPadding)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                offset = DpOffset(x = -endPadding, y = 0.dp)
            ) {
                DropdownMenuItem(
                    text = { Text("Inspect") },
                    onClick = {
                        showMenu = false
                        onInspect()
                    },
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                DropdownMenuItem(
                    text = { Text("Replay") },
                    onClick = {
                        showMenu = false
                        onReplay()
                    },
                    leadingIcon = { Icon(Icons.Default.Refresh, null) }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = { Icon(Icons.Outlined.Delete, null) }
                )
            }
        }
    }
}

@Preview
@Composable
private fun PastGamesPreview() {
    RandomTriviaTheme {
        PastGames(
            pastGames = List(2) { getGame(totalQuestions = 10, played = true) },
            onDelete = {},
            onReplay = {},
            onInspect = {},
            onAboutClick = {}
        )
    }
}