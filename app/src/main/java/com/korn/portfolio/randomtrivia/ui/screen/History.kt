@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.ui.common.CheckboxWithText
import com.korn.portfolio.randomtrivia.ui.common.FilterSortMenuBar
import com.korn.portfolio.randomtrivia.ui.common.RadioButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.SearchableTopBar
import com.korn.portfolio.randomtrivia.ui.hhmmssFrom
import com.korn.portfolio.randomtrivia.ui.viewmodel.HistoryFilter
import com.korn.portfolio.randomtrivia.ui.viewmodel.HistorySort
import com.korn.portfolio.randomtrivia.ui.viewmodel.HistoryViewModel
import java.util.Calendar
import java.util.UUID

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
    onReplay: (Game) -> Unit,
) {
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
    Scaffold(
        topBar = {
            SearchableTopBar(
                searchWord = "",
                onChange = {},
                hint = "",
                title = "Random Trivia",
                hideSearchButton = true
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            HistoryFilterSortMenuBar(
                historyViewModel.filter.collectAsState().value,
                historyViewModel::setFilter,
                historyViewModel.sort.collectAsState().value,
                historyViewModel::setSort,
                historyViewModel.reverseSort.collectAsState().value,
                historyViewModel::setReverseSort
            )
            val games by historyViewModel.games.collectAsState(emptyList())
            if (games.isEmpty())
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No game played yet.")
                }
            LazyColumn {
                itemsIndexed(games, key = { _, game -> game.detail.gameId }) { idx, game ->
                    if (idx > 0)
                        HorizontalDivider()
                    var inspect by remember { mutableStateOf(false) }
                    GameDisplayItem(
                        game = game,
                        inspectAction = { inspect = true },
                        replayAction = { onReplay(game) },
                        deleteAction = { historyViewModel.deleteGame(game.detail.gameId) }
                    )
                    if (inspect)
                        InspectDialog(
                            onDismissRequest = { inspect = false },
                            replayAction = { onReplay(game) },
                            game = game
                        )
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
    deleteAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 4.dp, bottom = 16.dp),
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
            IconButton({ showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                )
            }
            if (showMenu) {
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    offset = DpOffset(x = (-12).dp, y = 0.dp)
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