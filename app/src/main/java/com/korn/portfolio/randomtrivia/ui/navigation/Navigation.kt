package com.korn.portfolio.randomtrivia.ui.navigation

import androidx.annotation.DrawableRes
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.ui.common.WrappedGameSerializer
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
sealed interface BottomNav {
    val title: String
    @get:DrawableRes
    val icon: Int
}

@Serializable
data object Categories : BottomNav {
    override val title = "Categories"
    override val icon = R.drawable.ic_lists

    @Serializable data object Default
    @Serializable data class Questions(val categoryId: Int)
}

@Serializable
data object PrePlay : BottomNav {
    override val title = "Play"
    override val icon = R.drawable.ic_play

    @Serializable data object Setting
    @Serializable data class Loading(
        val onlineMode: Boolean,
        val settings: List<GameSetting>
    )
}

@Serializable
data object History : BottomNav {
    override val title = "History"
    override val icon = R.drawable.ic_history
}

@Serializable
data class Play(val wrappedGame: WrappedGame = WrappedGame()) {
    companion object SubNav {
        @Serializable data object Playing
        @Serializable data class Result(val wrappedGame: WrappedGame = WrappedGame())
    }
}

@Serializable data class Inspect(val wrappedGame: WrappedGame = WrappedGame())
@Serializable data object About

// Fix : Custom serializers declared directly on a class field via @Serializable(with = ...) is currently not supported by safe args for both custom types and third-party types.
@Serializable(with = WrappedGameSerializer::class)
data class WrappedGame(
    val game: Game = Game(GameDetail(Date(), 0), emptyList())
)