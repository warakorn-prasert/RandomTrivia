@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.korn.portfolio.randomtrivia.ui.navigation.BottomNav
import com.korn.portfolio.randomtrivia.ui.navigation.Categories
import com.korn.portfolio.randomtrivia.ui.navigation.History
import com.korn.portfolio.randomtrivia.ui.navigation.Play
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

// Ref. : https://developer.android.com/develop/ui/compose/navigation#bottom-nav

private val bottomNavs = listOf(Categories, Play, History)

fun NavController.navigateBottomNav(bottomNav: BottomNav) {
    navigate(bottomNav) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        restoreState = true
        // fix: 2nd back press from non-start bottom nav destination doesn't exit app
        launchSingleTop = true
    }
}

@Composable
fun BottomBar(navController: NavController) {
    BottomAppBar(
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        bottomNavs.forEach { bottomNav ->
            val selected = currentDestination?.hierarchy?.any { it.hasRoute(bottomNav::class) } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) navController.navigateBottomNav(bottomNav)
                },
                icon = {
                    Icon(
                        painter = painterResource(bottomNav.icon),
                        contentDescription = "${bottomNav.title} menu icon"
                    )
                },
                label = {
                    Text(bottomNav.title)
                }
            )
        }
    }
}

@Preview
@Composable
private fun BottomBarPreview() {
    RandomTriviaTheme {
        BottomBar(rememberNavController())
    }
}