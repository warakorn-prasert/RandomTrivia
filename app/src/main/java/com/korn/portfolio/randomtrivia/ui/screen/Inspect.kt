@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch

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
    goBack: () -> Unit,
    replay: (Game) -> Unit,
    game: Game,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { game.questions.size })
    val currentIdx = pagerState.currentPage
    ScrimmableBottomSheetScaffold(
        modifier = modifier,
        sheetContent = { paddingValues, spaceUnderPeekContent ->
            val scope = rememberCoroutineScope()
            QuestionSelector(
                currentIdx = currentIdx,
                questions = game.questions,
                onSelect = { idx ->
                    scope.launch {
                        pagerState.animateScrollToPage(idx)
                    }
                },
                paddingValues = paddingValues,
                isInspecting = true,
                spaceUnderQuestionIdx = spaceUnderPeekContent
            )
        },
        sheetContentPeekHeight = 56.dp,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButtonWithText(
                        onClick = goBack,
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Button to return to main menu.",
                        text = "Exit"
                    )
                },
                actions = {
                    IconButtonWithText(
                        onClick = { replay(game) },
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
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val question = game.questions[currentIdx]
            GameStatus(
                second = game.detail.totalTimeSecond,
                category = question.category,
                difficulty = question.question.difficulty,
                modifier = Modifier
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)
                    .fillMaxWidth()
            )
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 16.dp,
                userScrollEnabled = false
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
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
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(answer, Modifier.weight(1f))
                        if (state.trailingIcon != null) {
                            Spacer(Modifier.width(48.dp))
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
}

@Preview
@Composable
private fun InspectPreview() {
    RandomTriviaTheme {
        Inspect(
            goBack = {},
            replay = {},
            game = getGame(totalQuestions = 44, played = true)
        )
    }
}

@Preview
@Composable
private fun IncorrectPreview() {
    val answers = listOf("a", "b", "c", "d")
    RandomTriviaTheme {
        InspectAnswerButtons(
            userAnswer = answers[0],
            answers = answers,
            correctAnswer = answers[2]
        )
    }
}

@Preview
@Composable
private fun UnansweredPreview() {
    val answers = listOf("a", "b", "c", "d")
    RandomTriviaTheme {
        InspectAnswerButtons(
            userAnswer = "",
            answers = answers,
            correctAnswer = answers[2]
        )
    }
}

@Preview
@Composable
private fun CorrectPreview() {
    val answers = listOf("a", "b", "c", "d")
    RandomTriviaTheme {
        InspectAnswerButtons(
            userAnswer = answers[0],
            answers = answers,
            correctAnswer = answers[0]
        )
    }
}

@Preview
@Composable
private fun OverflowPreview() {
    val answers = List(4) { "overflow$it".repeat(10) }
    RandomTriviaTheme {
        InspectAnswerButtons(
            userAnswer = answers[0],
            answers = answers,
            correctAnswer = answers[2]
        )
    }
}