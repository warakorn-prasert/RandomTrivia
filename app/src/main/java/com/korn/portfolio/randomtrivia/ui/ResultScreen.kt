package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.model.GameSummary.Companion.summary
import com.korn.portfolio.randomtrivia.model.PastGame
import com.korn.portfolio.randomtrivia.model.QuestionAnswer
import java.util.Date

@Composable
fun ResultScreen(questionAnswers: List<QuestionAnswer>, onGameSaved: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        val triviaViewModel: TriviaViewModel = viewModel(factory = TriviaViewModel.Factory)
        val saveUiState: MutableState<UiState<Boolean>> =
            remember { mutableStateOf(UiState.Idle()) }
        val (category, difficulty, total, correctAnswers, incorrectAnswers) = questionAnswers.summary()
        val pastGame = PastGame(
            datetime = Date(),
            questionAnswers = questionAnswers
        )
        if (saveUiState.value is UiState.Success) {
            onGameSaved()
        }
        LazyColumn {
            item {
                Column {
                    Text("Category : $category")
                    Text("Difficulty : $difficulty")
                    Text("Total : $total ,  Correct : $correctAnswers ,  Incorrect : $incorrectAnswers")
                }
            }
            items(questionAnswers.size) {
                QuestionCard(
                    label = "${it + 1} / $total",
                    questionAnswer = questionAnswers[it],
                    enabled = false,
                    showResult = true,
                    modifier = Modifier.padding(12.dp)
                )
            }
            item {
                Button(
                    onClick = { triviaViewModel.savePastGame(pastGame, saveUiState) },
                    enabled = saveUiState.value is UiState.Idle
                ) {
                    Text(when (saveUiState.value) {
                        is UiState.Error -> "Retry"
                        is UiState.Idle -> "Save game"
                        is UiState.Loading -> "Saving"
                        is UiState.Success -> "Saved"
                    })
                }
            }
            item {
                if (saveUiState.value is UiState.Error)
                    Text("Failed to save. ${saveUiState.value}")
            }
        }
    }
}