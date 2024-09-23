package com.korn.portfolio.randomtrivia.ui.navigation

import androidx.annotation.DrawableRes
import com.korn.portfolio.randomtrivia.R

enum class MainNavigation(
    val title: String,
    @DrawableRes val icon: Int
) : Route {
    CATEGORIES("Categories", R.drawable.ic_lists),
    PLAY("Play", R.drawable.ic_play),
    HISTORY("History", R.drawable.ic_history)
}