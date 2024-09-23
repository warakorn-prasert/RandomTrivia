package com.korn.portfolio.randomtrivia.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "setting")

// save class name of ThemeData
val IS_DARK = stringPreferencesKey("isDark")
val SOURCE_COLOR = stringPreferencesKey("sourceColor")
val CONTRAST_LEVEL = stringPreferencesKey("contrastLevel")
// value of ThemeData.Custom
val IS_DARK_VALUE = booleanPreferencesKey("isDarkValue")
val SOURCE_COLOR_VALUE = intPreferencesKey("sourceColorValue")
val CONTRAST_LEVEL_VALUE = floatPreferencesKey("contrastLevelValue")