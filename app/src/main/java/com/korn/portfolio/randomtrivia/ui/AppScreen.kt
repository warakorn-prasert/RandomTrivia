@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private enum class Route(val icon: ImageVector) {
    CATEGORY(Icons.Default.Info),
    QUESTION(Icons.Default.Info),
    GAME(Icons.Default.Info);

    val route: String = name
}

@Composable
fun AppScreen() {
    val navController = rememberNavController()
    Scaffold(Modifier.fillMaxSize(),
        bottomBar = { CustomBottomBar(navController) },
        content = { paddingValues ->
            NavHost(navController, Route.CATEGORY.route) {
                composable(Route.CATEGORY.route) {
                    CategoryScreen(paddingValues)
                }
                composable(Route.QUESTION.route) {
                    QuestionScreen(paddingValues)
                }
                composable(Route.GAME.route) {
                    GameScreen(paddingValues)
                }
            }
        }
    )
}

@Composable
private fun CustomBottomBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    BottomAppBar {
        Route.entries.forEach {
            NavigationBarItem(
                selected = currentRoute == it.route,
                onClick = { navController.navigate(it.route) },
                icon = { Icon(it.icon, null) },
                label = { Text(it.route) }
            )
        }
    }
}

@Preview
@Composable
private fun BottomBarPreview() {
    CustomBottomBar(rememberNavController())
}