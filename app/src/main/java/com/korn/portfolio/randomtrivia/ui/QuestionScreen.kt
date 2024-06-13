@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.database.model.entity.Category
import com.korn.portfolio.database.model.CategoryWithQuestions
import com.korn.portfolio.database.model.Difficulty
import com.korn.portfolio.database.model.entity.Question
import com.korn.portfolio.randomtrivia.data.mockCategory1
import com.korn.portfolio.randomtrivia.data.mockCategoryEmpty
import com.korn.portfolio.randomtrivia.data.mockCategoryOverflowText
import com.korn.portfolio.randomtrivia.data.mockCategoryWithQuestions1
import com.korn.portfolio.randomtrivia.data.mockQuestion12
import java.util.UUID

private fun nonBlankListOf(vararg values: String): List<String> {
    return mutableListOf<String>().apply {
        values.forEach {
            if (it.isNotBlank()) add(it)
        }
    }
}

@Composable
fun QuestionScreen(paddingValues: PaddingValues) {
    val triviaViewModel: TriviaViewModel = viewModel(factory = TriviaViewModel.Factory)
    val categories by triviaViewModel.categoriesWithQuestions.collectAsState(emptyList())
    val uncategorizedQuestions by triviaViewModel.uncategorizedQuestions.collectAsState(emptyList())
    CategoryWithQuestionsCards(
        paddingValues, categories, uncategorizedQuestions,
        triviaViewModel::insertQuestions, triviaViewModel::updateQuestions,
        triviaViewModel::deleteQuestions, triviaViewModel::deleteByCategory
    )
}

@Composable
private fun CategoryWithQuestionsCards(
    paddingValues: PaddingValues,
    categories: List<CategoryWithQuestions>,
    uncategorizedQuestions: List<Question>,
    insertAction: (Question) -> Unit,
    updateAction: (Question) -> Unit,
    deleteAction: (Question) -> Unit,
    deleteByCategoryAction: (UUID?) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 12.dp)) {
        item {
            HorizontalDividerWithText(
                "With Categories (${categories.fold(0) { acc, it -> acc + it.questions.size  }})",
                Modifier.fillMaxWidth().padding(vertical = 12.dp)
            )
        }
        items(categories, key = { it.category.id }) {
            CategoryWithQuestionsCard(
                Modifier.padding(8.dp), it,
                insertAction, updateAction, deleteAction, deleteByCategoryAction
            )
        }
        item {
            Column {
                HorizontalDividerWithText(
                    "Uncategorized (${uncategorizedQuestions.size})",
                    Modifier.fillMaxWidth().padding(vertical = 12.dp)
                )
                if (uncategorizedQuestions.isNotEmpty()) {
                    IconButton({ deleteByCategoryAction(null) }) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
            }
        }
        items(uncategorizedQuestions.size, key = { uncategorizedQuestions[it].id }) {
            UncategorizedQuestion(it + 1, uncategorizedQuestions[it], Modifier.padding(8.dp))
        }
    }
}

