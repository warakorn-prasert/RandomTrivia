@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.ui.model.NetworkUiState
import kotlinx.coroutines.launch

private val List<Pair<Category, QuestionCount>>.summary: String
    get() {
        var (total, easy, medium, hard) = listOf(0, 0, 0, 0)
        forEach { (_, questionCount) ->
            total += questionCount.total
            easy += questionCount.easy
            medium += questionCount.medium
            hard += questionCount.hard
        }
        return """
            Categories: $size
            Questions: $total (Easy: $easy, Medium: $medium, Hard : $hard)
        """.trimIndent()
    }

@Composable
fun CategoryScreen(snackbarHostState: SnackbarHostState) {
    val screenScope = rememberCoroutineScope()
    fun popMessage(message: String) {
        screenScope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                withDismissAction = false,
                duration = SnackbarDuration.Short
            )
        }
    }

    val categoryViewModel: CategoryViewModel = viewModel(factory = CategoryViewModel.Factory)
    Column {
        val remoteCategories by categoryViewModel.remoteCategories.observeAsState(emptyList())
        val uiState: MutableState<NetworkUiState> = remember { mutableStateOf(NetworkUiState.Success) }
        LaunchedEffect(uiState) {
            val s = uiState.value
            if (s is NetworkUiState.Error) popMessage("Error (${s.error.message})")
        }
        SectionTitle("Remote") {
            IconButton(
                onClick = { popMessage(remoteCategories.summary) },
                content = { Icon(Icons.Default.Info, null) }
            )
            IconButton(
                onClick = { categoryViewModel.fetchCategory(uiState) },
                enabled = uiState.value !is NetworkUiState.Loading,
                content = { Icon(Icons.Default.Refresh, null) }
            )
        }
        if (uiState.value !is NetworkUiState.Loading) {
            RemoteCategoryCards(
                modifier = if (remoteCategories.isNotEmpty()) Modifier.weight(1f) else Modifier,
                categories = remoteCategories,
                fetchQuestionCount = categoryViewModel::fetchQuestionCount
            )
        } else {
            CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
        }

        val localCategories by categoryViewModel.localCategories.collectAsState(emptyList())
        SectionTitle("Local") {
            IconButton(
                onClick = { popMessage(localCategories.summary) },
                content = { Icon(Icons.Default.Info, null) }
            )
            IconButton(
                onClick = categoryViewModel::deleteAllLocalCategories,
                enabled = localCategories.isNotEmpty(),
                content = { Icon(Icons.Default.Delete, null) }
            )
        }
        LocalCategoryCards(
            modifier = if (localCategories.isNotEmpty()) Modifier.weight(1f) else Modifier,
            categories = localCategories,
            deleteLocalCategory = categoryViewModel::deleteLocalCategories
        )
    }
}

@Composable
private fun SectionTitle(
    text: String,
    trailingIcon: @Composable RowScope.() -> Unit = {}
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 16.dp),
            fontWeight = FontWeight.ExtraBold
        )
        trailingIcon()
    }
}

@Composable
private fun RemoteCategoryCards(
    modifier: Modifier = Modifier,
    categories: List<Pair<Category, QuestionCount>>,
    fetchQuestionCount: (Int, MutableState<NetworkUiState>) -> Unit
) {
    CardStaggeredGrid(modifier) {
        items(categories, key = { it.first.id }) { (category, questionCount) ->
            val showDialog = remember { mutableStateOf(false) }
            if (showDialog.value) {
                RemoteQuestionCountDialog(
                    show = showDialog,
                    category = category,
                    questionCount = questionCount,
                    fetchQuestionCount = fetchQuestionCount
                )
            }
            CategoryCard(
                category = category,
                onClick = { showDialog.value = true }
            )
        }
    }
}

