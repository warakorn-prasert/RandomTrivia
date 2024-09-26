package com.korn.portfolio.randomtrivia.ui.viewmodel

import android.annotation.SuppressLint
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.SchemeContent
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun isSystemInDarkTheme(context: Context): Boolean {
    val uiMode = context.resources.configuration.uiMode
    return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
}

/**
 * GitHub : https://github.com/material-components/material-components-android/blob/master/lib/java/com/google/android/material/color/DynamicColors.java
 * @see com.google.android.material.color.DynamicColors.getSystemContrast
 **/
fun getSystemContrast(context: Context): Float {
    val uiModeManager = context.getSystemService(Context.UI_MODE_SERVICE) as UiModeManager
    return if (Build.VERSION.SDK_INT >= 34) uiModeManager.contrast else 0.0f
}

/**
 * GitHub : https://github.com/material-components/material-components-android/blob/master/lib/java/com/google/android/material/color/utilities/MaterialDynamicColors.java
 * @see com.google.android.material.color.utilities.MaterialDynamicColors
 **/
@SuppressLint("RestrictedApi")
fun dynamicColorScheme(
    sourceColor: Int,
    isDark: Boolean,
    contrastLevel: Double
): ColorScheme {
    // For extracting color from image, see DynamicColorsOptions.Builder.setContentBasedSource
    val scheme: DynamicScheme = SchemeContent(Hct.fromInt(sourceColor), isDark, contrastLevel)
    return MaterialDynamicColors().let {
        ColorScheme(
            primary = Color(it.primary().getArgb(scheme)),
            onPrimary = Color(it.onPrimary().getArgb(scheme)),
            primaryContainer = Color(it.primaryContainer().getArgb(scheme)),
            onPrimaryContainer = Color(it.onPrimaryContainer().getArgb(scheme)),
            inversePrimary = Color(it.inversePrimary().getArgb(scheme)),
            secondary = Color(it.secondary().getArgb(scheme)),
            onSecondary = Color(it.onSecondary().getArgb(scheme)),
            secondaryContainer = Color(it.secondaryContainer().getArgb(scheme)),
            onSecondaryContainer = Color(it.onSecondaryContainer().getArgb(scheme)),
            tertiary = Color(it.tertiary().getArgb(scheme)),
            onTertiary = Color(it.onTertiary().getArgb(scheme)),
            tertiaryContainer = Color(it.tertiaryContainer().getArgb(scheme)),
            onTertiaryContainer = Color(it.onTertiaryContainer().getArgb(scheme)),
            error = Color(it.error().getArgb(scheme)),
            onError = Color(it.onError().getArgb(scheme)),
            errorContainer = Color(it.errorContainer().getArgb(scheme)),
            onErrorContainer = Color(it.onErrorContainer().getArgb(scheme)),
            background = Color(it.background().getArgb(scheme)),
            onBackground = Color(it.onBackground().getArgb(scheme)),
            surface = Color(it.surface().getArgb(scheme)),
            onSurface = Color(it.onSurface().getArgb(scheme)),
            surfaceVariant = Color(it.surfaceVariant().getArgb(scheme)),
            onSurfaceVariant = Color(it.onSurfaceVariant().getArgb(scheme)),
            inverseSurface = Color(it.inverseSurface().getArgb(scheme)),
            inverseOnSurface = Color(it.inverseOnSurface().getArgb(scheme)),
            outline = Color(it.outline().getArgb(scheme)),
            outlineVariant = Color(it.outlineVariant().getArgb(scheme)),
            scrim = Color(it.scrim().getArgb(scheme)),
            surfaceBright = Color(it.surfaceBright().getArgb(scheme)),
            surfaceDim = Color(it.surfaceDim().getArgb(scheme)),
            surfaceContainer = Color(it.surfaceContainer().getArgb(scheme)),
            surfaceContainerHigh = Color(it.surfaceContainerHigh().getArgb(scheme)),
            surfaceContainerHighest = Color(it.surfaceContainerHighest().getArgb(scheme)),
            surfaceContainerLow = Color(it.surfaceContainerLow().getArgb(scheme)),
            surfaceContainerLowest = Color(it.surfaceContainerLowest().getArgb(scheme)),
            surfaceTint = Color(it.surfaceTint().getArgb(scheme))
        )
    }
}

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

    private val customNavBarColor = MutableStateFlow<Int?>(null)

    fun getSystemBarData(context: Context): Flow<Triple<Boolean, Int, Int>> =
        combine(
            getIsDark(context),
            getSourceColor(context),
            getContrastLevel(context),
            customNavBarColor,
        ) { isDark, sourceColor, contrastLevel, customNavBarColor ->
            val isDarkValue = getIsDarkValue(context, isDark)
            val colorScheme = dynamicColorScheme(
                getSourceColorValue(sourceColor),
                isDarkValue,
                getContrastLevelValue(context, contrastLevel).toDouble()
            )
            val statusBarColor = colorScheme.surface.toArgb()
            val navBarColor = customNavBarColor ?: statusBarColor
            Triple(isDarkValue, statusBarColor, navBarColor)
        }

    fun enableCustomNavBarColor(color: Color) {
        viewModelScope.launch {
            customNavBarColor.emit(color.toArgb())
        }
    }

    fun disableCustomNavBarColor() {
        viewModelScope.launch {
            customNavBarColor.emit(null)
        }
    }
}