@Composable
private fun CategoryWithQuestionsCard(
    modifier: Modifier = Modifier,
    category: CategoryWithQuestions,
    insertAction: (Question) -> Unit,
    updateAction: (Question) -> Unit,
    deleteAction: (Question) -> Unit,
    deleteByCategoryAction: (UUID) -> Unit,
    defaultExpanded: Boolean = false
) {
    Column(modifier.animateContentSize().width(IntrinsicSize.Max)) {
        val showInsertDialog = remember { mutableStateOf(false) }
        if (showInsertDialog.value) {
            QuestionInsertDialog(showInsertDialog, category.category, insertAction)
        }
        var expanded by remember { mutableStateOf(defaultExpanded) }
        Card(Modifier.clickable { expanded = !expanded }) {
            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                ToggleArrowIcon(expanded)
                Text(category.category.name, Modifier.weight(1f), fontWeight = FontWeight.Bold)
                IconButton({ showInsertDialog.value = true }) {
                    Icon(Icons.Default.Add, null)
                }
                IconButton({
                    deleteByCategoryAction(category.category.id)
                }) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }
        if (expanded) {
            if (category.questions.isEmpty()) {
                Text("Empty", Modifier.align(Alignment.CenterHorizontally),
                    fontStyle = FontStyle.Italic
                )
            }
            category.questions.forEachIndexed { idx, question ->
                val showUpdateDialog = remember { mutableStateOf(false) }
                if (showUpdateDialog.value) {
                    QuestionUpdateDialog(showUpdateDialog, category.category, question, updateAction)
                }
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Row {
                    Column {
                        IconButton({ deleteAction(question) }) {
                            Icon(Icons.Default.Delete, null)
                        }
                        IconButton({ showUpdateDialog.value = true }) {
                            Icon(Icons.Default.Edit, null)
                        }
                    }
                    Column {
                        Text("${idx + 1}. ${question.question}")
                        Text("(${question.difficulty})")
                        QuestionChoices(question.correctAnswer, question.incorrectAnswers)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryWithQuestionsCardPreview() {
    Column {
        Row {
            CategoryWithQuestionsCard(
                category = mockCategoryWithQuestions1,
                insertAction = {},
                updateAction = {},
                deleteAction = {},
                deleteByCategoryAction = {}
            )
            Spacer(Modifier.width(4.dp))
            CategoryWithQuestionsCard(
                category = mockCategoryWithQuestions1,
                insertAction = {},
                updateAction = {},
                deleteAction = {},
                deleteByCategoryAction = {},
                defaultExpanded = true
            )
        }
        CategoryWithQuestionsCard(
            category = mockCategoryEmpty,
            insertAction = {},
            updateAction = {},
            deleteAction = {},
            deleteByCategoryAction = {},
            defaultExpanded = true
        )
        CategoryWithQuestionsCard(
            category = mockCategoryOverflowText,
            insertAction = {},
            updateAction = {},
            deleteAction = {},
            deleteByCategoryAction = {}
        )
    }
}

@Composable
private fun UncategorizedQuestion(idx: Int, question: Question, modifier: Modifier = Modifier) {
    Column(modifier) {
        var expanded by remember { mutableStateOf(false) }
        Column(Modifier.animateContentSize().clickable { expanded = !expanded }) {
            Text("$idx. (${question.difficulty}) ${question.question}")
            if (expanded) {
                QuestionChoices(question.correctAnswer, question.incorrectAnswers)
            }
        }
    }
}

@Composable
private fun QuestionChoices(correctAnswer: String, incorrectAnswers: List<String>) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Done, null)
            Text(correctAnswer)
        }
        incorrectAnswers.forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Close, null)
                Text(it)
            }
        }
    }
}

