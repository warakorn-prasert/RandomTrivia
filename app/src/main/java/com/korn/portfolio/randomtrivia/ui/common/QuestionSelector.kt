@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import android.view.View
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.GameQuestion
import com.korn.portfolio.randomtrivia.ui.common.QuestionButtonState.Companion.Badge
import com.korn.portfolio.randomtrivia.ui.common.QuestionButtonState.Companion.inspectButtonState
import com.korn.portfolio.randomtrivia.ui.common.QuestionButtonState.Companion.playingButtonState
import com.korn.portfolio.randomtrivia.ui.previewdata.getCategory
import com.korn.portfolio.randomtrivia.ui.previewdata.getGameQuestion
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ColumnScope.QuestionSelector(
    currentIdx: Int,
    questions: List<GameQuestion>,
    onSelect: (Int) -> Unit,
    paddingValues: PaddingValues = PaddingValues(),
    isInspecting: Boolean = false,
    spaceUnderQuestionIdx: (@Composable () -> Unit)? = null
) {
    val layoutDirection = when (LocalConfiguration.current.layoutDirection) {
        View.LAYOUT_DIRECTION_RTL -> LayoutDirection.Rtl
        else -> LayoutDirection.Ltr
    }

    val totalQuestions = questions.size
    CompositionLocalProvider(LocalMinimumInteractiveComponentEnforcement provides false) {
        Row(
            modifier = Modifier
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection)
                )
                .fillMaxWidth()
                .height(40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onSelect(currentIdx - 1) },
                enabled = currentIdx > 0
            ) {
                Icon(Icons.AutoMirrored.Default.ArrowBack, "Button to go to previous question.")
            }
            Text("Q ${currentIdx + 1} / $totalQuestions")
            IconButton(
                onClick = { onSelect(currentIdx + 1) },
                enabled = currentIdx + 1 < totalQuestions
            ) {
                Icon(Icons.AutoMirrored.Default.ArrowForward, "Button to go to next question.")
            }
        }
    }

    spaceUnderQuestionIdx?.invoke()

    var currentTabIdx by remember { mutableIntStateOf(0) }
    LaunchedEffect(currentIdx) {
        val correctTabIdx = floor(currentIdx / 10f).toInt()
        currentTabIdx = correctTabIdx
    }
    val totalTabs = ceil(totalQuestions / 10f).toInt()
    TabRow(
        selectedTabIndex = currentTabIdx,
        indicator = { tabPositions ->
            Box(
                Modifier
                    .tabIndicatorOffset(tabPositions[currentTabIdx])
                    // Default indicator does not have rounded corners.
                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(color = MaterialTheme.colorScheme.primary)
            )
        }
    ) {
        repeat(totalTabs) { tabIdx ->
            val min = 1 + tabIdx * 10
            val max = (min + 9).coerceAtMost(totalQuestions)
            Tab(selected = currentTabIdx == tabIdx, onClick = { currentTabIdx = tabIdx }) {
                Box(
                    modifier = Modifier.heightIn(min = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("$min-$max", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
    FlowRow(
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(
                start = paddingValues.calculateStartPadding(layoutDirection),
                top = 16.dp,
                end = paddingValues.calculateEndPadding(layoutDirection),
                bottom = paddingValues.calculateBottomPadding()
            ),
        horizontalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        maxItemsInEachRow = 5
    ) {
        val min = 1 + currentTabIdx * 10
        val max = (min + 9).coerceAtMost(totalQuestions)
        (min..max).forEach { questionNumber ->
            val state = questions[questionNumber - 1].run {
                if (isInspecting) inspectButtonState
                else playingButtonState
            }
            Box {
                QuestionButton(
                    questionNumber = questionNumber,
                    onClick = { onSelect(questionNumber - 1) },
                    state = state
                )
                if (state == QuestionButtonState.CORRECT || state == QuestionButtonState.INCORRECT)
                    Badge(state)
            }
        }
    }
}

@Composable
private fun QuestionButton(
    questionNumber: Int,
    onClick: () -> Unit,
    state: QuestionButtonState
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(width = 40.dp, height = 32.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = state.containerColor(),
            contentColor = state.contentColor()
        ),
        border = state.borderColor?.let { BorderStroke(1.dp, it()) },
        contentPadding = PaddingValues()
    ) {
        Text("$questionNumber")
    }
}

private data class BadgeData(
    @DrawableRes val resId: Int,
    val contentDescription: String?,
    val containerColor: @Composable () -> Color,
    val contentColor: @Composable () -> Color
)

private enum class QuestionButtonState(
    val containerColor: @Composable () -> Color,
    val contentColor: @Composable () -> Color,
    val borderColor: @Composable (() -> Color)? = null,
    val badgeData: BadgeData? = null
) {
    ANSWERED(
        containerColor = { MaterialTheme.colorScheme.surfaceVariant },
        contentColor = { MaterialTheme.colorScheme.onSurfaceVariant }
    ),
    UNANSWERED(
        containerColor = { Color.Transparent },
        contentColor = { MaterialTheme.colorScheme.onSurfaceVariant },
        borderColor = { MaterialTheme.colorScheme.outline }
    ),
    CORRECT(
        containerColor = { MaterialTheme.colorScheme.secondaryContainer },
        contentColor = { MaterialTheme.colorScheme.onSecondaryContainer },
        badgeData = BadgeData(
            resId = R.drawable.ic_check_circle,
            contentDescription = "Correct answer icon.",
            containerColor = { MaterialTheme.colorScheme.tertiary },
            contentColor = { MaterialTheme.colorScheme.onError },
        )
    ),
    INCORRECT(
        containerColor = { MaterialTheme.colorScheme.errorContainer },
        contentColor = { MaterialTheme.colorScheme.onErrorContainer },
        badgeData = BadgeData(
            resId = R.drawable.ic_cancel,
            contentDescription = "Incorrect answer icon.",
            containerColor = { MaterialTheme.colorScheme.error },
            contentColor = { MaterialTheme.colorScheme.onError },
        )
    );

    companion object {
        @Composable
        fun BoxScope.Badge(state: QuestionButtonState) {
            require(state.badgeData != null) { "badgeData should not be null." }
            Box(
                modifier = Modifier
                    .offset(x = 4.dp, y = (-2).dp)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    Modifier
                        .size((16f * 0.75f / sqrt(2f)).dp)
                        .background(state.badgeData.contentColor())
                )
                Icon(
                    painter = painterResource(state.badgeData.resId),
                    contentDescription = state.badgeData.contentDescription,
                    modifier = Modifier.size(16.dp),
                    tint = state.badgeData.containerColor()
                )
            }
        }

        val GameQuestion.playingButtonState: QuestionButtonState
            get() =
                if (answer.answer in question.run { incorrectAnswers + correctAnswer }) ANSWERED
                else UNANSWERED

        val GameQuestion.inspectButtonState: QuestionButtonState
            get() = when (answer.answer) {
                question.correctAnswer -> CORRECT
                in question.incorrectAnswers -> INCORRECT
                else -> UNANSWERED
            }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuestionSelectorPreview() {
    RandomTriviaTheme {
        Column {
            QuestionSelector(
                currentIdx = 0,
                questions = List(12) {
                    getGameQuestion(getCategory(it), correctAnswer = Random.nextBoolean())
                },
                onSelect = {},
                isInspecting = false
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InspectModePreview() {
    RandomTriviaTheme {
        Column {
            QuestionSelector(
                currentIdx = 0,
                questions = List(12) {
                    getGameQuestion(getCategory(it), correctAnswer = Random.nextBoolean())
                },
                onSelect = {},
                isInspecting = true
            )
        }
    }
}