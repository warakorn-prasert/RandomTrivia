@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import com.korn.portfolio.randomtrivia.ui.common.CheckboxWithText
import com.korn.portfolio.randomtrivia.ui.common.FilterSortMenuBar
import com.korn.portfolio.randomtrivia.ui.common.RadioButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.SearchableTopBarWithBackButton
import com.korn.portfolio.randomtrivia.ui.common.displayName
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import java.util.UUID

private enum class QuestionFilter(
    val displayText: String,
    val invoke: (List<Question>) -> List<Question>
) {
    ALL("All", { it }),
    EASY("Easy", { all -> all.filter { it.difficulty == Difficulty.EASY } }),
    MEDIUM("Medium", { all -> all.filter { it.difficulty == Difficulty.MEDIUM } }),
    HARD("Hard", { all -> all.filter { it.difficulty == Difficulty.HARD } })
}

private enum class QuestionSort(
    val displayText: String,
    val invoke: (List<Question>) -> List<Question>
) {
    QUESTION_STATEMENT("Question statement (A-Z)", { all -> all.sortedBy { it.question.lowercase() } }),
    DIFFICULTY("Difficulty (Easy-Hard)", { all -> all.sortedBy { it.difficulty.sortIndex } });
}

private val Difficulty.sortIndex: Int
    get() = when (this) {
        Difficulty.EASY -> 0
        Difficulty.MEDIUM -> 1
        Difficulty.HARD -> 2
    }

@Composable
fun Questions(
    categoryName: String,
    questions: List<Question>,
    onExit: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onExit() }

    var searchWord by rememberSaveable { mutableStateOf("") }
    Scaffold(
        modifier = modifier,
        topBar = {
            SearchableTopBarWithBackButton(
                searchWord = searchWord,
                onChange = { searchWord = it },
                onAboutClick = onAboutClick,
                hint = "Search for questions",
                title = categoryName,
                onBackClick = onExit
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            val listState = rememberLazyListState()
            var filter by rememberSaveable { mutableStateOf(QuestionFilter.ALL) }
            var sort by rememberSaveable { mutableStateOf(QuestionSort.QUESTION_STATEMENT) }
            var reverseSort by rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(searchWord, filter, sort, reverseSort) {
                listState.animateScrollToItem(0)
            }

            val itemsInView by remember {
                derivedStateOf {
                    listState.layoutInfo
                        .visibleItemsInfo.map { it.key as UUID }
                }
            }

            QuestionsFilterSortMenuBar(
                filter, { filter = it },
                sort, { sort = it },
                reverseSort, { reverseSort = it }
            )

            val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
            val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val bottomBarPadding = 80.dp
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = (imePadding - navBarPadding - bottomBarPadding + 8.dp).coerceAtLeast(8.dp)
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = questions
                        .let { filter.invoke(it) }
                        .filter {
                            it.question.lowercase().contains(searchWord.lowercase())
                        }
                        .let { sort.invoke(it) }
                        .let { if (reverseSort) it.reversed() else it },
                    key = { it.id }
                ) {
                    val alpha by animateFloatAsState(
                        targetValue = if (it.id in itemsInView) 1f else 0f,
                        animationSpec = tween(
                            durationMillis = 600,
                            delayMillis = 50,
                            easing = LinearOutSlowInEasing
                        )
                    )
                    Box(Modifier.alpha(alpha)) {
                        QuestionCard(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionsFilterSortMenuBar(
    filter: QuestionFilter,
    onFilterChange: (QuestionFilter) -> Unit,
    sort: QuestionSort,
    onSortChange: (QuestionSort) -> Unit,
    reverseSort: Boolean,
    onReverseSortChange: (Boolean) -> Unit
) {
    FilterSortMenuBar(
        selectedFilter = filter,
        filters = QuestionFilter.entries,
        onFilterSelect = onFilterChange,
        filterToString = { it.displayText },
        sortBottomSheetContent = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Sort By")
                CheckboxWithText(
                    checked = reverseSort,
                    onChange = onReverseSortChange,
                    text = "Reversed"
                )
            }
            QuestionSort.entries.forEach {
                RadioButtonWithText(
                    selected = sort == it,
                    onClick = { onSortChange(it) },
                    text = it.displayText
                )
            }
        }
    )
}

@Composable
private fun QuestionCard(question: Question, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier.clickable { expanded = !expanded },
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("(${question.difficulty.displayName}) ${question.question}", Modifier.weight(1f))
            Icon(
                painter = painterResource(
                    if (expanded) R.drawable.ic_arrow_drop_up
                    else R.drawable.ic_arrow_drop_down
                ),
                contentDescription = "Arrow drop ${if (expanded) "up" else "down"} on question card."
            )
        }
        if (expanded)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Answer(question.correctAnswer, isCorrect = true)
                question.incorrectAnswers.forEach {
                    Answer(it, false)
                }
            }
    }
}

@Composable
private fun Answer(answer: String, isCorrect: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.run {
                if (isCorrect) Check else Close
            },
            contentDescription = "Icon for ${if (isCorrect) "correct" else "incorrect"} answer."
        )
        Text(answer)
    }
}

@Preview
@Composable
private fun QuestionsPreview() {
    val mockQuestion = Question(
        question = "Question Statement",
        difficulty = Difficulty.EASY,
        categoryId = 0,
        correctAnswer = "Correct answer",
        incorrectAnswers = listOf("Incorrect 1", "Incorrect 2"),
    )
    RandomTriviaTheme {
        Questions(
            categoryName = "Category name",
            questions = listOf(
                mockQuestion,
                mockQuestion.copy(question = "abc", difficulty = Difficulty.MEDIUM, id = UUID.randomUUID()),
                mockQuestion.copy(question = "bcd", difficulty = Difficulty.EASY, id = UUID.randomUUID()),
                mockQuestion.copy(question = "efg", difficulty = Difficulty.HARD, id = UUID.randomUUID()),
            ),
            onExit = {},
            onAboutClick = {}
        )
    }
}