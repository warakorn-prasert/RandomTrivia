@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.GameQuestion
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.ui.common.IconButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.QuestionSelector
import com.korn.portfolio.randomtrivia.ui.common.ScrimmableBottomSheetScaffold
import com.korn.portfolio.randomtrivia.ui.hhmmssFrom
import com.korn.portfolio.randomtrivia.ui.previewdata.getCategory
import com.korn.portfolio.randomtrivia.ui.previewdata.getGameQuestion
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.PlayingViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.displayName

enum class AnswerButtonState(
    val containerColor: @Composable () -> Color,
    val contentColor: @Composable () -> Color,
) {
    ANSWERED(
        containerColor = { MaterialTheme.colorScheme.primaryContainer },
        contentColor = { MaterialTheme.colorScheme.onPrimaryContainer }
    ),
    UNANSWERED(
        containerColor = { MaterialTheme.colorScheme.surfaceContainerLow },
        contentColor = { MaterialTheme.colorScheme.onSurface }
    );

    companion object {
        fun getState(userAnswer: String, answer: String) =
            if (userAnswer == answer) ANSWERED else UNANSWERED
    }
}

@Composable
fun Playing(
    game: Game,
    onExit: () -> Unit,
    onSubmit: (Game) -> Unit
) {
    val viewModel: PlayingViewModel = viewModel(factory = PlayingViewModel.Factory(game))
    Playing(
        exitAction = { viewModel.exit(onExit) },
        submitAction = { viewModel.submit(onSubmit) },
        currentIdx = viewModel.currentIdx,
        questions = viewModel.questions,
        selectQuestion = viewModel::selectQuestion,
        submittable = viewModel.submittable,
        second = viewModel.second,
        answerAction = viewModel::answer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Playing(
    exitAction: () -> Unit,
    submitAction: () -> Unit,
    currentIdx: Int,
    questions: List<GameQuestion>,
    selectQuestion: (Int) -> Unit,
    submittable: Boolean,
    second: Int,
    answerAction: (String) -> Unit
) {
    BackHandler(onBack = exitAction)
    ScrimmableBottomSheetScaffold(
        sheetContent = { paddingValues ->
            QuestionSelector(
                currentIdx = currentIdx,
                questions = questions,
                selectAction = selectQuestion,
                paddingValues = paddingValues
            )
        },
        sheetContentPeekHeight = 56.dp,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButtonWithText(
                        onClick = exitAction,
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Button to return to main menu.",
                        text = "Exit"
                    )
                },
                actions = {
                    IconButtonWithText(
                        onClick = submitAction,
                        imageVector = Icons.Default.Check,
                        contentDescription = "Button to submit game answers.",
                        text = "Submit",
                        enabled = submittable
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
            val question = questions[currentIdx]
            GameStatus(
                second = second,
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
            AnswerButtons(
                userAnswer = question.answer.answer,
                answers = question.question.run { incorrectAnswers + correctAnswer },
                answerAction = answerAction,
                modifier = Modifier
                    .padding(vertical = 16.dp)
            )
        }
    }
}

@Composable
fun GameStatus(
    second: Int,
    category: Category?,
    difficulty: Difficulty?,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelLarge) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(R.drawable.ic_timer), "Timer icon")
                Text(hhmmssFrom(second))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Category")
                Text(category.displayName)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Difficulty")
                Text(difficulty.displayName)
            }
        }
    }
}

@Composable
fun QuestionStatementCard(questionStatement: String, modifier: Modifier = Modifier) {
    OutlinedCard(modifier) {
        Box {
            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier.verticalScroll(scrollState),
                contentAlignment = Alignment.Center
            ) {
                Text(questionStatement, Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
            }
            val scrollable = scrollState.run { canScrollForward || canScrollBackward }
            if (scrollable)
                Column(Modifier
                    .align(Alignment.TopEnd)
                    .padding(vertical = 12.dp)
                ) {
                    val position = scrollState.run { value.toFloat() / maxValue }
                    if (position > 0) Spacer(Modifier.weight(position))
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(topStart = 50f, bottomStart = 50f))
                            .size(width = 4.dp, height = 40.dp)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    if (position < 1) Spacer(Modifier.weight(1 - position))
                }
        }
    }
}

@Composable
private fun AnswerButtons(
    userAnswer: String,
    answers: List<String>,
    answerAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        answers.forEachIndexed { idx, answer ->
            val state = AnswerButtonState.getState(userAnswer, answer)
            ElevatedCard(
                onClick = { answerAction(answer) },
                modifier = Modifier
                    .fillMaxWidth(),
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
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PlayingPreview() {
    RandomTriviaTheme {
        Playing(
            exitAction = {},
            submitAction = {},
            currentIdx = 2,
            questions = List(12) { getGameQuestion(getCategory(it % 2)) },
            selectQuestion = {},
            submittable = true,
            second = 100,
            answerAction = {}
        )
    }
}

@Preview
@Composable
private fun OverflowQuestionPreview() {
    RandomTriviaTheme {
        Playing(
            exitAction = {},
            submitAction = {},
            currentIdx = 0,
            questions = listOf(getGameQuestion(getCategory(0), overflow = true)),
            selectQuestion = {},
            submittable = true,
            second = 100,
            answerAction = {}
        )
    }
}

@Preview
@Composable
private fun OverflowAnswerButtonsPreview() {
    RandomTriviaTheme {
        AnswerButtons(
            userAnswer = "",
            answers = listOf("OverflowAnswer".repeat(20)),
            answerAction = {}
        )
    }
}