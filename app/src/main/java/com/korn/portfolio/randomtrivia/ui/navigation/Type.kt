package com.korn.portfolio.randomtrivia.ui.navigation

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.navigation.NavType
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.ArrayList

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