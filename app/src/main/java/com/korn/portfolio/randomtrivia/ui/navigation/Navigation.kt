package com.korn.portfolio.randomtrivia.ui.navigation

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.navigation.NavType
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.ui.common.WrappedGameSerializer
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.ArrayList
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

val GameSettingType = object : NavType<List<GameSetting>>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): List<GameSetting> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return bundle.getParcelableArrayList(key, GameSetting::class.java)!!.toList()
        else
            @Suppress("DEPRECATION")
            return bundle.getParcelableArrayList<GameSetting>(key)!!.toList()
        // If GameSetting is not Parcelable, use this.
        // return bundle.getStringArrayList(key)!!.map(Json::decodeFromString)
    }

    override fun parseValue(value: String): List<GameSetting> {
        return Json.decodeFromString(Uri.decode(value))
    }

    override fun serializeAsValue(value: List<GameSetting>): String {
        return Uri.encode(Json.encodeToString(value))
    }

    override fun put(bundle: Bundle, key: String, value: List<GameSetting>) {
        bundle.putParcelableArrayList(key, ArrayList(value))
        // If GameSetting is not Parcelable, use this.
        // bundle.putStringArrayList(key, ArrayList(value.map(Json::encodeToString)))
    }
}

val WrappedGameType = object : NavType<WrappedGame>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): WrappedGame {
        return Json.decodeFromString(bundle.getString(key)!!)
    }

    override fun parseValue(value: String): WrappedGame {
        return Json.decodeFromString(Uri.decode(value))
    }

    override fun serializeAsValue(value: WrappedGame): String {
        return Uri.encode(Json.encodeToString(value))
    }

    override fun put(bundle: Bundle, key: String, value: WrappedGame) {
         bundle.putString(key, Json.encodeToString(value))
    }
}