package com.korn.portfolio.randomtrivia.model

import com.korn.portfolio.randomtrivia.ui.theme.TriviaAppColor

sealed interface IsDark {
    data class Custom(val value: Boolean) : IsDark
    data object Default : IsDark
}

sealed interface SourceColor {
    data class Custom(val value: Int = TriviaAppColor) : SourceColor
    data object Default : SourceColor
    data object Wallpaper : SourceColor
}

sealed interface ContrastLevel {
    data class Custom(val value: Float) : ContrastLevel
    data object Default : ContrastLevel
}