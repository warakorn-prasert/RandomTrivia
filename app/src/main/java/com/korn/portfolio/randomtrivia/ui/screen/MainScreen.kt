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
import com.korn.portfolio.randomtrivia.ui.navigation.Play
import com.korn.portfolio.randomtrivia.ui.navigation.GameSettingType
import com.korn.portfolio.randomtrivia.ui.navigation.WrappedGameType
import com.korn.portfolio.randomtrivia.ui.navigation.History
import com.korn.portfolio.randomtrivia.ui.navigation.Inspect
import com.korn.portfolio.randomtrivia.ui.navigation.PrePlay
import com.korn.portfolio.randomtrivia.ui.navigation.WrappedGame
import com.korn.portfolio.randomtrivia.ui.viewmodel.CategoriesViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
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
            navigation<PrePlay>(startDestination = PrePlay.Setting) {
                composable<PrePlay.Setting> {
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
                            navController.navigate(PrePlay.Loading(onlineMode, settings))
                        },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
                composable<PrePlay.Loading>(
                    typeMap = mapOf(typeOf<List<GameSetting>>() to GameSettingType)
                ) { backStackEntry ->
                    requestFullScreen()
                    val (onlineMode, settings) = backStackEntry.toRoute<PrePlay.Loading>()
                    val viewModel: LoadingBeforePlayingViewModel = viewModel(
                        factory = LoadingBeforePlayingViewModel.Factory(
                            onlineMode = onlineMode,
                            settings = settings,
                            onDone = { game ->
                                navController.navigate(Play(WrappedGame(game))) {
                                    popUpTo(PrePlay.Setting)
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
            navigation<Play>(
                startDestination = Play.SubNav.Playing,
                typeMap = mapOf(typeOf<WrappedGame>() to WrappedGameType)
            ) {
                composable<Play.SubNav.Playing> { backStackEntry ->
                    requestFullScreen()
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry<Play>()
                    }
                    val inputGame = parentEntry.toRoute<Play>().wrappedGame.game
                    Playing(
                        game = inputGame,
                        onExit = { navController.navigateUp() },
                        onSubmit = { game ->
                            sharedViewModel.saveGame(game)
                            navController.navigate(Play.SubNav.Result(WrappedGame(game))) {
                                popUpTo<Play.SubNav.Playing> { inclusive = true }
                            }
                        }
                    )
                }
                composable<Play.SubNav.Result>(
                    typeMap = mapOf(typeOf<WrappedGame>() to WrappedGameType)
                ) { backStackEntry ->
                    requestFullScreen()
                    val inputGame = backStackEntry.toRoute<Play.SubNav.Result>().wrappedGame.game
                    Result(
                        game = inputGame,
                        onExit = { navController.navigateUp() },
                        onReplay = { game ->
                            navController.navigate(Play(WrappedGame(game))) {
                                popUpTo<Play.SubNav.Result> { inclusive = true }
                            }
                        },
                        onInspect = { game ->
                            navController.navigate(Inspect(WrappedGame(game))) {
                                popUpTo<Play.SubNav.Result> { inclusive = true }
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
                        navController.navigate(Play(WrappedGame(game)))
                    },
                    onInspect = { game ->
                        navController.navigate(Inspect(WrappedGame(game)))
                    },
                    onAboutClick = { navController.navigate(About) },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            composable<Inspect>(
                typeMap = mapOf(typeOf<WrappedGame>() to WrappedGameType)
            ) { backStackEntry ->
                requestFullScreen()
                val inputGame = backStackEntry.toRoute<Inspect>().wrappedGame.game
                Inspect(
                    game = inputGame,
                    onExit = { navController.navigateUp() },
                    onReplay = { game ->
                        navController.navigate(Play(WrappedGame(game))) {
                            popUpTo<Inspect> { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}