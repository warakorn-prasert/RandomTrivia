@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.korn.portfolio.randomtrivia.ui.model.GameStage
import com.korn.portfolio.randomtrivia.ui.model.NetworkUiState
import kotlinx.coroutines.launch

@Composable
fun PlayScreen(
    snackbarHostState: SnackbarHostState,
    playing: MutableState<Boolean>
) {
    val navController = rememberNavController()
    val playViewModel: PlayViewModel = viewModel(factory = PlayViewModel.Factory)

    val screenScope = rememberCoroutineScope()
    var asking by remember { mutableStateOf(false) }
    fun askToExit() {
        if (!asking) { screenScope.launch {
            asking = true
            val result = snackbarHostState.showSnackbar(
                message = "Exit game?",
                actionLabel = "Yes",
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            )
            when (result) {
                SnackbarResult.Dismissed -> {}
                SnackbarResult.ActionPerformed -> {
                    navController.navigate(GameStage.SETTING.route) {
                        popUpTo(GameStage.PLAYING.route) {
                            inclusive = true
                        }
                    }
                    playing.value = false
                    playViewModel.resetGame()
                }
            }
            asking = false
        } }
    }
    if (playing.value) {
        BackHandler(true) { askToExit() }
    }

    NavHost(
        navController = navController,
        startDestination = GameStage.SETTING.route
    ) {
        composable(GameStage.SETTING.route) {
            val uiState: MutableState<NetworkUiState> = remember { mutableStateOf(NetworkUiState.Success) }
            LaunchedEffect(uiState.value) {
                when (val s = uiState.value) {
                    is NetworkUiState.Error -> snackbarHostState.showSnackbar("Error (${s.error.message})")
                    NetworkUiState.Loading -> {}
                    NetworkUiState.Success -> {
                        if (playing.value)
                            // playing is always set to true to update uiState to show loading animation
                            navController.navigate(GameStage.PLAYING.route)
                    }
                }
            }
            Box {
                val remoteCategories by playViewModel.remoteCategories.observeAsState(emptyList())
                val localCategories by playViewModel.localCategories.collectAsState(emptyList())
                SettingStage(
                    remoteCategories = remoteCategories,
                    localCategories = localCategories,
                    fetchCategories = playViewModel::fetchCategories,
                    fetchQuestionCount = playViewModel::fetchQuestionCount,
                    onDone = { options, offline ->
                        playing.value = true
                        playViewModel.fetchNewGame(options, offline, uiState)
                    }
                )
                if (uiState.value is NetworkUiState.Loading) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Card { CircularProgressIndicator(Modifier.padding(12.dp)) }
                    }
                }
            }
        }
        composable(GameStage.PLAYING.route) {
            LaunchedEffect(playing.value) {
                playViewModel.setTimer(playing.value)
            }
            PlayingStage(
                game = playViewModel.game,
                askToExit = ::askToExit,
                timerSecond = playViewModel.timerSecond,
                onDone = {
                    playViewModel.saveGame()
                    playing.value = false
                    navController.navigate(GameStage.RESULT.route) {
                        popUpTo(GameStage.PLAYING.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(GameStage.RESULT.route) {
            BackHandler(true) {
                playViewModel.resetGame()
                navController.navigate(GameStage.SETTING.route)
            }
            ResultStage()
        }
    }
}

@Composable
private fun ResultStage() {

}