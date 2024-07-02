@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.GameQuestion

private fun Game.answer(questionIdx: Int, answer: String): Game {
    val updatedQuestions = questions.toMutableList().apply {
        val updatedQuestion =  questions[questionIdx].let {
            it.copy(answer = it.answer.copy(answer = answer))
        }
        set(questionIdx, updatedQuestion)
    }
    return copy(questions = updatedQuestions)
}

@Composable
fun PlayingStage(
    game: MutableState<Game>,
    askToExit: () -> Unit,
    timerSecond: Int,
    onDone: (Game) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Time taken $timerSecond seconds",
                modifier = Modifier.weight(1f),
                fontStyle = FontStyle.Italic
            )
            TextButton(askToExit) { Text("Exit") }
        }
        HorizontalDivider(Modifier.padding(vertical = 12.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            var idx by remember { mutableIntStateOf(0) }
            IconButton(
                onClick = { idx-- },
                modifier = Modifier.align(Alignment.CenterVertically),
                enabled = idx > 0,
                content = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null) }
            )
            if (game.value.questions.isNotEmpty()) {
                if (idx < game.value.questions.size) {
                    AnswerEditor(
                        gameQuestion = game.value.questions[idx],
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState()),
                        answerAction = { newAnswer ->
                            game.value = game.value.answer(idx, newAnswer)
                        }
                    )
                } else {
                    Button({ onDone(game.value) }) {
                        Text("Submit")
                    }
                }
            } else {
                Text("No questions")
            }
            IconButton(
                onClick = { idx++ },
                modifier = Modifier.align(Alignment.CenterVertically),
                enabled = idx < game.value.questions.size,
                content = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
            )
        }
    }
}

@Composable
private fun AnswerEditor(
    gameQuestion: GameQuestion,
    modifier: Modifier = Modifier,
    answerAction: (String) -> Unit
) {
    val category = gameQuestion.category?.name ?: "Uncategorized"
    val difficulty = gameQuestion.question.difficulty
    val answers: List<String> = gameQuestion.question
        .run { incorrectAnswers + correctAnswer }
    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("($difficulty) ${gameQuestion.category?.id} $category")
        Card {
            Text(gameQuestion.question.question, Modifier.padding(12.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            answers.forEachIndexed { idx, answer ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                        .clickable { answerAction(answer) },
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                        .takeIf { answer == gameQuestion.answer.answer }
                ) {
                    Text("${idx + 1}. $answer", Modifier.padding(12.dp))
                }
            }
        }
    }
}