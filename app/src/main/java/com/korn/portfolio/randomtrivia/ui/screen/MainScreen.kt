@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.korn.portfolio.randomtrivia.ui.common.navigateBottomNav
import com.korn.portfolio.randomtrivia.ui.navigation.About
import com.korn.portfolio.randomtrivia.ui.navigation.Categories
import com.korn.portfolio.randomtrivia.ui.navigation.History
import com.korn.portfolio.randomtrivia.ui.navigation.Inspect
import com.korn.portfolio.randomtrivia.ui.navigation.Play
import com.korn.portfolio.randomtrivia.ui.viewmodel.SharedViewModel

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var showBottomBar by remember { mutableStateOf(true) }
    fun requestFullScreen() { showBottomBar = false }
    fun dismissFullScreen() { showBottomBar = true }

    val navController = rememberNavController()

    Scaffold(
        modifier = modifier,
        bottomBar = { if (showBottomBar) BottomBar(navController) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        val sharedViewModel: SharedViewModel = viewModel()
        NavHost(navController = navController, startDestination = Categories) {
            navigation<Categories>(startDestination = Categories.Default) {
                composable<About> {
                    LaunchedEffect(Unit) {
                        requestFullScreen()
                    }
                    AboutScreen(
                        modifier = Modifier.padding(paddingValues),
                        onBack = { navController.navigateUp() }
                    )
                }
                composable<Categories.Default> {
                    LaunchedEffect(Unit) {
                        dismissFullScreen()
                    }
                    Categories(
                        modifier = Modifier.padding(paddingValues),
                        navToQuestions = { categoryId ->
                            navController.navigate(Categories.Questions(categoryId))
                        },
                        navToAboutScreen = { navController.navigate(About) }
                    )
                }
                composable<Categories.Questions> { backStackEntry ->
                    LaunchedEffect(Unit) {
                        dismissFullScreen()
                    }
                    Questions(
                        modifier = Modifier.padding(paddingValues),
                        categoryId = backStackEntry.toRoute<Categories.Questions>().categoryId,
                        onBack = {
                            navController.navigate(Categories.Default) {
                                popUpTo(Categories.Default) { inclusive = true }
                            }
                        },
                        navToAboutScreen = { navController.navigate(About) }
                    )
                }
            }
            navigation<Play>(startDestination = Play.Setting) {
                composable<Play.Setting> {
                    LaunchedEffect(Unit) {
                        dismissFullScreen()
                    }
                    SettingBeforePlaying(
                        modifier = Modifier.padding(paddingValues),
                        onSubmit = { onlineMode, settings ->
                            sharedViewModel.onlineMode = onlineMode
                            sharedViewModel.settings = settings
                            navController.navigate(Play.Loading)
                        }
                    )
                }
                composable<Play.Loading> {
                    LaunchedEffect(Unit) {
                        requestFullScreen()
                    }
                    LoadingBeforePlaying(
                        modifier = Modifier.systemBarsPadding(),
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
                    LaunchedEffect(Unit) {
                        requestFullScreen()
                    }
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
                    LaunchedEffect(Unit) {
                        requestFullScreen()
                    }
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
                            navController.navigate(Inspect) {
                                popUpTo(Inspect) { inclusive = true }
                            }
                        }
                    )
                }
            }
            navigation<History>(startDestination = History.Default) {
                composable<History.Default> {
                    LaunchedEffect(Unit) {
                        dismissFullScreen()
                    }
                    PastGames(
                        modifier = Modifier.padding(paddingValues),
                        onReplay = { game ->
                            sharedViewModel.game = game
                            navController.navigate(History.Replay)
                        },
                        onInspect = { game ->
                            sharedViewModel.game = game
                            navController.navigate(Inspect)
                        },
                        navToAboutScreen = { navController.navigate(About) }
                    )
                }
                composable<History.Replay> {
                    LaunchedEffect(Unit) {
                        requestFullScreen()
                    }
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
                    LaunchedEffect(Unit) {
                        requestFullScreen()
                    }
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
                            navController.navigate(Inspect) {
                                popUpTo(Inspect) { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable<Inspect> {
                LaunchedEffect(Unit) {
                    requestFullScreen()
                }
                Inspect(
                    onBack = {
                        navController.navigateBottomNav(History)
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