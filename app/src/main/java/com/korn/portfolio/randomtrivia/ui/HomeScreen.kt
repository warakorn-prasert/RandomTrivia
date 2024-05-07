package com.korn.portfolio.randomtrivia.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.model.GameSummary
import com.korn.portfolio.randomtrivia.model.GameSummary.Companion.summary

@Composable
fun HomeScreen() {
    val triviaViewModel: TriviaViewModel = viewModel(factory = TriviaViewModel.Factory)
    val pastGames by triviaViewModel.getAllPastGames().collectAsState(emptyList())
    val stats = mutableMapOf<String, GameSummary>()
    pastGames.forEach { pastGame ->
        val summary = pastGame.questionAnswers.summary()
        val key = "${summary.category} (${summary.difficulty})"
        if (stats.contains(key)) {
            stats[key] = stats[key].let {
                it!!.copy(
                    total = it.total + summary.total,
                    correctAnswers = it.correctAnswers + summary.correctAnswers,
                    incorrectAnswers = it.incorrectAnswers + summary.incorrectAnswers
                )
            }
        } else {
            stats[key] = summary
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("Statistics")
        }
        items(items = stats.keys.toList(), key = { it }) { key ->
            var expanded by remember { mutableStateOf(false) }
            val summary = stats[key]!!
            val detail = """
                - total : ${summary.total}
                - correct : ${summary.correctAnswers}
                - incorrect : ${summary.incorrectAnswers}
            """.trimIndent()
            Text(
                if (expanded) "$key\n$detail" else key,
                Modifier
                    .padding(12.dp)
                    .border(
                        width = DividerDefaults.Thickness,
                        color = DividerDefaults.color,
                        shape = CardDefaults.shape
                    )
                    .animateContentSize()
                    .clickable {
                        expanded = !expanded
                    }
            )
        }
    }
}