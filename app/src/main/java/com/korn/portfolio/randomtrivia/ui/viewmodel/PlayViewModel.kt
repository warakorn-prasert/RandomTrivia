package com.korn.portfolio.randomtrivia.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import java.util.Date

// TODO : Remove Category.downloadable (Seems like it is not used.)

// TODO : Remove categoryId property from GameAnswer (redundant)

val Category?.displayName: String
    get() = this?.name ?: "Random"

val Difficulty?.displayName: String
    get() = when (this) {
        Difficulty.EASY -> "Easy"
        Difficulty.MEDIUM -> "Medium"
        Difficulty.HARD -> "Hard"
        null -> "Random"
    }

class PlayViewModel : ViewModel() {
    var onlineMode = false
    var settings = emptyList<GameSetting>()
    var game = Game(GameDetail(Date(), 0), emptyList())
}