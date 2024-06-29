@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@Composable
fun PlayScreen(
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    playing: MutableState<Boolean>
) {
    // TODO : If playing, disable back button and use custom action.
    val screenScope = rememberCoroutineScope()
    BackHandler(enabled = playing.value) {
        screenScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Stop on-going game?",
                actionLabel = "Yes",
                withDismissAction = false,
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.Dismissed -> {}
                SnackbarResult.ActionPerformed -> {
                    playing.value = false
                    val previousRoute = navController.previousBackStackEntry?.destination
                        ?.route
                        ?: Screen.PAST_GAMES.route
                    navController.navigate(previousRoute)
                }
            }
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(if (playing.value) "Playing" else "Not playing")
        IconButton({ playing.value = !playing.value }) { Icon(Icons.Default.Refresh, null) }
    }
}