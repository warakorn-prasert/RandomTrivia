@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.database.model.GameQuestion

@Composable
fun HistoryScreen() {
    val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
    val games by historyViewModel.games.collectAsState(emptyList())
    LazyColumn(
        modifier = Modifier.padding(horizontal = horizontalPadding),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(games, key = { it.detail.gameId }) { game ->
            val show = remember { mutableStateOf(false) }
            if (show.value) {
                GameQuestionsDialog(show, game.questions)
            }
            Card(Modifier.clickable { show.value = true }) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton({ historyViewModel.deleteGame(game.detail.gameId) }) {
                        Icon(Icons.Default.Delete, null)
                    }
                    Column {
                        val (correct, total) = game.score
                        Text("$correct / $total")
                        Text(game.detail.timestamp.toString())
                    }
                }
            }
        }
    }
}

@Composable
private fun GameQuestionsDialog(show: MutableState<Boolean>, questions: List<GameQuestion>) {
    Dialog(onDismissRequest = { show.value = false }) {
        Card(border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                questions.forEachIndexed { idx, it ->
                    val correct = it.answer.answer == it.question.correctAnswer
                    Text("""
                        ${idx + 1}. ${it.question.question}
                        - (${if (correct) "correct" else "incorrect"}) ${it.answer.answer}
                    """.trimIndent())
                }
            }
        }
    }
}