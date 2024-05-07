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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.model.QuestionAnswer

@Composable
fun PlayScreen(
    showTopAppBar: MutableState<Boolean>,
    questionAnswers: List<QuestionAnswer>,
    onResult: (List<QuestionAnswer>) -> Unit
) {
    showTopAppBar.value = false
    Column(
        Modifier.fillMaxSize(),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        LazyColumn {
            items(questionAnswers.size) {
                QuestionCard(
                    label = "${it + 1} / ${questionAnswers.size}",
                    questionAnswer = questionAnswers[it],
                    modifier = Modifier.padding(12.dp)
                )
            }
            item {
                Button({
                    onResult(questionAnswers)
                    showTopAppBar.value = true
                }) {
                    Text("End game")
                }
            }
        }
    }
}