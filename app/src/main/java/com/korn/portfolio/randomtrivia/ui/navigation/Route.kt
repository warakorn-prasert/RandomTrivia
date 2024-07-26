package com.korn.portfolio.randomtrivia.ui.navigation

interface Route {
    val route: String
        get() = (this as Enum<*>).name
}