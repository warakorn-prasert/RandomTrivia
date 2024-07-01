package com.korn.portfolio.randomtrivia.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(val icon: ImageVector) {
    CATEGORIES(Icons.Default.Info),
    PLAY(Icons.Default.Face),
    HISTORY(Icons.Default.Menu);

    val route: String = name.lowercase()
}