@Composable
private fun QuestionInsertDialog(
    show: MutableState<Boolean>,
    category: Category,
    insertAction: (Question) -> Unit
) {
    Dialog(onDismissRequest = { show.value = false }) {
        Card {
            Column(Modifier.padding(12.dp)) {
                Text("New question", fontWeight = FontWeight.Bold)
                Text("(${category.name})")
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                val question = remember { mutableStateOf("") }
                val difficulty = remember { mutableStateOf(Difficulty.EASY) }
                val correctAnswer = remember { mutableStateOf("") }
                val incorrectAnswer1 = remember { mutableStateOf("") }
                val incorrectAnswer2 = remember { mutableStateOf("") }
                val incorrectAnswer3 = remember { mutableStateOf("") }
                val incorrectAnswer4 = remember { mutableStateOf("") }
                QuestionEditor(question, difficulty, correctAnswer,
                    incorrectAnswer1, incorrectAnswer2, incorrectAnswer3, incorrectAnswer4
                )
                Row {
                    IconButton(
                        onClick = { show.value = false },
                        content = { Icon(Icons.Default.Close, null) }
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            insertAction(
                                Question(
                                    question.value,
                                    difficulty.value,
                                    category.id,
                                    correctAnswer.value,
                                    nonBlankListOf(
                                        incorrectAnswer1.value, incorrectAnswer2.value,
                                        incorrectAnswer3.value, incorrectAnswer4.value
                                    )
                                )
                            )
                            show.value = false
                        },
                        content = { Icon(Icons.Default.Done, null) }
                    )
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
private fun QuestionInsertDialogPreview() {
    QuestionInsertDialog(mutableStateOf(true), mockCategory1) {}
}

@Composable
private fun QuestionUpdateDialog(
    show: MutableState<Boolean>,
    category: Category,
    question: Question,
    updateAction: (Question) -> Unit
) {
    Dialog(onDismissRequest = { show.value = false }) {
        Card {
            Column(Modifier.padding(12.dp)) {
                Text("Update question", fontWeight = FontWeight.Bold)
                Text("(${category.name})")
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                val questionText = remember { mutableStateOf(question.question) }
                val difficulty = remember { mutableStateOf(question.difficulty) }
                val correctAnswer = remember { mutableStateOf(question.correctAnswer) }
                val incorrectAnswer1 = remember { mutableStateOf(
                    if (question.incorrectAnswers.isNotEmpty())
                        question.incorrectAnswers[0]
                    else ""
                ) }
                val incorrectAnswer2 = remember { mutableStateOf(
                    if (question.incorrectAnswers.size > 1)
                        question.incorrectAnswers[1]
                    else ""
                ) }
                val incorrectAnswer3 = remember { mutableStateOf(
                    if (question.incorrectAnswers.size > 2)
                        question.incorrectAnswers[2]
                    else ""
                ) }
                val incorrectAnswer4 = remember { mutableStateOf(
                    if (question.incorrectAnswers.size > 3)
                        question.incorrectAnswers[3]
                    else ""
                ) }
                QuestionEditor(questionText, difficulty, correctAnswer,
                    incorrectAnswer1, incorrectAnswer2, incorrectAnswer3, incorrectAnswer4
                )
                Row {
                    IconButton(
                        onClick = { show.value = false },
                        content = { Icon(Icons.Default.Close, null) }
                    )
                    Spacer(Modifier.weight(1f))
                    var uncategorized by remember { mutableStateOf(false) }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Checkbox(uncategorized, { uncategorized = it })
                        Text("Uncategorize")
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            updateAction(
                                Question(
                                    questionText.value,
                                    difficulty.value,
                                    if (uncategorized) null else category.id,
                                    correctAnswer.value,
                                    nonBlankListOf(
                                        incorrectAnswer1.value,
                                        incorrectAnswer2.value,
                                        incorrectAnswer3.value,
                                        incorrectAnswer4.value
                                    ),
                                    question.id
                                )
                            )
                            show.value = false
                        },
                        content = { Icon(Icons.Default.Done, null) }
                    )
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
private fun QuestionUpdateDialogPreview() {
    QuestionUpdateDialog(mutableStateOf(true), mockCategory1, mockQuestion12) {}
}

@Composable
private fun QuestionEditor(
    question: MutableState<String>,
    difficulty: MutableState<Difficulty>,
    correctAnswer: MutableState<String>,
    incorrectAnswer1: MutableState<String>,
    incorrectAnswer2: MutableState<String>,
    incorrectAnswer3: MutableState<String>,
    incorrectAnswer4: MutableState<String>,
) {
    Column {
        PlainTextField(
            question.value, { question.value = it },
            leadingIcon = { Text("Question") }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Difficulty", Modifier.padding(end = 4.dp))
            CustomDropdown(
                value = difficulty.value,
                onValueChange = { difficulty.value = it },
                options = Difficulty.entries,
                toString = { it.name }
            )
        }
        PlainTextField(
            correctAnswer.value, { correctAnswer.value = it },
            leadingIcon = { Text("Correct Ans") }
        )
        PlainTextField(
            incorrectAnswer1.value, { incorrectAnswer1.value = it },
            leadingIcon = { Text("Incorrect Ans 1") }
        )
        PlainTextField(
            incorrectAnswer2.value, { incorrectAnswer2.value = it },
            leadingIcon = { Text("Incorrect Ans 2") }
        )
        PlainTextField(
            incorrectAnswer3.value, { incorrectAnswer3.value = it },
            leadingIcon = { Text("Incorrect Ans 3") }
        )
        PlainTextField(
            incorrectAnswer4.value, { incorrectAnswer4.value = it },
            leadingIcon = { Text("Incorrect Ans 4") }
        )
    }
}