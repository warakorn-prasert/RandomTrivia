@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import com.korn.portfolio.randomtrivia.ui.common.CheckboxWithText
import com.korn.portfolio.randomtrivia.ui.common.FilterSortMenuBar
import com.korn.portfolio.randomtrivia.ui.common.RadioButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.SearchableTopBarWithBackButton
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.QuestionsViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.displayName
import java.util.UUID

private enum class QuestionFilter(
    val displayText: String,
    val function: (List<Question>) -> List<Question>
) {
    ALL("All", { it }),
    EASY("Easy", { all -> all.filter { it.difficulty == Difficulty.EASY } }),
    MEDIUM("Medium", { all -> all.filter { it.difficulty == Difficulty.MEDIUM } }),
    HARD("Hard", { all -> all.filter { it.difficulty == Difficulty.HARD } })
}

private enum class QuestionSort(
    val displayText: String,
    val function: (List<Question>) -> List<Question>
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

private fun List<Question>.process(
    filter: QuestionFilter,
    searchWord: String,
    sort: QuestionSort,
    reverseSort: Boolean
): List<Question> = this
    .let { filter.function(it) }
    .let { questions ->
        if (searchWord.isNotBlank()) questions.filter {
            it.question.lowercase().contains(searchWord.lowercase())
        }
        else questions
    }
    .let { sort.function(it) }
    .let { if (reverseSort) it.reversed() else it }

@Composable
fun Questions(
    categoryId: Int,
    onBack: () -> Unit,
    navToAboutScreen: () -> Unit,
    onShowSortMenuChange: (Boolean) -> Unit = {}
) {
    val viewModel: QuestionsViewModel = viewModel(factory = QuestionsViewModel.Factory(categoryId))
    Questions(
        categoryName = viewModel.categoryName,
        questions = viewModel.questions,
        onBack = onBack,
        navToAboutScreen = navToAboutScreen,
        onShowSortMenuChange = onShowSortMenuChange
    )
}

@Composable
private fun Questions(
    categoryName: String,
    questions: List<Question>,
    onBack: () -> Unit,
    navToAboutScreen: () -> Unit,
    onShowSortMenuChange: (Boolean) -> Unit = {}
) {
    var searchWord by remember { mutableStateOf("") }
    BackHandler(onBack = onBack)
    Scaffold(
        topBar = {
            SearchableTopBarWithBackButton(
                searchWord = searchWord,
                onChange = { searchWord = it },
                navToAboutScreen = navToAboutScreen,
                hint = "Search for questions",
                title = categoryName,
                onBackButtonClick = onBack
            )
        }
    ) { paddingValues ->
        Column(Modifier.padding(paddingValues)) {
            var filter by remember { mutableStateOf(QuestionFilter.ALL) }
            var sort by remember { mutableStateOf(QuestionSort.QUESTION_STATEMENT) }
            var reverseSort by remember { mutableStateOf(false) }

            val listState = rememberLazyListState()
            LaunchedEffect(searchWord, filter, sort, reverseSort) {
                listState.scrollToItem(0)
            }

            QuestionsFilterSortMenuBar(
                filter, { filter = it },
                sort, { sort = it },
                reverseSort, { reverseSort = it },
                onShowSortMenuChange = onShowSortMenuChange
            )
            val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
            val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            val bottomBarPadding = 80.dp
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 8.dp,
                    top = 16.dp,
                    end = 8.dp,
                    bottom = (imePadding - navBarPadding - bottomBarPadding + 16.dp).coerceAtLeast(16.dp)
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = questions.process(filter, searchWord, sort, reverseSort),
                    key = { it.id }
                ) {
                    QuestionCard(it)
                }
            }
        }
    }
}

@Composable
private fun QuestionsFilterSortMenuBar(
    filter: QuestionFilter,
    onFilterSelect: (QuestionFilter) -> Unit,
    sort: QuestionSort,
    onSortSelect: (QuestionSort) -> Unit,
    reverseSort: Boolean,
    onReverseSortChange: (Boolean) -> Unit,
    onShowSortMenuChange: (Boolean) -> Unit = {}
) {
    FilterSortMenuBar(
        selectedFilter = filter,
        filters = QuestionFilter.entries,
        onFilterSelect = onFilterSelect,
        filterToString = { it.displayText },
        sortBottomSheetContent = {
            Column(Modifier.height(IntrinsicSize.Min)) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sort By")
                    CheckboxWithText(
                        checked = reverseSort,
                        onCheckedChange = onReverseSortChange,
                        text = "Reversed"
                    )
                }
                QuestionSort.entries.forEach {
                    RadioButtonWithText(
                        selected = sort == it,
                        onClick = { onSortSelect(it) },
                        text = it.displayText
                    )
                }
            }
        },
        onShowSortMenuChange = onShowSortMenuChange
    )
}

@Composable
private fun QuestionCard(question: Question, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded },
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
                Answer(question.correctAnswer, correct = true)
                question.incorrectAnswers.forEach {
                    Answer(it, false)
                }
            }
    }
}

@Composable
private fun Answer(answer: String, correct: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.run {
                if (correct) Check else Close
            },
            contentDescription = "Icon for ${if (correct) "correct" else "incorrect"} answer."
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
            onBack = {},
            navToAboutScreen = {}
        )
    }
}