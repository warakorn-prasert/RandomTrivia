package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.model.Category
import com.korn.portfolio.randomtrivia.model.Count
import com.korn.portfolio.randomtrivia.model.Question
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

private enum class DifficultyOption {
    UNSELECTED,
    EASY,
    MEDIUM,
    HARD;
}

@Composable
fun SettingScreen(onResult: (List<Question>) -> Unit) {
    val triviaViewModel: TriviaViewModel =
        viewModel(factory = TriviaViewModel.Factory)

    // UiState variables holding data
    val categoryUiState: MutableState<UiState<List<Category>>> =
        remember { mutableStateOf(UiState.Idle()) }
    val countUiState: MutableState<UiState<Count>> =
        remember { mutableStateOf(UiState.Idle()) }
    val questionsUiState: MutableState<UiState<List<Question>>> =
        remember { mutableStateOf(UiState.Idle()) }

    // Selection
    var category by remember { mutableStateOf<Category?>(null) }
    var difficulty by remember { mutableStateOf(DifficultyOption.UNSELECTED) }
    var totalQuestions by remember { mutableIntStateOf(0) }
    LaunchedEffect(category) {
        difficulty = DifficultyOption.UNSELECTED
    }
    LaunchedEffect(difficulty) {
        totalQuestions = 0
    }

    // Flags checking if each selection is selected
    val categorySelected = category != null
    val difficultySelected = difficulty != DifficultyOption.UNSELECTED
    val totalQuestionsSelected = totalQuestions > 0

    Column(
        Modifier.fillMaxSize(),
        Arrangement.Center,
        Alignment.CenterHorizontally
    ) {
        // Select category
        LoadingDropdown(
            enabled = questionsUiState.value !is UiState.Loading,
            selected = category,
            loading = categoryUiState.value is UiState.Loading,
            loadAction = { triviaViewModel.getCategories(categoryUiState) },
            error = categoryUiState.value is UiState.Error,
            items = when (val s = categoryUiState.value) {
                is UiState.Error, is UiState.Idle, is UiState.Loading ->
                    emptyList()
                is UiState.Success ->
                    s.data.sortedBy { it.name }
            },
            getName = { it?.name ?: "--  CATEGORY  --" },
            onItemSelect = {
                triviaViewModel.getCount(
                    categoryId = it!!.id,
                    uiState = countUiState
                )
                category = it
            }
        )

        // Select difficulty
        LoadingDropdown(
            enabled = categorySelected && questionsUiState.value !is UiState.Loading,
            selected = difficulty,
            loading = countUiState.value is UiState.Loading,
            loadAction = {},
            error = countUiState.value is UiState.Error,
            items = DifficultyOption.entries.filter { it != DifficultyOption.UNSELECTED },
            getName = {
                if (it == DifficultyOption.UNSELECTED) "--  DIFFICULTY  --"
                else it.name
            },
            onItemSelect = {
                difficulty = it
            }
        )

        // Select total questions
        val enabled = difficultySelected && questionsUiState.value !is UiState.Loading
        val count: Count = when (val s = countUiState.value) {
            is UiState.Error, is UiState.Idle, is UiState.Loading ->
                Count(0, 0, 0, 0)
            is UiState.Success ->
                s.data
        }
        val max = if (enabled || questionsUiState.value is UiState.Loading) when (difficulty) {
            DifficultyOption.UNSELECTED -> 0
            DifficultyOption.EASY -> count.easy
            DifficultyOption.MEDIUM -> count.medium
            DifficultyOption.HARD -> count.hard
        }.coerceAtMost(50) else 0
        val min = if (max > 0) 1 else 0
        totalQuestions = totalQuestions.coerceIn(min, max)
        Column(Modifier.fillMaxWidth(0.5f)) {
            Slider(
                value = totalQuestions.toFloat(),
                onValueChange = { totalQuestions = it.toInt() },
                enabled = enabled,
                valueRange = min.toFloat()..max.toFloat(),
                steps = max - min
            )
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text(min.toString())
                Text(totalQuestions.toString())
                Text(max.toString())
            }
        }

        // Finish
        when (val s = questionsUiState.value) {
            is UiState.Idle, is UiState.Error -> {
                Button(
                    onClick = {
                        triviaViewModel.getQuestions(
                            amount = totalQuestions,
                            categoryId = category!!.id,
                            difficulty = difficulty.name.lowercase(),
                            uiState = questionsUiState
                        )
                    },
                    enabled = categorySelected && difficultySelected && totalQuestionsSelected
                ) {
                    Text(if (s is UiState.Idle) "Start" else "Retry")
                }
                if (s is UiState.Error) Text("Failed to fetch")
            }
            is UiState.Loading -> CircularProgressIndicator()
            is UiState.Success -> onResult(s.data)
        }
    }
}

@Composable
private fun <T> LoadingDropdown(
    enabled: Boolean,
    selected: T,
    loading: Boolean,
    loadAction: suspend () -> Unit,
    error: Boolean,
    items: List<T>,
    getName: (T) -> String,
    onItemSelect: (selectedItem: T) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    expanded = expanded && enabled
    Column(modifier) {
        Row(Modifier.let {
            if (enabled) it.clickable {
                // Load on expand
                if (!expanded) {
                    scope.launch { loadAction() }
                } else {
                    scope.cancel()
                }
                expanded = !expanded
            } else it
        }) {
            Text(getName(selected))
            Icon(
                imageVector =
                if (expanded) Icons.Default.KeyboardArrowUp
                else Icons.Default.KeyboardArrowDown,
                contentDescription = null
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (loading) {
                CircularProgressIndicator()
            } else if (error) {
                Text("Error")
            } else {
                items.forEach {
                    DropdownMenuItem(
                        text = { Text(getName(it)) },
                        onClick = {
                            expanded = false
                            onItemSelect(it)
                        }
                    )
                }
            }
        }
    }
}