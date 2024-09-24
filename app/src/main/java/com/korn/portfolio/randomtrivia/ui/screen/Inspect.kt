@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.ui.common.IconButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.QuestionSelector
import com.korn.portfolio.randomtrivia.ui.common.ScrimmableBottomSheetScaffold
import com.korn.portfolio.randomtrivia.ui.previewdata.getGame
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

private enum class InspectAnswerButtonState(
    val containerColor: @Composable () -> Color,
    val contentColor: @Composable () -> Color,
    val trailingIcon: ImageVector? = null
) {
    USER_CORRECT(
        containerColor = AnswerButtonState.ANSWERED.containerColor,
        contentColor = AnswerButtonState.ANSWERED.contentColor,
        trailingIcon = Icons.Default.Check
    ),
    USER_INCORRECT(
        containerColor = { MaterialTheme.colorScheme.errorContainer },
        contentColor = { MaterialTheme.colorScheme.onErrorContainer },
        trailingIcon = Icons.Default.Close
    ),
    REVEAL_CORRECT(
        containerColor = AnswerButtonState.UNANSWERED.containerColor,
        contentColor = AnswerButtonState.UNANSWERED.contentColor,
        trailingIcon = Icons.Default.Check
    ),
    UNANSWERED(
        containerColor = AnswerButtonState.UNANSWERED.containerColor,
        contentColor = AnswerButtonState.UNANSWERED.contentColor
    );

    companion object {
        fun getState(userAnswer: String, answer: String, correctAnswer: String) =
            when {
                userAnswer == answer && answer == correctAnswer -> USER_CORRECT
                userAnswer == answer && answer != correctAnswer -> USER_INCORRECT
                userAnswer != answer && answer == correctAnswer -> REVEAL_CORRECT
                else -> UNANSWERED
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Inspect(
    onBack: () -> Unit,
    onReplay: (Game) -> Unit,
    game: Game
) {
    BackHandler(onBack = onBack)
    var currentIdx by remember { mutableIntStateOf(0) }
        ScrimmableBottomSheetScaffold(
            sheetContent = { paddingValues ->
                QuestionSelector(
                    currentIdx = currentIdx,
                    questions = game.questions,
                    selectAction = { currentIdx = it },
                    paddingValues = paddingValues,
                    isInspecting = true
                )
            },
            sheetContentPeekHeight = 56.dp,
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButtonWithText(
                            onClick = onBack,
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Button to return to main menu.",
                            text = "Exit"
                        )
                    },
                    actions = {
                        IconButtonWithText(
                            onClick = { onReplay(game) },
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Replay button.",
                            text = "Replay"
                        )
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val question = game.questions[currentIdx]
                GameStatus(
                    second = game.detail.totalTimeSecond,
                    category = question.category,
                    difficulty = question.question.difficulty,
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 16.dp)
                        .fillMaxWidth()
                )
                QuestionStatementCard(
                    questionStatement = question.question.question,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentSize()
                )
                InspectAnswerButtons(
                    userAnswer = question.answer.answer,
                    answers = question.question.run { incorrectAnswers + correctAnswer },
                    correctAnswer = question.question.correctAnswer,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                )
            }
        }
}

@Composable
private fun InspectAnswerButtons(
    userAnswer: String,
    answers: List<String>,
    correctAnswer: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        answers.forEachIndexed { idx, answer ->
            val state = InspectAnswerButtonState.getState(userAnswer, answer, correctAnswer)
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors().copy(
                    containerColor = state.containerColor(),
                    contentColor = state.contentColor()
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("${idx + 1}.")
                    Text(answer)
                    if (state.trailingIcon != null) {
                        Spacer(Modifier.weight(1f).widthIn(48.dp))
                        Icon(
                            imageVector = state.trailingIcon,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun InspectPreview() {
    RandomTriviaTheme {
        Inspect(
            onBack = {},
            onReplay = {},
            game = getGame(totalQuestions = 44, played = true)
        )
    }
}

@Preview
@Composable
private fun InspectAnswerButtonsPreview() {
    val answers = listOf("a", "b", "c", "d")
    InspectAnswerButtons(
        userAnswer = answers[0],
        answers = answers,
        correctAnswer = answers[2]
    )
}