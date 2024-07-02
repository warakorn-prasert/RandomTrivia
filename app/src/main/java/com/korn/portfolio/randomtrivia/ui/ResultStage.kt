@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.database.model.Game

private val Game.score: Pair<Int, Int>
    get() = questions
        .fold(0 to 0) { acc, (question, answer, _) ->
            val correct = question.correctAnswer == answer.answer
            acc.copy(
                first = acc.first + if (correct) 1 else 0,
                second = acc.second + 1
            )
        }

@Composable
fun ResultStage(game: Game) {
    val (correct, total) = game.score
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontalPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Result", fontWeight = FontWeight.ExtraBold)
        Text("$correct / $total")
        Text("Time taken ${game.detail.totalTimeSecond} seconds")
        Text("Date ${game.detail.timestamp}")
    }
}