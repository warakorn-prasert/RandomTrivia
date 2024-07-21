@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.model.IsDark
import com.korn.portfolio.randomtrivia.model.SourceColor
import com.korn.portfolio.randomtrivia.ui.common.FullScreenDialog
import com.korn.portfolio.randomtrivia.ui.common.OutlinedDropdown
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.theme.ThemeViewModel

@Composable
fun SettingDialog(expanded: Boolean, onDismissRequest: () -> Unit) {
    val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory)
    val context = LocalContext.current
    FullScreenDialog(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        title = "Setting"
    ) {
        Spacer(Modifier.height(24.dp))
        val menuModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
        DarkThemeMenu(
            isDark = themeViewModel.isDark,
            setIsDark = { themeViewModel.setIsDark(context, it) },
            modifier = menuModifier
        )
        ThemeColorMenu(
            sourceColor = themeViewModel.sourceColor,
            setSourceColor = { themeViewModel.setSourceColor(context, it) },
            modifier = menuModifier
        )
    }
}

@Composable
private fun DarkThemeMenu(
    isDark: IsDark,
    setIsDark: (IsDark) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Dark theme")
        OutlinedDropdown<String>(
            selected = when (isDark) {
                IsDark.Custom(false) -> "Light"
                IsDark.Custom(true) -> "Dark"
                else -> "Auto"
            },
            onSelect = {
                setIsDark(
                    when (it) {
                        "Light" -> IsDark.Custom(false)
                        "Dark" -> IsDark.Custom(true)
                        else -> IsDark.Default
                    }
                )
            },
            items = listOf("Auto", "Light", "Dark"),
            itemContent = { Text(it) }
        )
    }
}

@Composable
private fun ThemeColorMenu(
    sourceColor: SourceColor,
    setSourceColor: (SourceColor) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Dark theme")
        OutlinedDropdown<String>(
            selected = when (sourceColor) {
                SourceColor.Wallpaper -> "Wallpaper-based"
                else -> "App color"
            },
            onSelect = {
                setSourceColor(
                    when (it) {
                        "Wallpaper-based" -> SourceColor.Wallpaper
                        else -> SourceColor.Default
                    }
                )
            },
            items = listOf("App color", "Wallpaper-based"),
            itemContent = { Text(it) }
        )
    }
}

@Preview
@Composable
fun SettingDialogPreview() {
    val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory)
    RandomTriviaTheme(
        themeViewModel.isDark,
        themeViewModel.sourceColor,
        themeViewModel.contrastLevel
    ) {
        SettingDialog(
            expanded = true,
            onDismissRequest = {}
        )
    }
}