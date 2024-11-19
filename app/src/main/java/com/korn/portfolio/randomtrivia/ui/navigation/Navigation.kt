package com.korn.portfolio.randomtrivia.ui.navigation

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.navigation.NavType
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

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
data object Play : BottomNav {
    override val title = "Play"
    override val icon = R.drawable.ic_play

    @Serializable data object Setting
    @Serializable data class Loading(
        val onlineMode: Boolean,
        val settings: List<SerializableGameSetting>
    )
}

@Serializable
data object History : BottomNav {
    override val title = "History"
    override val icon = R.drawable.ic_history
}

@Serializable
data object Game {
    @Serializable data object Play
    @Serializable data object Result
}
@Serializable data object Inspect
@Serializable data object About

@Parcelize
@Serializable
data class SerializableGameSetting(
    val categoryId: Int?,
    val categoryName: String?,
    val difficulty: Difficulty?,
    val amount: Int
) : Parcelable

fun List<GameSetting>.serialized() = map {
    SerializableGameSetting(
        categoryId = it.category?.id,
        categoryName = it.category?.name,
        difficulty = it.difficulty,
        amount = it.amount
    )
}

fun List<SerializableGameSetting>.deserialized() = map {
    GameSetting(
        category =
        if (it.categoryId == null || it.categoryName == null) null
        else Category(
            name = it.categoryName,
            id = it.categoryId
        ),
        difficulty = it.difficulty,
        amount = it.amount
    )
}

val GameSettingType = object : NavType<List<SerializableGameSetting>>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): List<SerializableGameSetting> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            return bundle.getParcelableArrayList(key, SerializableGameSetting::class.java)!!.toList()
        else
            @Suppress("DEPRECATION")
            return bundle.getParcelableArrayList<SerializableGameSetting>(key)!!.toList()
    }

    override fun parseValue(value: String): List<SerializableGameSetting> {
        return Json.decodeFromString(value)
    }

    override fun serializeAsValue(value: List<SerializableGameSetting>): String {
        return Json.encodeToString(value)
    }

    override fun put(bundle: Bundle, key: String, value: List<SerializableGameSetting>) {
        bundle.putParcelableArrayList(key, ArrayList(value))
    }
}