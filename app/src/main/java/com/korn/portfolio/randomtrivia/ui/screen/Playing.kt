@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import android.os.Parcelable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.GameQuestion
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.ui.common.GameQuestionParceler
import com.korn.portfolio.randomtrivia.ui.common.IconButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.QuestionSelector
import com.korn.portfolio.randomtrivia.ui.common.ScrimmableBottomSheetScaffold
import com.korn.portfolio.randomtrivia.ui.common.displayName
import com.korn.portfolio.randomtrivia.ui.common.hhmmssFrom
import com.korn.portfolio.randomtrivia.ui.previewdata.PreviewWindowSizes
import com.korn.portfolio.randomtrivia.ui.previewdata.getGame
import com.korn.portfolio.randomtrivia.ui.previewdata.windowSizeForPreview
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import java.util.Date
import java.util.UUID
import kotlin.concurrent.timer

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

private fun MutableList<GameQuestion>.answer(questionIdx: Int, answer: String) {
    this[questionIdx] = this[questionIdx].let {
        it.copy(
            answer = it.answer.copy(
                answer = answer
            )
        )
    }
}

@Parcelize
private data class WrappedGameQuestion(
    val question: @WriteWith<GameQuestionParceler> GameQuestion
) : Parcelable

// Ref. : https://stackoverflow.com/a/68887484
private val GameQuestionListSaver = listSaver<SnapshotStateList<GameQuestion>, WrappedGameQuestion>(
    save = { stateList -> stateList.map { WrappedGameQuestion(it) }.toList() },
    restore = { it.map { it.question }.toMutableStateList() },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Playing(
    game: Game,
    onExit: () -> Unit,
    onSubmit: (Game) -> Unit,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    BackHandler { onExit() }

    val questions = rememberSaveable(saver = GameQuestionListSaver) {
        game.questions.toMutableStateList()
    }
    val pagerState = rememberPagerState(pageCount = { questions.size })
    val currentIdx = pagerState.currentPage

    val second by rememberSaveable {
        mutableIntStateOf(0).apply {
            timer(period = 1000L) { intValue++ }
        }
    }

    ScrimmableBottomSheetScaffold(
        modifier = modifier,
        sheetContent = { paddingValues, spaceUnderPeekContent ->
            val scope = rememberCoroutineScope()
            QuestionSelector(
                currentIdx = currentIdx,
                questions = questions,
                onSelect = { idx ->
                    scope.launch {
                        pagerState.animateScrollToPage(idx)
                    }
                },
                paddingValues = paddingValues,
                spaceUnderQuestionIdx = spaceUnderPeekContent
            )
        },
        sheetContentPeekHeight = 56.dp,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButtonWithText(
                        onClick = onExit,
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Button to return to main menu.",
                        text = "Exit"
                    )
                },
                actions = {
                    IconButtonWithText(
                        onClick = {
                            val newGameId = UUID.randomUUID()
                            val saveableGame = game.copy(
                                detail = game.detail.copy(
                                    timestamp = Date(),
                                    totalTimeSecond = second,
                                    gameId = newGameId
                                ),
                                questions = questions.map {
                                    it.copy(
                                        answer = it.answer.copy(
                                            gameId = newGameId
                                        )
                                    )
                                }
                            )
                            onSubmit(saveableGame)
                        },
                        imageVector = Icons.Default.Check,
                        contentDescription = "Button to submit game answers.",
                        text = "Submit",
                        enabled = questions.all {
                            it.answer.answer in it.question.run {
                                incorrectAnswers + correctAnswer
                            }
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        val layoutDir = LocalLayoutDirection.current
        Column(
            modifier = Modifier
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDir),
                    top = paddingValues.calculateTopPadding(),
                    end = paddingValues.calculateStartPadding(layoutDir)
                )
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val question = questions[currentIdx]
            GameStatus(
                second = second,
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
                val showInColumn = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
                        || windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.EXPANDED
                if (showInColumn)
                    Column(
                        modifier = Modifier
                            .padding(bottom = paddingValues.calculateBottomPadding())
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        QuestionStatementCard(
                            questionStatement = question.question.question,
                            modifier = Modifier.weight(1f).wrapContentSize()
                        )
                        AnswerButtons(
                            userAnswer = question.answer.answer,
                            answers = question.question.run { incorrectAnswers + correctAnswer },
                            onAnswer = { questions.answer(currentIdx, it) },
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                else
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuestionStatementCard(
                            questionStatement = question.question.question,
                            modifier = Modifier
                                .padding(bottom = 12.dp + paddingValues.calculateBottomPadding())
                                .weight(1f)
                                .wrapContentSize()
                        )
                        val configuration = LocalConfiguration.current
                        val screenWidth = configuration.screenWidthDp.dp
                        AnswerButtons(
                            userAnswer = question.answer.answer,
                            answers = question.question.run { incorrectAnswers + correctAnswer },
                            onAnswer = { questions.answer(currentIdx, it) },
                            modifier = Modifier
                                .padding(bottom = 16.dp + paddingValues.calculateBottomPadding())
                                .widthIn(max = screenWidth * 2 / 3)
                        )
                    }
            }
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
    onAnswer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // make scrollable in case of not enough space
    val scrollState = rememberScrollState()

    // scroll to top every revisit
    LaunchedEffect(Unit) {
        scrollState.scrollTo(0)
    }

    Column(
        modifier = Modifier
            .width(IntrinsicSize.Max)  // to have same width
            .verticalScroll(scrollState)
            .then(modifier),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        answers.forEachIndexed { idx, answer ->
            val state = AnswerButtonState.getState(userAnswer, answer)
            ElevatedCard(
                onClick = { onAnswer(answer) },
                colors = CardDefaults.elevatedCardColors().copy(
                    containerColor =
                        if (state == AnswerButtonState.ANSWERED) MaterialTheme.colorScheme.surface
                        else state.containerColor(),
                    contentColor = state.contentColor()
                )
            ) {
                val fillColor = state.containerColor()
                val fillPercent by animateFloatAsState(
                    targetValue = if (state == AnswerButtonState.ANSWERED) 1f else 0f,
                    animationSpec = tween(1000)
                )
                Box(
                    Modifier
                        .height(IntrinsicSize.Max)
                        .fillMaxWidth()
                        .drawBehind {
                            drawRect(
                                color = fillColor,
                                size = Size(
                                    width = this.size.width * fillPercent,
                                    height = this.size.height
                                )
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 8.dp),
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
}

@PreviewWindowSizes
@Composable
private fun PlayingPreview() {
    RandomTriviaTheme {
        Playing(
            game = getGame(11),
            onExit = {},
            onSubmit = {},
            windowSizeClass = windowSizeForPreview()
        )
    }
}

@Preview
@Composable
private fun OverflowQuestionPreview() {
    RandomTriviaTheme {
        Playing(
            game = getGame(1, true),
            onExit = {},
            onSubmit = {},
            windowSizeClass = windowSizeForPreview()
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
            onAnswer = {}
        )
    }
}