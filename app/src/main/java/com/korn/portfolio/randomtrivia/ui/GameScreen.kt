@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun GameScreen(paddingValues: PaddingValues) {
    val triviaViewModel: TriviaViewModel = viewModel(factory = TriviaViewModel.Factory)
    val games by triviaViewModel.games.collectAsState(emptyList())
    LazyColumn(Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp)) {
        item {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly, Alignment.CenterVertically) {
                Text("${games.size} games")
                IconButton(triviaViewModel::deleteAllGames) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }
        items(games, key = { it.detail.gameId }) {
            Card(Modifier.padding(bottom = 4.dp)) {
                Column(Modifier.width(IntrinsicSize.Max).padding(8.dp)) {
                    Text(it.detail.timeStamp)
                    HorizontalDivider()
                    it.questions.forEachIndexed { idx, it ->
                        Text("${idx + 1}. (${it.question.category?.id}) ${it.question.question.question}")
                    }
                }
            }
        }
    }
}