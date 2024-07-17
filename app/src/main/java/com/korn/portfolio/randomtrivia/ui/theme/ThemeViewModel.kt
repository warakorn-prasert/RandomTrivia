package com.korn.portfolio.randomtrivia.ui.theme

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
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
import com.korn.portfolio.randomtrivia.model.ContrastLevel
import com.korn.portfolio.randomtrivia.model.IsDark
import com.korn.portfolio.randomtrivia.model.SourceColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ThemeViewModel(initContext: Context) : ViewModel() {
    var isDark: IsDark by mutableStateOf(IsDark.Default)
        private set
    var sourceColor: SourceColor by mutableStateOf(SourceColor.Default)
        private set
    var contrastLevel: ContrastLevel by mutableStateOf(ContrastLevel.Default)
        private set

    init {
        viewModelScope.launch {
            loadIsDark(initContext)
            loadSourceColor(initContext)
            loadContrastLevel(initContext)
        }
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
            isDark = value
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
            sourceColor = value
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
            contrastLevel = value
        }
    }

    private suspend fun loadIsDark(context: Context) {
        isDark = withContext(Dispatchers.IO) {
            val str = context.dataStore.data
                .map { it[IS_DARK] }
                .first()
            when (str) {
                IsDark.Custom::class.simpleName -> {
                    val value: Boolean = context.dataStore.data
                        .map { it[IS_DARK_VALUE] ?: isSystemInDarkTheme(context) }
                        .first()
                    IsDark.Custom(value)
                }
                else -> IsDark.Default
            }
        }
    }

    private suspend fun loadSourceColor(context: Context) {
        sourceColor = withContext(Dispatchers.IO) {
            val str = context.dataStore.data
                .map { it[SOURCE_COLOR] }
                .first()
            when (str) {
                SourceColor.Custom::class.simpleName -> {
                    val value = context.dataStore.data
                        .map { it[SOURCE_COLOR_VALUE] ?: TriviaAppColor }
                        .first()
                    SourceColor.Custom(value)
                }

                SourceColor.Wallpaper::class.simpleName -> SourceColor.Wallpaper
                else -> SourceColor.Default
            }
        }
    }

    private suspend fun loadContrastLevel(context: Context) {
        contrastLevel = withContext(Dispatchers.IO) {
            val str = context.dataStore.data
                .map { it[CONTRAST_LEVEL] }
                .first()
            when (str) {
                ContrastLevel.Custom::class.simpleName -> {
                    val value = context.dataStore.data
                        .map { it[CONTRAST_LEVEL_VALUE] ?: getSystemContrast(context) }
                        .first()
                    ContrastLevel.Custom(value)
                }

                else -> ContrastLevel.Default
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as Application
                ThemeViewModel(application.applicationContext)
            }
        }
    }
}