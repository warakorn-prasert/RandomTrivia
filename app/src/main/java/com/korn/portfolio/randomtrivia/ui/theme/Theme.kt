package com.korn.portfolio.randomtrivia.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.ThemeViewModel
import com.korn.portfolio.randomtrivia.ui.viewmodel.dynamicColorScheme

@Suppress("FunctionName")
@Composable
fun RandomTriviaTheme(content: @Composable () -> Unit) {
    val themeViewModel: ThemeViewModel = viewModel()

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

//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = colorScheme.primary.toArgb()
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
//                when (isDark) {
//                    is IsDark.Custom -> isDarkValue
//                    IsDark.Default -> isSystemInDarkTheme(context)
//                }
//        }
//    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}