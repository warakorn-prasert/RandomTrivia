@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.korn.portfolio.randomtrivia.ui.common.BottomBar
import com.korn.portfolio.randomtrivia.ui.navigation.Categories
import com.korn.portfolio.randomtrivia.ui.navigation.History
import com.korn.portfolio.randomtrivia.ui.navigation.Inspect
import com.korn.portfolio.randomtrivia.ui.navigation.Play
import com.korn.portfolio.randomtrivia.ui.viewmodel.SharedViewModel

@Composable
fun MainScreen() {
    var showBottomBar by remember { mutableStateOf(true) }
    fun requestFullScreen() { showBottomBar = false }
    fun dismissFullScreen() { showBottomBar = true }

    val navController = rememberNavController()

    Scaffold(
        bottomBar = { if (showBottomBar) BottomBar(navController) }
    ) { paddingValues ->
        val sharedViewModel: SharedViewModel = viewModel()
        NavHost(
            navController = navController,
            startDestination = Categories,
            modifier = Modifier.padding(paddingValues)
        ) {
            navigation<Categories>(startDestination = Categories.Default) {
                composable<Categories.Default> {
                    dismissFullScreen()
                    Categories(
                        navToQuestions = { categoryId ->
                            navController.navigate(Categories.Questions(categoryId))
                        }
                    )
                }
                composable<Categories.Questions> { backStackEntry ->
                    dismissFullScreen()
                    Questions(
                        categoryId = backStackEntry.toRoute<Categories.Questions>().categoryId,
                        onBack = {
                            navController.navigate(Categories.Default) {
                                popUpTo(Categories.Default) { inclusive = true }
                            }
                        }
                    )
                }
            }
            navigation<Play>(startDestination = Play.Setting) {
                composable<Play.Setting> {
                    dismissFullScreen()
                    SettingBeforePlaying(
                        onSubmit = { onlineMode, settings ->
                            sharedViewModel.onlineMode = onlineMode
                            sharedViewModel.settings = settings
                            navController.navigate(Play.Loading)
                        }
                    )
                }
                composable<Play.Loading> {
                    requestFullScreen()
                    LoadingBeforePlaying(
                        onlineMode = sharedViewModel.onlineMode,
                        settings = sharedViewModel.settings,
                        onCancel = {
                            navController.navigate(Play.Setting) {
                                popUpTo(Play.Setting) { inclusive = true }
                            }
                        },
                        onStart = { game ->
                            sharedViewModel.game = game
                            navController.navigate(Play.Playing) {
                                popUpTo(Play.Playing) { inclusive = true }
                            }
                        }
                    )
                }
                composable<Play.Playing> {
                    requestFullScreen()
                    Playing(
                        game = sharedViewModel.game,
                        onExit = {
                            navController.navigate(Play.Setting) {
                                popUpTo(Play.Setting) { inclusive = true }
                            }
                        },
                        onSubmit = { game ->
                            sharedViewModel.game = game
                            navController.navigate(Play.Result) {
                                popUpTo(Play.Result) { inclusive = true }
                            }
                        }
                    )
                }
                composable<Play.Result> {
                    requestFullScreen()
                    Result(
                        game = sharedViewModel.game,
                        onExit = {
                            navController.navigate(Play.Setting) {
                                popUpTo(Play.Setting) { inclusive = true }
                            }
                        },
                        onReplay = { _ ->
                            navController.navigate(Play.Playing) {
                                popUpTo(Play.Playing) { inclusive = true }
                            }
                        },
                        onInspect = { game ->
                            sharedViewModel.game = game
                            navController.navigate(Inspect)
                        }
                    )
                }
            }
            navigation<History>(startDestination = History.Default) {
                composable<History.Default> {
                    dismissFullScreen()
                    PastGames(
                        onReplay = { game ->
                            sharedViewModel.game = game
                            navController.navigate(History.Replay)
                        },
                        onInspect = { game ->
                            sharedViewModel.game = game
                            navController.navigate(Inspect)
                        }
                    )
                }
                composable<History.Replay> {
                    requestFullScreen()
                    Playing(
                        game = sharedViewModel.game,
                        onExit = {
                            navController.navigate(History.Default) {
                                popUpTo(History.Default) { inclusive = true }
                            }
                        },
                        onSubmit = { game ->
                            sharedViewModel.game = game
                            navController.navigate(History.Result) {
                                popUpTo(History.Result) { inclusive = true }
                            }
                        }
                    )
                }
                composable<History.Result> {
                    requestFullScreen()
                    Result(
                        game = sharedViewModel.game,
                        onExit = {
                            navController.navigate(History.Default) {
                                popUpTo(History.Default) { inclusive = true }
                            }
                        },
                        onReplay = { _ ->
                            navController.navigate(History.Replay) {
                                popUpTo(History.Replay) { inclusive = true }
                            }
                        },
                        onInspect = { game ->
                            sharedViewModel.game = game
                            navController.navigate(Inspect)
                        }
                    )
                }
            }
            composable<Inspect> {
                requestFullScreen()
                Inspect(
                    onBack = {
                        navController.navigate(History) {
                            popUpTo(History) { inclusive = true }
                        }
                    },
                    onReplay = { _ ->
                        navController.navigate(History.Replay) {
                            popUpTo(History.Replay) { inclusive = true }
                        }
                    },
                    game = sharedViewModel.game
                )
            }
        }
    }
}