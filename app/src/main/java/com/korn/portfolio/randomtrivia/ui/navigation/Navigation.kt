package com.korn.portfolio.randomtrivia.ui.navigation

import androidx.annotation.DrawableRes
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.ui.common.WrappedGameSerializer
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import kotlinx.serialization.Serializable
import java.util.Date
import kotlin.reflect.KClass

@Serializable
sealed interface TopLevelDestination {
    val label: String
    @get:DrawableRes val icon: Int
    val contentDescription: String
    val screens: List<KClass<*>>

    companion object {
        val entries = listOf(Categories, PrePlay, History)
    }

    @Serializable
    data object Categories : TopLevelDestination {
        override val label = "Categories"
        override val icon = R.drawable.ic_lists
        override val contentDescription = "Categories menu icon"
        override val screens = listOf(Categories::class, Questions::class)

        @Serializable
        data object Categories
        @Serializable
        data class Questions(val categoryId: Int)
    }

    @Serializable
    data object PrePlay : TopLevelDestination {
        override val label = "Play"
        override val icon = R.drawable.ic_play
        override val contentDescription = "Play menu icon"
        override val screens = listOf(Setting::class, Loading::class)

        @Serializable
        data object Setting
        @Serializable
        data class Loading(val onlineMode: Boolean, val settings: List<GameSetting>)
    }

    @Serializable
    data object History : TopLevelDestination {
        override val label = "History"
        override val icon = R.drawable.ic_history
        override val contentDescription = "History menu icon"
        override val screens = listOf(History::class)
    }

    // Top-level destinations that don't appear in navigation UI, don't extend TopLevelDestination.

    @Serializable
    data class Play(val wrappedGame: WrappedGame = WrappedGame()) {
        companion object Screen {
            @Serializable
            data object Playing
            @Serializable
            data class Result(val wrappedGame: WrappedGame = WrappedGame())
        }
    }

    @Serializable
    data class Inspect(val wrappedGame: WrappedGame = WrappedGame())
    @Serializable
    data object About
}

// Fix : Custom serializers declared directly on a class field via @Serializable(with = ...) is currently not supported by safe args for both custom types and third-party types.
@Serializable(with = WrappedGameSerializer::class)
data class WrappedGame(
    val game: Game = Game(GameDetail(Date(), 0), emptyList())
)