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
import com.korn.portfolio.randomtrivia.model.Category
import com.korn.portfolio.randomtrivia.model.CategoryWithQuestions
import com.korn.portfolio.randomtrivia.model.Difficulty
import com.korn.portfolio.randomtrivia.model.Question

fun nonBlankListOf(vararg values: String): List<String> {
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
    CategoryWithQuestionsCards(paddingValues, categories,
        triviaViewModel::insertQuestions, triviaViewModel::updateQuestions,
        triviaViewModel::deleteQuestions, triviaViewModel::deleteByCategory
    )
}

@Composable
private fun CategoryWithQuestionsCards(
    paddingValues: PaddingValues,
    categories: List<CategoryWithQuestions>,
    insertAction: (Question) -> Unit,
    updateAction: (Question) -> Unit,
    deleteAction: (Question) -> Unit,
    deleteAllAction: (Int) -> Unit,
) {
    LazyColumn(Modifier.fillMaxSize().padding(paddingValues)) {
        items(categories, key = { it.category.id }) {
            CategoryWithQuestionsCard(Modifier.padding(8.dp), it, insertAction, updateAction, deleteAction, deleteAllAction)
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
    deleteAllAction: (Int) -> Unit,
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
                IconButton({ deleteAllAction(category.category.id) }) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        }
        if (expanded) {
            if (category.questions.isEmpty()) {
                Text("Empty", Modifier.align(Alignment.CenterHorizontally), fontStyle = FontStyle.Italic)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Done, null)
                            Text(question.correctAnswer)
                        }
                        question.incorrectAnswers.forEach {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Close, null)
                                Text(it)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryWithQuestionsCardPreview() {
    val category = CategoryWithQuestions(
        Category("Name", 1, 2, 3, false),
        listOf(
            Question("Question1", Difficulty.EASY, 0, "C", listOf("I1", "I2", "I3", "I4")),
            Question("Question2", Difficulty.MEDIUM, 0, "C", listOf("I1", "I2", "I3", "I4")),
        )
    )
    Column {
        Row {
            CategoryWithQuestionsCard(
                category = category,
                insertAction = {},
                updateAction = {},
                deleteAction = {},
                deleteAllAction = {}
            )
            Spacer(Modifier.width(4.dp))
            CategoryWithQuestionsCard(
                category = category,
                insertAction = {},
                updateAction = {},
                deleteAction = {},
                deleteAllAction = {},
                defaultExpanded = true
            )
        }
        CategoryWithQuestionsCard(
            category = category.copy(category = category.category.copy(name = "Empty"), questions = emptyList()),
            insertAction = {},
            updateAction = {},
            deleteAction = {},
            deleteAllAction = {},
            defaultExpanded = true
        )
        val overflowText = "Overflow................................................................"
        CategoryWithQuestionsCard(
            category = category.copy(category = category.category.copy(name = overflowText)),
            insertAction = {},
            updateAction = {},
            deleteAction = {},
            deleteAllAction = {}
        )
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
                QuestionEditor(question, difficulty, correctAnswer, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3, incorrectAnswer4)
                Row {
                    IconButton(
                        onClick = { show.value = false },
                        content = { Icon(Icons.Default.Close, null) }
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            insertAction(Question(question.value, difficulty.value, category.id, correctAnswer.value, nonBlankListOf(incorrectAnswer1.value, incorrectAnswer2.value, incorrectAnswer3.value, incorrectAnswer4.value)))
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
    val category = Category("Category name", 1, 2, 3, false)
    QuestionInsertDialog(mutableStateOf(true), category, {})
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
                QuestionEditor(questionText, difficulty, correctAnswer, incorrectAnswer1, incorrectAnswer2, incorrectAnswer3, incorrectAnswer4)
                Row {
                    IconButton(
                        onClick = { show.value = false },
                        content = { Icon(Icons.Default.Close, null) }
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            updateAction(Question(questionText.value, difficulty.value, category.id, correctAnswer.value, nonBlankListOf(incorrectAnswer1.value, incorrectAnswer2.value, incorrectAnswer3.value, incorrectAnswer4.value), question.id))
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
    val category = Category("Category name", 1, 2, 3, false)
    val question  =Question("Question1", Difficulty.EASY, 0, "C", listOf("I1", "I2", "I3", "I4"))

    QuestionUpdateDialog(mutableStateOf(true), category, question, {})
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
                selection = difficulty,
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