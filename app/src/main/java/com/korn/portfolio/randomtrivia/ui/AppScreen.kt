@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.network.model.Category
import com.korn.portfolio.randomtrivia.network.model.Difficulty
import com.korn.portfolio.randomtrivia.network.model.Question
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.network.model.Type
import kotlinx.coroutines.launch

private val headerPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
private val contentPadding = PaddingValues(start = 28.dp, end = 28.dp, bottom = 12.dp)

@Composable
fun AppScreen() {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    fun showSnackbar(text: String) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = text,
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
        }
    }
    val triviaViewModel: TriviaViewModel = viewModel(factory = TriviaViewModel.Factory)
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            val centerContentModifier = Modifier.padding(contentPadding).align(Alignment.CenterHorizontally)
            val categories = triviaViewModel.categories
            val questionCounts = triviaViewModel.questionCounts
            LaunchedEffect(categories.uiState) {
                if (categories.uiState is UiState.Error) {
                    showSnackbar("Couldn't load categories :(")
                }
            }
            LaunchedEffect(questionCounts.uiState) {
                val uiState = questionCounts.uiState
                if (uiState is UiState.Error) {
                    showSnackbar("Couldn't load category detail (${uiState.error.message})")
                }
            }
            HeaderText("${categories.data.size} Categories, ${categories.data.values.sum()} Question(s)") {
                IconButton(
                    onClick = triviaViewModel::getCategories,
                    enabled = categories.uiState !is UiState.Loading,
                    content = { Icon(Icons.Default.Refresh, null) }
                )
            }
            if (categories.uiState is UiState.Loading) {
                CircularProgressIndicator(centerContentModifier)
            } else if (categories.data.isNotEmpty()) {
                LazyHorizontalStaggeredGrid(
                    rows = StaggeredGridCells.Adaptive(minSize = 64.dp),
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalItemSpacing = 4.dp,
                    contentPadding = contentPadding
                ) {
                    items(categories.data.toList(), key = { it.first.id }) { (category, total) ->
                        val showDialog = remember { mutableStateOf(false) }
                        Card(Modifier.fillMaxHeight().wrapContentWidth().clickable {
                            if (!questionCounts.data.containsKey(category)) {
                                triviaViewModel.getQuestionCount(category.id)
                            }
                            showDialog.value = true
                        }) {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(category.id.toString())
                                VerticalDivider(Modifier.padding(horizontal = 8.dp))
                                Column {
                                    Text(category.name)
                                    Text("$total question(s)")
                                }
                            }
                        }
                        if (showDialog.value) {
                            QuestionCountDialog(
                                show = showDialog,
                                category = category,
                                uiState = questionCounts.uiState,
                                questionCount = questionCounts.data[category],
                                refreshAction = { triviaViewModel.getQuestionCount(category.id) }
                            )
                        }
                    }
                }
            }

            val token = triviaViewModel.token
            LaunchedEffect(token.uiState) {
                val uiState = token.uiState
                if (uiState is UiState.Error) {
                    showSnackbar("Couldn't renew token (${uiState.error.message})")
                }
            }
            val tokenShort = token.data?.substring(0, 5)?.let { "$it..." }
            val enableTokenAction = token.uiState !is UiState.Loading
                    && triviaViewModel.questions.uiState !is UiState.Loading
            HeaderText("Token ($tokenShort)") {
                IconButton(
                    onClick = triviaViewModel::clearToken,
                    enabled = enableTokenAction && token.data != null,
                    content = { Icon(Icons.Default.Delete, null) }
                )
                IconButton(
                    onClick = triviaViewModel::getToken,
                    enabled = enableTokenAction,
                    content = { Icon(Icons.Default.Refresh, null) }
                )
            }

            val questions = triviaViewModel.questions
            LaunchedEffect(questions.uiState) {
                val uiState = questions.uiState
                if (uiState is UiState.Error) {
                    showSnackbar("Couldn't get questions (${uiState.error.message})")
                }
            }
            HeaderText("${questions.data.size} Question(s)") {
                IconButton(
                    onClick = triviaViewModel::deleteAllQuestions,
                    enabled = questions.data.isNotEmpty(),
                    content = { Icon(Icons.Default.Delete, null) }
                )
                val showDialog = remember { mutableStateOf(false) }
                IconButton(
                onClick = { showDialog.value = true },
                    enabled = questions.uiState !is UiState.Loading
                            && categories.uiState is UiState.Success
                            && categories.data.isNotEmpty(),
                    content = { Icon(Icons.Default.Add, null) }
                )
                if (showDialog.value) {
                    InsertQuestionDialog(
                        show = showDialog,
                        categories = categories.data.keys,
                        questionCounts = questionCounts,
                        getQuestionCount = triviaViewModel::getQuestionCount,
                        getQuestions = triviaViewModel::getQuestions
                    )
                }
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(
                    items = triviaViewModel.questions.data.groupBy { it.category }.toList(),
                    key = { it.first }
                ) { (category, catQuestions) ->
                    Box(Modifier.padding(contentPadding), contentAlignment = Alignment.Center) {
                        HorizontalDivider()
                        Text(
                            text = "$category (${catQuestions.size})",
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .padding(horizontal = 8.dp)
                        )
                    }
                    LazyRow(
                        contentPadding = contentPadding,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(catQuestions, key = { it.question }) { question ->
                            val showDialog = remember { mutableStateOf(false) }
                            Card(Modifier.clickable { showDialog.value = true }) {
                                Text(question.question, Modifier.padding(8.dp))
                            }
                            if (showDialog.value) {
                                QuestionDialog(showDialog, question)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderText(
    text: String,
    trailingIcon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.padding(headerPadding).height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text)
        trailingIcon()
    }
}

@Composable
private fun QuestionCountDialog(
    show: MutableState<Boolean>,
    category: Category,
    uiState: UiState,
    questionCount: QuestionCount?,
    refreshAction: () -> Unit
) {
    Dialog(onDismissRequest = { show.value = false }) {
        Card(border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)) {
            Column(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${category.id} | ${category.name}", Modifier.padding(bottom = 12.dp))
                if (uiState is UiState.Error && questionCount != null) {
                    QuestionCountDetail(questionCount)
                } else if (uiState is UiState.Loading) {
                    CircularProgressIndicator(Modifier.padding(bottom = 20.dp))
                } else if (uiState is UiState.Success && questionCount != null) {
                    QuestionCountDetail(questionCount)
                }
                if (uiState is UiState.Error || uiState is UiState.Success) {
                    IconButton(refreshAction) { Icon(Icons.Default.Refresh, null) }
                }
            }
        }
    }
}

@Composable
private fun QuestionCountDetail(questionCount: QuestionCount) {
    Column {
        Text("Total : ${questionCount.total} question(s)")
        Text("Easy : ${questionCount.easy} question(s)")
        Text("Medium : ${questionCount.medium} question(s)")
        Text("Hard : ${questionCount.hard} question(s)")
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
private fun QuestionCountDialogPreview() {
    QuestionCountDialog(
        show = mutableStateOf(true),
        category = Category(id = 0, name = "Category name"),
        uiState = UiState.Success,
        questionCount = QuestionCount(total = 10, easy = 5, medium = 3, hard = 2),
        refreshAction = {}
    )
}

@Composable
private fun QuestionDialog(show: MutableState<Boolean>, question: Question) {
    Dialog(onDismissRequest = { show.value = false }) {
        Card(border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)) {
            Column(Modifier.padding(20.dp)) {
                Text(question.question)
                Text("Type : ${question.type}")
                Text("Difficulty : ${question.difficulty}")
                Text("Category : ${question.category}")
                Text("Correct answer : ${question.correctAnswer}")
                question.incorrectAnswers.forEach {
                    Text("Incorrect answer : $it")
                }
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
private fun QuestionDialogPreview() {
    QuestionDialog(
        show = mutableStateOf(true),
        question = Question(
            question = "What is 1 + 1?",
            difficulty = Difficulty.EASY,
            category = "Math",
            type = "multiple",
            correctAnswer = "2",
            incorrectAnswers = listOf("1", "3", "4")
        )
    )
}

@Composable
private fun InsertQuestionDialog(
    show: MutableState<Boolean>,
    categories: Set<Category>,
    questionCounts: ViewModelData<Map<Category, QuestionCount>>,
    getQuestionCount: (categoryId: Int) -> Unit,
    getQuestions: (amount: Int, categoryId: Int?, difficulty: Difficulty?, type: Type?) -> Unit
) {
    val randomCategory = Category(id = -1, name = "random")
    var category by remember { mutableStateOf(randomCategory) }
    var difficulty by remember { mutableStateOf(Difficulty.RANDOM) }
    var type by remember { mutableStateOf(Type.RANDOM) }
    var amount by remember { mutableIntStateOf(1) }

    var maxAmount by remember { mutableIntStateOf(50) }
    val difficulties =
        if (category == randomCategory) {
            Difficulty.entries
        } else {
            mutableSetOf(Difficulty.RANDOM).apply {
                for (questionCount in questionCounts.data.values) {
                    with(questionCount) {
                        if (easy > 0) add(Difficulty.EASY)
                        if (medium > 0) add(Difficulty.MEDIUM)
                        if (hard > 0) add(Difficulty.HARD)
                    }
                    if (size == 4) {
                        break
                    }
                }
            }
        }

    LaunchedEffect(category) {
        difficulty = Difficulty.RANDOM
        if (category != randomCategory && !questionCounts.data.containsKey(category)) {
            getQuestionCount(category.id)
        }
    }
    LaunchedEffect(category, difficulty) {
        maxAmount =
            if (category == randomCategory || !questionCounts.data.containsKey(category))
                50
            else {
                with(questionCounts.data[category]!!) {
                    when (difficulty) {
                        Difficulty.EASY -> easy
                        Difficulty.MEDIUM -> medium
                        Difficulty.HARD -> hard
                        Difficulty.RANDOM -> easy + medium + hard
                    }.coerceAtMost(50)
                }
            }
        amount = amount.coerceAtMost(maxAmount)
    }

    Dialog(onDismissRequest = { show.value = false }) {
        Card(border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)) {
            Column(Modifier.padding(20.dp), Arrangement.spacedBy(4.dp)) {
                CustomDropdown(
                    value = category,
                    onValueChange = { category = it },
                    options = categories + randomCategory,
                    toString = { "${it.id} ${it.name}" }
                )
                CustomDropdown(
                    value = type,
                    onValueChange = { type = it },
                    options = Type.entries,
                    toString = { it.name.lowercase() }
                )
                if (category == randomCategory || questionCounts.data.containsKey(category)) {
                    CustomDropdown(
                        value = difficulty,
                        onValueChange = { difficulty = it },
                        options = difficulties,
                        toString = { it.name.lowercase() }
                    )
                    PlainTextField(
                        value = amount.toString(),
                        onValueChange = {
                            amount = it.toIntOrNull()?.coerceAtMost(maxAmount) ?: 1
                        },
                        numberOnly = true,
                        leadingIcon = { Text("Amount (max $maxAmount)") }
                    )
                    IconButton(
                        onClick = {
                            getQuestions(
                                amount,
                                category.takeUnless { it == randomCategory }?.id,
                                difficulty.takeUnless { it == Difficulty.RANDOM },
                                type.takeUnless { it == Type.RANDOM }
                            )
                            show.value = false
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        content = { Icon(Icons.Default.Add, null) }
                    )
                } else if (category != randomCategory && questionCounts.uiState is UiState.Loading) {
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                } else if (category != randomCategory && questionCounts.uiState is UiState.Error) {
                    Text(
                        text = "Couldn't load category detail.",
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    IconButton(
                        onClick = { getQuestionCount(category.id) },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        content = { Icon(Icons.Default.Refresh, null) }
                    )
                }
            }
        }
    }
}

@Composable
fun <T> CustomDropdown(
    value: T,
    onValueChange: (T) -> Unit,
    options: Collection<T>,
    modifier: Modifier = Modifier,
    toString: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier.wrapContentSize().clickable { expanded = !expanded },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(Modifier.padding(4.dp).padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(toString(value))
            Icon(if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            content =  {
                options.forEach {
                    DropdownMenuItem(
                        text = { Text(toString(it)) },
                        onClick = {
                            onValueChange(it)
                            expanded = false
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun PlainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    numberOnly: Boolean = false,
    roundness: Dp = 8.dp,
    leadingIcon: @Composable () -> Unit = {}
) {
    Row(modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        leadingIcon()
        Spacer(Modifier.width(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = if (numberOnly) KeyboardType.Number else KeyboardType.Text
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(Modifier
                    .height(IntrinsicSize.Min)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(roundness))
                    .clip(RoundedCornerShape(roundness))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    innerTextField()
                }
            }
        )
    }
}