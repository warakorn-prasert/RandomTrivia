package com.korn.portfolio.randomtrivia.ui.navigation

import androidx.annotation.DrawableRes
import com.korn.portfolio.randomtrivia.R
import kotlinx.serialization.Serializable

@Serializable
sealed interface BottomNav {
    val title: String
    @get:DrawableRes
    val icon: Int
}

sealed interface SubNav

@Serializable
data object Categories : BottomNav {
    override val title = "Categories"
    override val icon = R.drawable.ic_lists

    @Serializable data object Default : SubNav
    @Serializable data class Questions(val categoryId: Int) : SubNav
}

@Serializable
data object Play : BottomNav {
    override val title = "Play"
    override val icon = R.drawable.ic_play

    @Serializable data object Setting
    @Serializable data object Loading
    @Serializable data object Playing
    @Serializable data object Result
}

@Serializable
data object History : BottomNav {
    override val title = "History"
    override val icon = R.drawable.ic_history

    @Serializable data object Default
    @Serializable data object Replay
    @Serializable data object Result
}

@Serializable data object Inspect