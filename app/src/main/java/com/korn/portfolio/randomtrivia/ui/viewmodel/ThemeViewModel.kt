package com.korn.portfolio.randomtrivia.ui.viewmodel

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.data.CONTRAST_LEVEL
import com.korn.portfolio.randomtrivia.data.CONTRAST_LEVEL_VALUE
import com.korn.portfolio.randomtrivia.data.IS_DARK
import com.korn.portfolio.randomtrivia.data.IS_DARK_VALUE
import com.korn.portfolio.randomtrivia.data.SOURCE_COLOR
import com.korn.portfolio.randomtrivia.data.SOURCE_COLOR_VALUE
import com.korn.portfolio.randomtrivia.data.dataStore
import com.korn.portfolio.randomtrivia.ui.theme.ContrastLevel
import com.korn.portfolio.randomtrivia.ui.theme.IsDark
import com.korn.portfolio.randomtrivia.ui.theme.SourceColor
import com.korn.portfolio.randomtrivia.ui.theme.TriviaAppColor
import com.korn.portfolio.randomtrivia.ui.theme.getSystemContrast
import com.korn.portfolio.randomtrivia.ui.theme.isSystemInDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThemeViewModel : ViewModel() {
    fun getIsDark(context: Context): Flow<IsDark> {
        val str = context.dataStore.data
            .map { it[IS_DARK] }
        val value = context.dataStore.data
            .map { it[IS_DARK_VALUE] ?: isSystemInDarkTheme(context) }
        return combineTransform(str, value) { s, v ->
            when (s) {
                IsDark.Custom::class.simpleName -> emit(IsDark.Custom(v))
                else -> emit(IsDark.Default)
            }
        }
    }

    fun getSourceColor(context: Context): Flow<SourceColor> {
        val str = context.dataStore.data
            .map { it[SOURCE_COLOR] }
        val value = context.dataStore.data
            .map { it[SOURCE_COLOR_VALUE] ?: TriviaAppColor }
        return combineTransform(str, value) { s, v ->
            when (s) {
                SourceColor.Custom::class.simpleName -> emit(SourceColor.Custom(v))
                SourceColor.Wallpaper::class.simpleName -> emit(SourceColor.Wallpaper)
                else -> emit(SourceColor.Default)
            }
        }
    }

    fun getContrastLevel(context: Context): Flow<ContrastLevel> {
        val str = context.dataStore.data
            .map { it[CONTRAST_LEVEL] }
        val value = context.dataStore.data
            .map { it[CONTRAST_LEVEL_VALUE] ?: getSystemContrast(context) }
        return combineTransform(str, value) { s, v ->
            when (s) {
                ContrastLevel.Custom::class.simpleName -> emit(ContrastLevel.Custom(v))
                else -> emit(ContrastLevel.Default)
            }
        }
    }

    fun getIsDarkValue(context: Context, isDark: IsDark): Boolean =
        when (val i = isDark) {
            is IsDark.Custom -> i.value
            IsDark.Default -> isSystemInDarkTheme(context)
        }

    fun getSourceColorValue(sourceColor: SourceColor): Int =
        when (val s = sourceColor) {
            is SourceColor.Custom -> s.value
            SourceColor.Default -> TriviaAppColor
            SourceColor.Wallpaper -> 0
        }

    fun getContrastLevelValue(context: Context, contrastLevel: ContrastLevel): Float =
        when (val c = contrastLevel) {
            is ContrastLevel.Custom -> c.value
            ContrastLevel.Default -> getSystemContrast(context)
        }

    fun setIsDark(context: Context, value: IsDark) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.dataStore.edit {
                    it[IS_DARK] = value::class.simpleName!!
                    if (value is IsDark.Custom)
                        it[IS_DARK_VALUE] = value.value
                }
            }
        }
    }

    fun setSourceColor(context: Context, value: SourceColor) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.dataStore.edit {
                    it[SOURCE_COLOR] = value::class.simpleName!!
                    if (value is SourceColor.Custom)
                        it[SOURCE_COLOR_VALUE] = value.value
                }
            }
        }
    }

    fun setContrastLevel(context: Context, value: ContrastLevel) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                context.dataStore.edit {
                    it[CONTRAST_LEVEL] = value::class.simpleName!!
                    if (value is ContrastLevel.Custom)
                        it[CONTRAST_LEVEL_VALUE] = value.value
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ThemeViewModel()
            }
        }
    }
}