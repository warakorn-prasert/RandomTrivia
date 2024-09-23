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
import com.korn.portfolio.randomtrivia.ui.common.BottomBar
import com.korn.portfolio.randomtrivia.ui.navigation.Categories
import com.korn.portfolio.randomtrivia.ui.navigation.History
import com.korn.portfolio.randomtrivia.ui.navigation.Play
import com.korn.portfolio.randomtrivia.ui.viewmodel.CategoriesViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.HistoryViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.PlayViewModel

@Composable
fun MainScreen() {
    var showBottomBar by remember { mutableStateOf(true) }
    fun requestFullScreen() { showBottomBar = false }
    fun dismissFullScreen() { showBottomBar = true }

    val navController = rememberNavController()

    Scaffold(
        bottomBar = { if (showBottomBar) BottomBar(navController) }
    ) { paddingValues ->
        // TODO : Localize viewModels (for passing data, use savedStateHandle either inside viewModel or in nav block)
        val categoriesViewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory)
        val playViewModel: PlayViewModel = viewModel()
        val historyViewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.Factory)
        NavHost(
            navController = navController,
            startDestination = Categories,
            modifier = Modifier.padding(paddingValues)
        ) {
            navigation<Categories>(startDestination = Categories.Default) {
                composable<Categories.Default> {
                    dismissFullScreen()
                    Categories(
                        categoriesViewModel = categoriesViewModel,
                        onCategoryCardClick = { categoryId ->
                            categoriesViewModel.getPlayedQuestions(categoryId)
                            navController.navigate(Categories.Questions)
                        }
                    )
                }
                composable<Categories.Questions> {
                    dismissFullScreen()
                    Questions(
                        categoryName = categoriesViewModel.categoryName,
                        questions = categoriesViewModel.questions,
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
                            playViewModel.onlineMode = onlineMode
                            playViewModel.settings = settings
                            navController.navigate(Play.Loading)
                        }
                    )
                }
                composable<Play.Loading> {
                    requestFullScreen()
                    LoadingBeforePlaying(
                        onlineMode = playViewModel.onlineMode,
                        settings = playViewModel.settings,
                        onCancel = {
                            navController.navigate(Play.Setting) {
                                popUpTo(Play.Setting) { inclusive = true }
                            }
                        },
                        onStart = { game ->
                            playViewModel.game = game
                            navController.navigate(Play.Playing) {
                                popUpTo(Play.Playing) { inclusive = true }
                            }
                        }
                    )
                }
                composable<Play.Playing> {
                    requestFullScreen()
                    Playing(
                        game = playViewModel.game,
                        onExit = {
                            navController.navigate(Play.Setting) {
                                popUpTo(Play.Setting) { inclusive = true }
                            }
                        },
                        onSubmit = { game ->
                            playViewModel.game = game
                            navController.navigate(Play.Result) {
                                popUpTo(Play.Result) { inclusive = true }
                            }
                        }
                    )
                }
                composable<Play.Result> {
                    requestFullScreen()
                    Result(
                        game = playViewModel.game,
                        onExit = {
                            navController.navigate(Play.Setting) {
                                popUpTo(Play.Setting) { inclusive = true }
                            }
                        },
                        onReplay = { _ ->
                            navController.navigate(Play.Playing) {
                                popUpTo(Play.Playing) { inclusive = true }
                            }
                        }
                    )
                }
            }
            navigation<History>(startDestination = History.Default) {
                composable<History.Default> {
                    dismissFullScreen()
                    PastGames(
                        historyViewModel = historyViewModel,
                        onReplay = { game ->
                            historyViewModel.gameToReplay = game
                            navController.navigate(History.Replay)
                        }
                    )
                }
                composable<History.Replay> {
                    requestFullScreen()
                    Playing(
                        game = historyViewModel.gameToReplay,
                        onExit = {
                            navController.navigate(History.Default) {
                                popUpTo(History.Default) { inclusive = true }
                            }
                        },
                        onSubmit = { game ->
                            historyViewModel.gameToReplay = game
                            navController.navigate(History.Result) {
                                popUpTo(History.Result) { inclusive = true }
                            }
                        }
                    )
                }
                composable<History.Result> {
                    requestFullScreen()
                    Result(
                        game = historyViewModel.gameToReplay,
                        onExit = {
                            navController.navigate(History.Default) {
                                popUpTo(History.Default) { inclusive = true }
                            }
                        },
                        onReplay = { _ ->
                            navController.navigate(History.Replay) {
                                popUpTo(History.Replay) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}