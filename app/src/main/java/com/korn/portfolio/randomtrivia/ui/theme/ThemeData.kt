package com.korn.portfolio.randomtrivia.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "setting")

sealed interface IsDark {
    companion object {
        val TYPE_KEY = stringPreferencesKey("isDark")
        val VALUE_KEY = booleanPreferencesKey("isDarkValue")
    }

    data class Custom(val value: Boolean) : IsDark {
        companion object {
            const val TYPE_VALUE = "Custom"
        }
    }

    data object Default : IsDark {
        const val TYPE_VALUE = "Default"
    }
}

sealed interface SourceColor {
    companion object {
        val TYPE_KEY = stringPreferencesKey("sourceColor")
        val VALUE_KEY = intPreferencesKey("sourceColorValue")
    }

    data class Custom(val value: Int = TriviaAppColor) : SourceColor {
        companion object {
            const val TYPE_VALUE = "Custom"
        }
    }

    data object Default : SourceColor {
        const val TYPE_VALUE = "Default"
    }

    data object Wallpaper : SourceColor {
        const val TYPE_VALUE = "Wallpaper"
    }
}

sealed interface ContrastLevel {
    companion object {
        val TYPE_KEY = stringPreferencesKey("contrastLevel")
        val VALUE_KEY = floatPreferencesKey("contrastLevelValue")
    }

    data class Custom(val value: Float) : ContrastLevel {
        companion object {
            const val TYPE_VALUE = "Custom"
        }
    }

    data object Default : ContrastLevel {
        const val TYPE_VALUE = "Default"
    }
}