@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.korn.portfolio.randomtrivia.ui.model.Screen

@Composable
fun AppScreen() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val playing = remember { mutableStateOf(false) }
    Scaffold(
        bottomBar = {
            TriviaBottomAppBar(navController, playing.value)
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.CATEGORIES.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.CATEGORIES.route) {
                CategoryScreen(snackbarHostState)
            }
            composable(Screen.PLAY.route) {
                PlayScreen(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    playing = playing
                )
            }
            composable(Screen.HISTORY.route) {
                HistoryScreen()
            }
        }
    }
}

@Composable
private fun TriviaBottomAppBar(navController: NavController, playing: Boolean) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    // If playing, change icon to Lock and disable button.
    BottomAppBar {
        Screen.entries.forEach {
            NavigationBarItem(
                selected = it.route == currentRoute && !playing,
                onClick = { if (currentRoute != it.route) navController.navigate(it.route) },
                icon = { Icon(if (!playing) it.icon else Icons.Default.Lock, null) },
                enabled = !playing,
                label = { Text(it.name.replace('_', ' ')) }
            )
        }
    }
}

@Preview
@Composable
private fun TriviaBottomAppBarPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        TriviaBottomAppBar(rememberNavController(), playing = false)
        TriviaBottomAppBar(rememberNavController(), playing = true)
    }
}