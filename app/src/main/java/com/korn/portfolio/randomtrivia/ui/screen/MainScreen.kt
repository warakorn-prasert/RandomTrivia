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
import com.korn.portfolio.randomtrivia.ui.viewmodel.CategoriesViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.LoadingBeforePlayingViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.QuestionsViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.SettingBeforePlayingViewModel
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
                    val viewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory)
                    val categories by viewModel.categories.collectAsState(emptyList())
                    Categories(
                        categories = categories,
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
                    val categoryId = backStackEntry.toRoute<Categories.Questions>().categoryId
                    val viewModel: QuestionsViewModel = viewModel(factory = QuestionsViewModel.Factory(categoryId))
                    Questions(
                        categoryName = viewModel.categoryName,
                        questions = viewModel.questions,
                        onExit = { navController.navigateUp() },
                        onAboutClick = { navController.navigate(About) },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
            navigation<Play>(startDestination = Play.Setting) {
                composable<Play.Setting> {
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
                    val viewModel: LoadingBeforePlayingViewModel = viewModel(
                        factory = LoadingBeforePlayingViewModel.Factory(
                            onlineMode = onlineMode,
                            settings = settings.deserialized(),
                            onDone = { game ->
                                sharedViewModel.game = game
                                navController.navigate(Game) {
                                    popUpTo(Play.Setting)
                                }
                            }
                        )
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