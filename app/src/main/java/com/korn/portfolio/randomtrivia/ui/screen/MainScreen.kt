@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.korn.portfolio.randomtrivia.ui.navigation.GameSettingType
import com.korn.portfolio.randomtrivia.ui.navigation.WrappedGameType
import com.korn.portfolio.randomtrivia.ui.navigation.TopLevelDestination
import com.korn.portfolio.randomtrivia.ui.navigation.WrappedGame
import com.korn.portfolio.randomtrivia.ui.viewmodel.CategoriesViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import com.korn.portfolio.randomtrivia.ui.viewmodel.LoadingBeforePlayingViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.SettingBeforePlayingViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.SharedViewModel
import kotlin.reflect.typeOf

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var showNavigationUi by remember { mutableStateOf(true) }
    fun requestFullScreen() { showNavigationUi = false }
    fun dismissFullScreen() { showNavigationUi = true }

    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = viewModel(factory = SharedViewModel.Factory)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            TopLevelDestination.entries.forEach { topDest ->
                val selected = topDest.screens.any { screen ->
                    currentDestination?.hierarchy?.any { it.hasRoute(screen) } == true
                }
                item(
                    icon = {
                        Icon(
                            painter = painterResource(topDest.icon),
                            contentDescription = topDest.contentDescription
                        )
                    },
                    label = { Text(topDest.label) },
                    selected = selected,
                    onClick = {
                        if (!selected)
                            navController.navigate(topDest) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                restoreState = true
                                // fix: 2nd back press from non-start bottom nav destination doesn't exit app
                                launchSingleTop = true
                            }
                    }
                )
            }
        },
        layoutType =
            if (showNavigationUi)
                NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(currentWindowAdaptiveInfo())
            else
                NavigationSuiteType.None,
        modifier = modifier
    ) {
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Categories,
            modifier = Modifier.displayCutoutPadding()
        ) {
            composable<TopLevelDestination.About> {
                requestFullScreen()
                AboutScreen(
                    onExit = { navController.navigateUp() },
                    modifier = Modifier.systemBarsPadding()
                )
            }
            composable<TopLevelDestination.Categories> {
                dismissFullScreen()
                val viewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory)
                val categories by viewModel.categories.collectAsState(emptyList())
                CategoriesAndQuestions(
                    categories = categories,
                    categoriesFetchStatus = sharedViewModel.categoriesFetchStatus,
                    onRetryFetch = { sharedViewModel.fetchCategories() },
                    onAboutClick = { navController.navigate(TopLevelDestination.About) },
                    onGetQuestionsRequest = { categoryId, onDone ->
                        viewModel.getQuestions(categoryId, onDone)
                    },
                    modifier = Modifier.systemBarsPadding()
                )
            }
            navigation<TopLevelDestination.PrePlay>(startDestination = TopLevelDestination.PrePlay.Setting) {
                composable<TopLevelDestination.PrePlay.Setting> {
                    dismissFullScreen()
                    val viewModel: SettingBeforePlayingViewModel = viewModel(factory = SettingBeforePlayingViewModel.Factory)
                    val onlineMode by viewModel.onlineMode.collectAsState()
                    val catsWithCounts by viewModel.categoriesWithQuestionCounts.collectAsState(emptyList())
                    SettingBeforePlaying(
                        categoriesWithQuestionCounts = catsWithCounts,
                        onlineMode = onlineMode,
                        onOnlineModeChange = { online -> viewModel.changeOnlineMode(online) },
                        categoriesFetchStatus = sharedViewModel.categoriesFetchStatus,
                        onFetchCategoriesRequest = { sharedViewModel.fetchCategories() },
                        onFetchQuestionCountRequest = { categoryId, onFetchStatusChange ->
                            viewModel.fetchQuestionCountIfNotAlready(categoryId, onFetchStatusChange)
                        },
                        onSubmit = { settings ->
                            navController.navigate(TopLevelDestination.PrePlay.Loading(onlineMode, settings))
                        },
                        modifier = Modifier.systemBarsPadding()
                    )
                }
                composable<TopLevelDestination.PrePlay.Loading>(
                    typeMap = mapOf(typeOf<List<GameSetting>>() to GameSettingType)
                ) { backStackEntry ->
                    requestFullScreen()
                    val (onlineMode, settings) = backStackEntry.toRoute<TopLevelDestination.PrePlay.Loading>()
                    val viewModel: LoadingBeforePlayingViewModel = viewModel(
                        factory = LoadingBeforePlayingViewModel.Factory(onlineMode, settings)
                    )
                    val fetchStatus by viewModel.fetchStatus.collectAsState()
                    LoadingBeforePlaying(
                        progress = viewModel.progress,
                        statusText = viewModel.statusText,
                        fetchStatus = fetchStatus,
                        onCancel = {
                            viewModel.cancelFetch()
                            navController.navigateUp()
                        },
                        onRetry = { viewModel.fetch() },
                        onMaxProgress = {
                            navController.navigate(TopLevelDestination.Play(WrappedGame(viewModel.game!!))) {
                                popUpTo(TopLevelDestination.PrePlay.Setting)
                            }
                        },
                        modifier = Modifier.systemBarsPadding()
                    )
                }
            }
            navigation<TopLevelDestination.Play>(
                startDestination = TopLevelDestination.Play.Screen.Playing,
                typeMap = mapOf(typeOf<WrappedGame>() to WrappedGameType)
            ) {
                composable<TopLevelDestination.Play.Screen.Playing> { backStackEntry ->
                    requestFullScreen()
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry<TopLevelDestination.Play>()
                    }
                    val inputGame = parentEntry.toRoute<TopLevelDestination.Play>().wrappedGame.game
                    Playing(
                        game = inputGame,
                        onExit = { navController.navigateUp() },
                        onSubmit = { game ->
                            sharedViewModel.saveGame(game)
                            navController.navigate(TopLevelDestination.Play.Screen.Result(WrappedGame(game))) {
                                popUpTo<TopLevelDestination.Play.Screen.Playing> { inclusive = true }
                            }
                        }
                    )
                }
                composable<TopLevelDestination.Play.Screen.Result>(
                    typeMap = mapOf(typeOf<WrappedGame>() to WrappedGameType)
                ) { backStackEntry ->
                    requestFullScreen()
                    val inputGame = backStackEntry.toRoute<TopLevelDestination.Play.Screen.Result>().wrappedGame.game
                    Result(
                        game = inputGame,
                        onExit = { navController.navigateUp() },
                        onReplay = { game ->
                            navController.navigate(TopLevelDestination.Play(WrappedGame(game))) {
                                popUpTo<TopLevelDestination.Play.Screen.Result> { inclusive = true }
                            }
                        },
                        onInspect = { game ->
                            navController.navigate(TopLevelDestination.Inspect(WrappedGame(game))) {
                                popUpTo<TopLevelDestination.Play.Screen.Result> { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable<TopLevelDestination.History> {
                dismissFullScreen()
                PastGames(
                    pastGames = sharedViewModel.pastGames.collectAsState(emptyList()).value,
                    onDelete = { sharedViewModel.deleteGame(it) },
                    onReplay = { game ->
                        navController.navigate(TopLevelDestination.Play(WrappedGame(game)))
                    },
                    onInspect = { game ->
                        navController.navigate(TopLevelDestination.Inspect(WrappedGame(game)))
                    },
                    onAboutClick = { navController.navigate(TopLevelDestination.About) },
                    modifier = Modifier.systemBarsPadding()
                )
            }
            composable<TopLevelDestination.Inspect>(
                typeMap = mapOf(typeOf<WrappedGame>() to WrappedGameType)
            ) { backStackEntry ->
                requestFullScreen()
                val inputGame = backStackEntry.toRoute<TopLevelDestination.Inspect>().wrappedGame.game
                Inspect(
                    game = inputGame,
                    onExit = { navController.navigateUp() },
                    onReplay = { game ->
                        navController.navigate(TopLevelDestination.Play(WrappedGame(game))) {
                            popUpTo<TopLevelDestination.Inspect> { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}