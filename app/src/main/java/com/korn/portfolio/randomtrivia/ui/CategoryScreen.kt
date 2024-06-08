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
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.model.Category

private fun List<Category>.filterByTitle(filterWord: String): List<Category> {
    return filter { it.name.lowercase().contains(filterWord.lowercase()) }
}

@Composable
fun CategoryScreen(paddingValues: PaddingValues) {
    val triviaViewModel: TriviaViewModel = viewModel(factory = TriviaViewModel.Factory)
    val categories by triviaViewModel.categories.collectAsState(emptyList())
    val filterWord = remember { mutableStateOf("") }
    Scaffold(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        topBar = {
            CategoryTopAppBar(filterWord,
                triviaViewModel::insertCategories, triviaViewModel::deleteAllCategories
            )
        },
        content = { contentPaddingValues ->
            CategoryCards(contentPaddingValues, categories, filterWord,
                triviaViewModel::updateCategories, triviaViewModel:: deleteCategories
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTopAppBar(
    filterWord: MutableState<String>,
    insertAction: (Category) -> Unit,
    deleteAllAction: () -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    if (expanded.value) {
        CategoryInsertDialog(expanded, insertAction)
    }
    TopAppBar(
        title = {
            PlainTextField(filterWord.value, { filterWord.value = it }, roundness = 16.dp)
        },
        navigationIcon = { Text("Filter") },
        actions = {
            IconButton({ expanded.value = true }) { Icon(Icons.Default.Add, null) }
            IconButton(deleteAllAction) { Icon(Icons.Default.Delete, null) }
        }
    )
}

@SuppressLint("UnrememberedMutableState")
@Preview
@Composable
private fun CategoryTopAppBarPreview() {
    Column {
        CategoryTopAppBar(mutableStateOf(""), {}, {})
        Spacer(Modifier.height(4.dp))
        CategoryTopAppBar(mutableStateOf("Not empty"), {}, {})
    }
}

@Composable
private fun CategoryCards(
    paddingValues: PaddingValues,
    categories: List<Category>,
    filterWord: MutableState<String>,
    updateAction: (Category) -> Unit,
    deleteAction: (Category) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(paddingValues)) {
        items(categories.filterByTitle(filterWord.value), key = { it.id }) {
            val expanded = remember { mutableStateOf(false) }
            if (expanded.value) {
                CategoryUpdateDialog(expanded, it, updateAction)
            }
            CategoryCard(Modifier.padding(8.dp), it,
                updateAction = { expanded.value = true },
                deleteAction = deleteAction
            )
        }
    }
}

@Composable
private fun CategoryCard(
    modifier: Modifier = Modifier,
    category: Category,
    defaultExpanded: Boolean = false,
    updateAction: (Category) -> Unit,
    deleteAction: (Category) -> Unit
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }
    val title = category.name
    val hint = with(category) { "${easy + medium + hard} questions" }
    val bullets = with(category) { listOf(
        "id : $id", "$easy Easy", "$medium Medium", "$hard Hard",
        "Downloadable : $downloadable"
    ) }
    Card(modifier
        .width(IntrinsicSize.Max)
        .animateContentSize()
        .clickable { expanded = !expanded }
    ) {
        Column(Modifier.padding(8.dp).height(IntrinsicSize.Min)) {
            Row {
                Column {
                    Text(title, fontWeight = FontWeight.Bold)
                    Text(hint)
                }
                Spacer(Modifier.weight(1f))
                ToggleArrowIcon(expanded)
            }
            if (expanded) {
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Row {
                    Column(Modifier.padding(4.dp)) {
                        bullets.forEach { Text("- $it") }
                    }
                    Spacer(Modifier.weight(1f))
                    Column {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.clickable { updateAction(category) }
                        )
                        Spacer(Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.clickable {
                                expanded = false
                                deleteAction(category)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryInsertDialog(expanded: MutableState<Boolean>, insertAction: (Category) -> Unit) {
    Dialog(onDismissRequest = { expanded.value = false }) {
        Card {
            Column(Modifier.padding(12.dp).width(IntrinsicSize.Max)) {
                Text("New Category", fontWeight = FontWeight.Bold)
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                val name = remember { mutableStateOf("") }
                val easy = remember { mutableIntStateOf(0) }
                val medium = remember { mutableIntStateOf(0) }
                val hard = remember { mutableIntStateOf(0) }
                val downloadable = remember { mutableStateOf(false) }
                CategoryEditor(name, easy, medium, hard, downloadable)
                Row {
                    IconButton(
                        onClick = { expanded.value = false },
                        content = { Icon(Icons.Default.Close, null) }
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            insertAction(Category(name.value, easy.intValue, medium.intValue, hard.intValue, downloadable.value))
                            expanded.value = false
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
private fun CategoryInsertDialogPreview() {
    CategoryInsertDialog(mutableStateOf(true)) {}
}

@Composable
private fun CategoryUpdateDialog(
    expanded: MutableState<Boolean>,
    category: Category,
    updateAction: (Category) -> Unit
) {
    Dialog(onDismissRequest = { expanded.value = false }) {
        Card {
            Column(Modifier.padding(12.dp).width(IntrinsicSize.Max)) {
                Text("Update Category", fontWeight = FontWeight.Bold)
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                Text("id : ${category.id}")
                val name = remember { mutableStateOf(category.name) }
                val easy = remember { mutableIntStateOf(category.easy) }
                val medium = remember { mutableIntStateOf(category.medium) }
                val hard = remember { mutableIntStateOf(category.hard) }
                val downloadable = remember { mutableStateOf(category.downloadable) }
                CategoryEditor(name, easy, medium, hard, downloadable)
                Row {
                    IconButton(
                        onClick = { expanded.value = false },
                        content = { Icon(Icons.Default.Close, null) }
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            updateAction(Category(name.value, easy.intValue, medium.intValue, hard.intValue, downloadable.value))
                            expanded.value = false
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
private fun CategoryUpdateDialogPreview() {
    val expanded = mutableStateOf(true)
    val category = Category("Name", 1, 2, 3, false, 0)
    CategoryUpdateDialog(expanded, category, {})
}

@Composable
private fun CategoryEditor(
    name: MutableState<String>,
    easy: MutableIntState,
    medium: MutableIntState,
    hard: MutableIntState,
    downloadable: MutableState<Boolean>,
) {
    Column {
        PlainTextField(
            name.value, { name.value = it },
            leadingIcon = { Text("Name") },
        )
        PlainTextField(
            easy.intValue.toString(), { easy.intValue = it.toIntOrNull() ?: 0 },
            numberOnly = true,
            leadingIcon = { Text("Easy") },
        )
        PlainTextField(
            medium.intValue.toString(), { medium.intValue = it.toIntOrNull() ?: 0 },
            numberOnly = true,
            leadingIcon = { Text("Medium") },
        )
        PlainTextField(
            hard.intValue.toString(), { hard.intValue = it.toIntOrNull() ?: 0 },
            numberOnly = true,
            leadingIcon = { Text("Hard") },
        )
        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Downloadable")
            Spacer(Modifier.width(4.dp))
            Checkbox(downloadable.value, { downloadable.value = it })
        }
    }
}