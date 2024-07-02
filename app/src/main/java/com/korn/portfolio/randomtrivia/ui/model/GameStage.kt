package com.korn.portfolio.randomtrivia.ui.model

enum class GameStage {
    SETTING,
    PLAYING,
    RESULT;

    val route: String = name.lowercase()
}