@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.korn.portfolio.randomtrivia.ui.common.BottomBar
import com.korn.portfolio.randomtrivia.ui.navigation.MainNavigation

@Composable
fun MainScreen() {
    var currentRoute by remember { mutableStateOf(MainNavigation.CATEGORIES) }
    Scaffold(
        bottomBar = {
            BottomBar(
                selected = currentRoute,
                onClick = { currentRoute = it }
            )
        }
    ) { paddingValues ->
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = currentRoute.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(MainNavigation.CATEGORIES.route) {
                Categories()
            }
            composable(MainNavigation.PLAY.route) {

            }
            composable(MainNavigation.HISTORY.route) {

            }
        }
    }
}