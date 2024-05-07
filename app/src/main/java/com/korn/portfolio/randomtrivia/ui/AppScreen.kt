package com.korn.portfolio.randomtrivia.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.korn.portfolio.randomtrivia.R
import kotlinx.coroutines.launch

enum class Destination(val title: String) {
    HOME("Home"),
    CATEGORIES("Categories"),
    GAME("Play"),
    PAST_GAMES("Past games"),
    ABOUT("About");

    val route: String = this.toString()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = rememberNavController()
    var currentDestination by remember { mutableStateOf(Destination.HOME) }
    val showTopAppBar = remember { mutableStateOf(true) }
    ModalNavigationDrawer(
        drawerContent = {
            DrawerMenu(navigate = {
                navController.navigate(it.route) {
                    // In stack, replace current destination with this destination.
                    // So that back-pressed and gesture navigation will always exit the app.
                    popUpTo(currentDestination.route) {
                        inclusive = true
                    }
                }
                currentDestination = it
                scope.launch { drawerState.close() }
            })
        },
        modifier = Modifier,
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        scrimColor = DrawerDefaults.scrimColor,
        content = {
            Scaffold(
                topBar = {
                    AnimatedVisibility(
                        visible = showTopAppBar.value,
                        enter = slideInVertically(initialOffsetY = { -it }),
                        exit = slideOutVertically(targetOffsetY = { -it }),
                        content = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = currentDestination.title,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                modifier = Modifier.shadow(12.dp),
                                navigationIcon = {
                                    IconButton(
                                        onClick = { scope.launch { drawerState.open() } },
                                        content = { Icon(Icons.Default.Menu, null) }
                                    )
                                },
                                actions = {
                                    Image(
                                        painter = painterResource(R.drawable.question_mark),
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp)
                                    )
                                }
                            )
                        }
                    )
                },
                content = { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Destination.HOME.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(Destination.HOME.route) {
                            HomeScreen()
                        }
                        composable(Destination.CATEGORIES.route) {
                            CategoryScreen()
                        }
                        composable(Destination.GAME.route) {
                            GameScreen(showTopAppBar)
                        }
                        composable(Destination.PAST_GAMES.route) {
                            PastGamesScreen(showTopAppBar)
                        }
                        composable(Destination.ABOUT.route) {
                            AboutScreen()
                        }
                    }
                }
            )
        }
    )
}

@Composable
private fun DrawerMenu(navigate: (Destination) -> Unit) {
    ModalDrawerSheet {
        val modifier = Modifier.align(Alignment.CenterHorizontally)

        Text("Random Trivia", modifier)
        HorizontalDivider(modifier)

        Destination.entries.filter { it != Destination.ABOUT }.forEach {
            Text(it.title, modifier.clickable { navigate(it) })
        }

        Spacer(modifier.weight(1f))
        Destination.ABOUT.let {
            Text(it.title, modifier.clickable { navigate(it) })
        }
    }
}

@Preview
@Composable
private fun DrawerMenuPreview() {
    DrawerMenu(navigate = {})
}

@Preview
@Composable
private fun AppScreenPreview() {
    AppScreen()
}