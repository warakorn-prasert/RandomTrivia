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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.korn.portfolio.randomtrivia.ui.common.BottomBar
import com.korn.portfolio.randomtrivia.ui.navigation.About
import com.korn.portfolio.randomtrivia.ui.navigation.Categories
import com.korn.portfolio.randomtrivia.ui.navigation.History
import com.korn.portfolio.randomtrivia.ui.navigation.Inspect
import com.korn.portfolio.randomtrivia.ui.navigation.Play
import com.korn.portfolio.randomtrivia.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    mainViewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var showBottomBar by remember { mutableStateOf(true) }
    fun requestFullScreen() { showBottomBar = false }
    fun dismissFullScreen() { showBottomBar = true }

    val navController = rememberNavController()

    Scaffold(
        modifier = modifier,
        bottomBar = { if (showBottomBar) BottomBar(navController) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        NavHost(navController, startDestination = Categories) {
            navigation<Categories>(startDestination = Categories.Default) {
                composable<About> {
                    LaunchedEffect(Unit) {
                        requestFullScreen()
                    }
                    AboutScreen(
                        goBack = { navController.navigateUp() },
                        modifier = Modifier.systemBarsPadding()
                    )
                }
                composable<Categories.Default> {
                    LaunchedEffect(Unit) {
                        dismissFullScreen()
                    }
                    Categories(
                        fetchStatus = mainViewModel.categoriesFetchStatus,
                        fetchCategories = mainViewModel::fetchCategories,
                        onCategoryClick = { categoryId ->
                            navController.navigate(Categories.Questions(categoryId))
                        },
                        onAboutClick = { navController.navigate(About) },
                        modifier = Modifier.padding(paddingValues),
                    )
                }
                composable<Categories.Questions> { backStackEntry ->
                    LaunchedEffect(Unit) {
                        dismissFullScreen()
                    }
                    Questions(
                        categoryId = backStackEntry.toRoute<Categories.Questions>().categoryId,
                        goBack = { navController.navigateUp() },
                        onAboutClick = { navController.navigate(About) },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            navigation<Play>(startDestination = Play.Setting) {
                composable<Play.Setting> {
                    LaunchedEffect(Unit) {
                        dismissFullScreen()
                    }
                    SettingBeforePlaying(
                        categoriesFetchStatus = mainViewModel.categoriesFetchStatus,
                        fetchCategories = mainViewModel::fetchCategories,
                        submit = { onlineMode, settings ->
                            mainViewModel.onlineMode = onlineMode
                            mainViewModel.settings = settings
                            navController.navigate(Play.Loading)
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                composable<Play.Loading> {
                    LaunchedEffect(Unit) {
                        requestFullScreen()
                    }
                    LoadingBeforePlaying(
                        onlineMode = mainViewModel.onlineMode,
                        settings = mainViewModel.settings,
                        cancel = { navController.navigateUp() },
                        onDone = { game ->
                            mainViewModel.game = game
                            navController.navigate(Play.Playing) {
                                popUpTo(Play.Setting)
                            }
                        },
                        modifier = Modifier.systemBarsPadding()
                    )
                }
                composable<Play.Playing> {
                    LaunchedEffect(Unit) {
                        requestFullScreen()
                    }
                    Playing(
                        game = mainViewModel.game,
                        exit = { navController.navigateUp() },
                        submit = { game ->
                            mainViewModel.game = game
                            navController.navigate(Play.Result) {
                                popUpTo(Play.Setting)
                            }
                        }
                    )
                }
                composable<Play.Result> {
                    LaunchedEffect(Unit) {
                        requestFullScreen()
                    }
                    Result(
                        game = mainViewModel.game,
                        exit = { navController.navigateUp() },
                        replay = { _ ->
                            navController.navigate(Play.Playing) {
                                popUpTo(Play.Setting)
                            }
                        },
                        inspect = { game ->
                            mainViewModel.game = game
                            navController.navigate(Inspect) {
                                popUpTo(Play.Setting)
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
                        replay = { game ->
                            mainViewModel.game = game
                            navController.navigate(History.Replay)
                        },
                        inspect = { game ->
                            mainViewModel.game = game
                            navController.navigate(Inspect)
                        },
                        onAboutClick = { navController.navigate(About) },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                composable<History.Replay> {
                    LaunchedEffect(Unit) {
                        requestFullScreen()
                    }
                    Playing(
                        game = mainViewModel.game,
                        exit = { navController.navigateUp() },
                        submit = { game ->
                            mainViewModel.game = game
                            navController.navigate(History.Result) {
                                popUpTo(History.Default)
                            }
                        }
                    )
                }
                composable<History.Result> {
                    LaunchedEffect(Unit) {
                        requestFullScreen()
                    }
                    Result(
                        game = mainViewModel.game,
                        exit = { navController.navigateUp() },
                        replay = { _ ->
                            navController.navigate(History.Replay) {
                                popUpTo(History.Default)
                            }
                        },
                        inspect = { game ->
                            mainViewModel.game = game
                            navController.navigate(Inspect) {
                                popUpTo(History.Default)
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
                    goBack = { navController.navigateUp() },
                    replay = { _ ->
                        navController.navigate(History.Replay) {
                            popUpTo(Inspect) { inclusive = true }
                        }
                    },
                    game = mainViewModel.game
                )
            }
        }
    }
}