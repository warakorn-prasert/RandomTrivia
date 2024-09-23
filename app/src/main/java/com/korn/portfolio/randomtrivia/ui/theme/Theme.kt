package com.korn.portfolio.randomtrivia.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.SchemeContent
import com.korn.portfolio.randomtrivia.model.ContrastLevel
import com.korn.portfolio.randomtrivia.model.IsDark
import com.korn.portfolio.randomtrivia.model.SourceColor
import com.korn.portfolio.randomtrivia.ui.viewmodel.ThemeViewModel

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

@Suppress("FunctionName")
@Composable
fun RandomTriviaTheme(content: @Composable () -> Unit) {
    val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory)
    val context = LocalContext.current

    val isDark by themeViewModel.getIsDark(context).collectAsState(IsDark.Default)
    val contrastLevel by themeViewModel.getContrastLevel(context).collectAsState(ContrastLevel.Default)
    val sourceColor by themeViewModel.getSourceColor(context).collectAsState(SourceColor.Default)

    val isDarkValue = themeViewModel.getIsDarkValue(context, isDark)
    val contrastLevelValue = themeViewModel.getContrastLevelValue(context, contrastLevel)
    val sourceColorValue = themeViewModel.getSourceColorValue(sourceColor)

    val colorScheme =
        if (sourceColor == SourceColor.Wallpaper && Build.VERSION.SDK_INT >= 31) {
            when (val i = isDark) {
                is IsDark.Custom ->
                    if (i.value) dynamicDarkColorScheme(context)
                    else dynamicLightColorScheme(context)
                IsDark.Default ->
                    if (isSystemInDarkTheme()) dynamicDarkColorScheme(context)
                    else dynamicLightColorScheme(context)
            }
        } else {
            dynamicColorScheme(
                sourceColor = sourceColorValue,
                isDark = isDarkValue,
                contrastLevel = contrastLevelValue.toDouble()
            )
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                when (isDark) {
                    is IsDark.Custom -> isDarkValue
                    IsDark.Default -> isSystemInDarkTheme(context)
                }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}