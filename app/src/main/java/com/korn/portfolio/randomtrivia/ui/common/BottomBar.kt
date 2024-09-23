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
import com.korn.portfolio.randomtrivia.ui.navigation.Categories
import com.korn.portfolio.randomtrivia.ui.navigation.History
import com.korn.portfolio.randomtrivia.ui.navigation.Play
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

// Ref. : https://developer.android.com/develop/ui/compose/navigation#bottom-nav

private val bottomNavs = listOf(Categories, Play, History)

@Composable
fun BottomBar(navController: NavController) {
    BottomAppBar(
        tonalElevation = 0.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        bottomNavs.forEach { bottomNav ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.hasRoute(bottomNav::class) } == true,
                onClick = {
                    navController.navigate(bottomNav) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
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