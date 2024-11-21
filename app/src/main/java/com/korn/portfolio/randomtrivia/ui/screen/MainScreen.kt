@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.korn.portfolio.randomtrivia.ui.navigation.About
import com.korn.portfolio.randomtrivia.ui.navigation.Categories
import com.korn.portfolio.randomtrivia.ui.navigation.Game
import com.korn.portfolio.randomtrivia.ui.navigation.GameSettingType
import com.korn.portfolio.randomtrivia.ui.navigation.History
import com.korn.portfolio.randomtrivia.ui.navigation.Inspect
import com.korn.portfolio.randomtrivia.ui.navigation.Play
import com.korn.portfolio.randomtrivia.ui.navigation.SerializableGameSetting
import com.korn.portfolio.randomtrivia.ui.navigation.deserialized
import com.korn.portfolio.randomtrivia.ui.navigation.serialized
import com.korn.portfolio.randomtrivia.ui.viewmodel.SharedViewModel
import kotlin.reflect.typeOf

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var showBottomBar by remember { mutableStateOf(true) }
    fun requestFullScreen() { showBottomBar = false }
    fun dismissFullScreen() { showBottomBar = true }

    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = viewModel(factory = SharedViewModel.Factory)

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Box(Modifier.animateContentSize()) {
                if (showBottomBar) BottomBar(navController)
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        NavHost(navController, startDestination = Categories) {
            composable<About> {
                requestFullScreen()
                AboutScreen(
                    onExit = { navController.navigateUp() },
                    modifier = Modifier.systemBarsPadding()
                )
            }
            navigation<Categories>(startDestination = Categories.Default) {
                composable<Categories.Default> {
                    dismissFullScreen()
                    Categories(
                        categoriesFetchStatus = sharedViewModel.categoriesFetchStatus,
                        onRetryFetch = { sharedViewModel.fetchCategories() },
                        onCategoryClick = { categoryId ->
                            navController.navigate(Categories.Questions(categoryId))
                        },
                        onAboutClick = { navController.navigate(About) },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                composable<Categories.Questions> { backStackEntry ->
                    dismissFullScreen()
                    Questions(
                        categoryId = backStackEntry.toRoute<Categories.Questions>().categoryId,
                        onExit = { navController.navigateUp() },
                        onAboutClick = { navController.navigate(About) },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            navigation<Play>(startDestination = Play.Setting) {
                composable<Play.Setting> {
                    dismissFullScreen()
                    SettingBeforePlaying(
                        categoriesFetchStatus = sharedViewModel.categoriesFetchStatus,
                        onRetryFetch = { sharedViewModel.fetchCategories() },
                        onSubmit = { onlineMode, settings ->
                            navController.navigate(Play.Loading(onlineMode, settings.serialized()))
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                composable<Play.Loading>(
                    typeMap = mapOf(typeOf<List<SerializableGameSetting>>() to GameSettingType)
                ) { backStackEntry ->
                    requestFullScreen()
                    val (onlineMode, settings) = backStackEntry.toRoute<Play.Loading>()
                    LoadingBeforePlaying(
                        onlineMode = onlineMode,
                        settings = settings.deserialized(),
                        onCancel = { navController.navigateUp() },
                        onDone = { game ->
                            sharedViewModel.game = game
                            navController.navigate(Game) {
                                popUpTo(Play.Setting)
                            }
                        },
                        modifier = Modifier.systemBarsPadding()
                    )
                }
            }
            navigation<Game>(startDestination = Game.Play) {
                composable<Game.Play> {
                    requestFullScreen()
                    Playing(
                        game = sharedViewModel.game,
                        onExit = { navController.navigateUp() },
                        onSubmit = { game ->
                            sharedViewModel.game = game
                            sharedViewModel.saveGame()
                            navController.navigate(Game.Result) {
                                popUpTo(Game.Play) { inclusive = true }
                            }
                        }
                    )
                }
                composable<Game.Result> {
                    requestFullScreen()
                    Result(
                        game = sharedViewModel.game,
                        onExit = { navController.navigateUp() },
                        onReplay = { _ ->
                            navController.navigate(Game.Play) {
                                popUpTo(Game.Result) { inclusive = true }
                            }
                        },
                        onInspect = { game ->
                            sharedViewModel.game = game
                            navController.navigate(Inspect) {
                                popUpTo(Game.Result) { inclusive = true }
                            }
                        }
                    )
                }
            }
            composable<History> {
                dismissFullScreen()
                PastGames(
                    pastGames = sharedViewModel.pastGames.collectAsState(emptyList()).value,
                    onDelete = { sharedViewModel.deleteGame(it) },
                    onReplay = { game ->
                        sharedViewModel.game = game
                        navController.navigate(Game.Play)
                    },
                    onInspect = { game ->
                        sharedViewModel.game = game
                        navController.navigate(Inspect)
                    },
                    onAboutClick = { navController.navigate(About) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            composable<Inspect> {
                requestFullScreen()
                Inspect(
                    game = sharedViewModel.game,
                    onExit = { navController.navigateUp() },
                    onReplay = { _ ->
                        navController.navigate(Game) {
                            popUpTo(Inspect) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}