package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.model.Category
import com.korn.portfolio.randomtrivia.model.Count

@Composable
fun CategoryScreen() {
    val triviaViewModel: TriviaViewModel = viewModel(factory = TriviaViewModel.Factory)
    val uiState: MutableState<UiState<List<Category>>> = remember { mutableStateOf(UiState.Loading()) }

    LaunchedEffect(Unit) {
        triviaViewModel.getCategories(uiState)
    }

    when (val s = uiState.value) {
        is UiState.Idle, is UiState.Loading ->
            LoadingScreen()
        is UiState.Error ->
            ErrorScreen(refreshAction = { triviaViewModel.getCategories(uiState) })
        is UiState.Success ->
            SuccessScreen(s.data, triviaViewModel::getCount)
    }
}

@Composable
private fun ErrorScreen(refreshAction: () -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Connection error")
        Text("Retry.", Modifier.clickable(onClick = refreshAction), textDecoration = TextDecoration.Underline)
    }
}

@Composable
private fun LoadingScreen() {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Icon(Icons.Default.Info, null)
        Text("-- LOADING ANIM --")
    }
}

@Composable
private fun SuccessScreen(
    categories: List<Category>,
    getCount: (categoryId: Int, uiState: MutableState<UiState<Count>>) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val search = remember { mutableStateOf("") }
        Box(Modifier.fillMaxWidth(), Alignment.Center) {
            MyTextField(search, "Search")
        }

        categories
            .filter { it.name.lowercase().contains(search.value.lowercase()) }
            .forEach { savedCategory ->
                val expanded = remember { mutableStateOf(false) }
                val uiState: MutableState<UiState<Count>> = remember { mutableStateOf(UiState.Loading()) }
                CategoryCard(expanded, savedCategory,  uiState, getCount)
            }
    }
}

@Composable
private fun CategoryCard(
    expanded: MutableState<Boolean>,
    savedCategory: Category,
    uiState: MutableState<UiState<Count>>,
    getCount: (categoryId: Int, uiState: MutableState<UiState<Count>>) -> Unit
) {
    Card {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(savedCategory.name, Modifier.align(Alignment.Start))
                if (expanded.value) {
                    when (val s = uiState.value) {
                        is UiState.Error -> {
                            Text("Loading error")
                            Text(
                                text = "Retry.",
                                modifier = Modifier.clickable {
                                    getCount(savedCategory.id, uiState) },
                                textDecoration = TextDecoration.Underline)
                        }
                        is UiState.Idle, is UiState.Loading -> {
                            Icon(Icons.Default.Info, null)
                            Text("-- LOADING ANIM --")
                        }
                        is UiState.Success -> {
                            Column {
                                Text("${s.data.total} questions")
                                Text("• ${s.data.easy} easy")
                                Text("• ${s.data.medium} medium")
                                Text("• ${s.data.hard} hard")
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = if (expanded.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier
                    .clickable {
                        getCount(savedCategory.id, uiState)
                        expanded.value = !expanded.value
                    }
                    .align(if (expanded.value) Alignment.Bottom else Alignment.CenterVertically)
            )
        }
    }
}