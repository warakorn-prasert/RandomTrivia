@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.ui.navigation.MainNavigation
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

@Composable
fun BottomBar(
    selected: MainNavigation,
    onClick: (MainNavigation) -> Unit
) {
    BottomAppBar(
        tonalElevation = 0.dp
    ) {
        MainNavigation.entries.forEach {
            NavigationBarItem(
                selected = selected == it,
                onClick = { onClick(it) },
                icon = {
                    Icon(
                        painter = painterResource(it.icon),
                        contentDescription = "${it.title} menu icon"
                    )
                },
                label = {
                    Text(it.title)
                }
            )
        }
    }
}

@Preview
@Composable
private fun BottomBarPreview() {
    RandomTriviaTheme {
        var mainNavigation by remember { mutableStateOf(MainNavigation.CATEGORIES) }
        BottomBar(
            selected = mainNavigation,
            onClick = { mainNavigation = it }
        )
    }
}