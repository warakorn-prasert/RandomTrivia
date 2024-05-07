package com.korn.portfolio.randomtrivia.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.model.GameSummary.Companion.summary
import com.korn.portfolio.randomtrivia.model.PastGame
import com.korn.portfolio.randomtrivia.model.QuestionAnswer
import java.text.DateFormat.getDateTimeInstance
import java.util.Date
import java.util.UUID

fun Date.toPrettyString(): String {
    return getDateTimeInstance().format(this)
}

@Composable
fun PastGamesScreen(showTopAppBar: MutableState<Boolean>) {
    val triviaViewModel: TriviaViewModel = viewModel(factory = TriviaViewModel.Factory)
    var gameState: GameState by remember { mutableStateOf(GameState.Set) }
    when (val s = gameState) {
        is GameState.Set -> {
            val pastGames by triviaViewModel.getAllPastGames().collectAsState(emptyList())
            BrowseScreen(
                pastGames = pastGames,
                replayAction = { gameState = GameState.Play(it) },
                deleteAction = triviaViewModel::deletePastGame
            )
        }
        is GameState.Play ->
            PlayScreen(showTopAppBar, s.questions) {
                gameState = GameState.Result(s.questions)
            }
        is GameState.Result ->
            ResultScreen(s.questionAnswers, {  })
    }
}

@Composable
private fun BrowseScreen(
    pastGames: List<PastGame>,
    replayAction: (List<QuestionAnswer>) -> Unit,
    deleteAction: (UUID) -> Unit
) {
    LazyColumn {
        items(count = pastGames.size, key = { pastGames[it].id }) { it ->
            PastGameCard(
                pastGame = pastGames[it],
                replayAction = { id ->
                    replayAction(pastGames.first { it.id == id }.questionAnswers)
                },
                deleteAction = deleteAction,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun PastGameCard(
    pastGame: PastGame,
    replayAction: (UUID) -> Unit,
    deleteAction: (UUID) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val (category, difficulty, total, correctAnswers, incorrectAnswers) =
        pastGame.questionAnswers.summary()
    Card(modifier.animateContentSize()) {
        Column {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween,
                Alignment.CenterVertically
            ) {
                Column {
                    Text(pastGame.datetime.toPrettyString())
                    Text(category)
                    Text(difficulty)
                }
                Icon(
                    imageVector =
                    if (expanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = !expanded }
                )
            }

            if (expanded) {
                HorizontalDivider()
                Text("Total : $total ,  Correct : $correctAnswers ,  Incorrect : $incorrectAnswers")
                Row {
                    Button({ replayAction(pastGame.id) }) { Text("Replay") }
                    Button({ deleteAction(pastGame.id) }) { Text("Delete") }
                }
            }
        }
    }

}