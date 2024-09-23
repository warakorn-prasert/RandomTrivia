@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.korn.portfolio.randomtrivia.ui.navigation.PlayNavigation
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.PlayViewModel

@Composable
fun Play(
    requestFullScreen: () -> Unit,
    dismissFullScreen: () -> Unit
) {
    val playViewModel: PlayViewModel = viewModel()
    val navController = rememberNavController()
    NavHost(navController, startDestination = PlayNavigation.SETTING.route) {
        composable(PlayNavigation.SETTING.route) {
            dismissFullScreen()
            SettingBeforePlaying(
                onSubmit = { onlineMode, settings ->
                    playViewModel.onlineMode = onlineMode
                    playViewModel.settings = settings
                    navController.navigate(PlayNavigation.LOADING.route)
                }
            )
        }
        composable(PlayNavigation.LOADING.route) {
            requestFullScreen()
            LoadingBeforePlaying(
                onlineMode = playViewModel.onlineMode,
                settings = playViewModel.settings,
                onCancel = {
                    navController.navigate(PlayNavigation.SETTING.route) {
                        popUpTo(PlayNavigation.LOADING.route) {
                            inclusive = true
                        }
                    }
                },
                onStart = { game ->
                    playViewModel.game = game
                    navController.navigate(PlayNavigation.PLAYING.route) {
                        popUpTo(PlayNavigation.LOADING.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(PlayNavigation.PLAYING.route) {
            requestFullScreen()
            Playing(
                game = playViewModel.game,
                onExit = {
                    navController.navigate(PlayNavigation.SETTING.route) {
                        popUpTo(PlayNavigation.PLAYING.route) {
                            inclusive = true
                        }
                    }
                },
                onSubmit = { game ->
                    playViewModel.game = game
                    navController.navigate(PlayNavigation.RESULT.route) {
                        popUpTo(PlayNavigation.PLAYING.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(PlayNavigation.RESULT.route) {
            requestFullScreen()
            Result(
                game = playViewModel.game,
                onExit = {
                    navController.navigate(PlayNavigation.SETTING.route) {
                        popUpTo(PlayNavigation.RESULT.route) {
                            inclusive = true
                        }
                    }
                },
                onReplay = { _ ->
                    navController.navigate(PlayNavigation.PLAYING.route) {
                        popUpTo(PlayNavigation.RESULT.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

@Preview
@Composable
private fun PlayPreview() {
    RandomTriviaTheme {
        Play({}, {})
    }
}