@Composable
private fun RemoteQuestionCountDialog(
    show: MutableState<Boolean>,
    category: Category,
    questionCount: QuestionCount,
    fetchQuestionCount: (Int, MutableState<NetworkUiState>) -> Unit
) {
    val uiState: MutableState<NetworkUiState> = remember {
        mutableStateOf(
            if (questionCount.invalid) NetworkUiState.Loading
            else NetworkUiState.Success
        )
    }
    LaunchedEffect(Unit) {
        if (questionCount.invalid) {
            fetchQuestionCount(category.id, uiState)
        }
    }
    Dialog(onDismissRequest = { show.value = false }) {
        DialogCard(
            title = { Text("${category.id} ${category.name}") }
        ) {
            Text("- Total : ${questionCount.total}")
            Text("- Easy : ${questionCount.easy}")
            Text("- Medium : ${questionCount.medium}")
            Text("- Hard : ${questionCount.hard}")

            if (uiState.value !is NetworkUiState.Loading) {
                IconButton(
                    onClick = { fetchQuestionCount(category.id, uiState) },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Refresh, null)
                }
            }
            when (val s = uiState.value) {
                is NetworkUiState.Error ->
                    Text(
                        text = "Error (${s.error.message})",
                        modifier = Modifier.align(Alignment.End),
                        fontStyle = FontStyle.Italic
                    )
                NetworkUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
                NetworkUiState.Success -> {}
            }
        }
    }
}

@Composable
private fun LocalCategoryCards(
    modifier: Modifier = Modifier,
    categories: List<Pair<Category, QuestionCount>>,
    deleteLocalCategory: (Int) -> Unit
) {
    CardStaggeredGrid(modifier) {
        items(categories, key = { it.first.id }) { (category, questionCount) ->
            val showDialog = remember { mutableStateOf(false) }
            if (showDialog.value) {
                LocalQuestionCountDialog(
                    show = showDialog,
                    category = category,
                    questionCount = questionCount,
                    deleteLocalCategory = deleteLocalCategory
                )
            }
            CategoryCard(
                category = category,
                onClick = { showDialog.value = true }
            )
        }
    }
}

@Composable
private fun LocalQuestionCountDialog(
    show: MutableState<Boolean>,
    category: Category,
    questionCount: QuestionCount,
    deleteLocalCategory: (Int) -> Unit
) {
    Dialog(onDismissRequest = { show.value = false }) {
        DialogCard(
            title = { Text("${category.id} ${category.name}") }
        ) {
            Text("- Total : ${questionCount.total}")
            Text("- Easy : ${questionCount.easy}")
            Text("- Medium : ${questionCount.medium}")
            Text("- Hard : ${questionCount.hard}")
            IconButton(
                onClick = {
                    deleteLocalCategory(category.id)
                    show.value = false
                },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Icon(Icons.Default.Delete, null)
            }
        }
    }
}

@Composable
private fun CardStaggeredGrid(
    modifier: Modifier = Modifier,
    content: LazyStaggeredGridScope.() -> Unit
) {
    LazyHorizontalStaggeredGrid(
        rows = StaggeredGridCells.Adaptive(minSize = 56.dp),
        modifier = modifier.padding(bottom = 16.dp),
        contentPadding = PaddingValues(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalItemSpacing = 4.dp
    ) {
        content()
    }
}

@Composable
private fun CategoryCard(category: Category, onClick: () -> Unit) {
    Card(Modifier.clickable { onClick() }) {
        Column(Modifier.fillMaxSize(), Arrangement.Center) {
            Text(
                text = "${category.id} ${category.name}",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
            )
        }
    }
}

@Composable
private fun DialogCard(
    title: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)) {
        Column(Modifier
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .width(IntrinsicSize.Max)
        ) {
            title()
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
            content()
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
private fun RemoteQuestionCountDialogPreview() {
    RemoteQuestionCountDialog(
        show = mutableStateOf(true),
        category = Category(name = "Category Name", id = 9),
        questionCount = QuestionCount(10, 5, 3, 2),
        fetchQuestionCount = { _, _ -> }
    )
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
private fun LocalQuestionCountDialogPreview() {
    LocalQuestionCountDialog(
        show = mutableStateOf(true),
        category = Category(name = "Category Name", id = 9),
        questionCount = QuestionCount(10, 5, 3, 2),
        deleteLocalCategory = {}
    )
}