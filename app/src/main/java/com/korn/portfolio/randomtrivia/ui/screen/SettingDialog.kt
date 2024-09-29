@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import android.os.Build
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.ui.common.OutlinedDropdown
import com.korn.portfolio.randomtrivia.ui.common.PaddedDialog
import com.korn.portfolio.randomtrivia.ui.theme.IsDark
import com.korn.portfolio.randomtrivia.ui.theme.M3Purple
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.theme.SourceColor
import com.korn.portfolio.randomtrivia.ui.viewmodel.ThemeViewModel

@Composable
fun SettingDialog(onDismissRequest: () -> Unit) {
    val themeViewModel: ThemeViewModel = viewModel()
    val context = LocalContext.current
    val isDark: IsDark by themeViewModel.getIsDark(context).collectAsState(IsDark.Default)
    val sourceColor: SourceColor by themeViewModel.getSourceColor(context).collectAsState(SourceColor.Default)
    PaddedDialog(
        show = true,
        onDismissRequest = onDismissRequest,
        title = {
            IconButton(
                onClick = onDismissRequest,
                content = { Icon(Icons.Default.Close, "Close setting menu") },
                modifier = Modifier.offset(x = (-12).dp)
            )
            Text("Setting", Modifier.offset(x = (-12).dp))
        },
        actions = {}
    ) {
        DarkThemeMenu(
            isDark = isDark,
            setIsDark = { themeViewModel.setIsDark(context, it) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(Modifier.height(16.dp))
        ThemeColorMenu(
            sourceColor = sourceColor,
            setSourceColor = { themeViewModel.setSourceColor(context, it) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun DarkThemeMenu(
    isDark: IsDark,
    setIsDark: (IsDark) -> Unit,
    modifier: Modifier = Modifier
) {
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
        modifier = modifier,
        label = { Text("Dark Theme") },
        itemContent = { Text(it) }
    )
}

@Composable
private fun ThemeColorMenu(
    sourceColor: SourceColor,
    setSourceColor: (SourceColor) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = mutableListOf("App color", "Preset 1").apply {
        if (Build.VERSION.SDK_INT >= 31) add(1,"Wallpaper-based")
    }
    OutlinedDropdown<String>(
        selected = when (val s = sourceColor) {
            SourceColor.Wallpaper -> "Wallpaper-based"
            is SourceColor.Custom -> when (s.value) {
                M3Purple -> "Preset 1"
                else -> "App color"
            }
            else -> "App color"
        },
        onSelect = {
            setSourceColor(
                when (it) {
                    "Wallpaper-based" -> SourceColor.Wallpaper
                    "Preset 1" -> SourceColor.Custom(M3Purple)
                    else -> SourceColor.Default
                }
            )
        },
        items = options,
        modifier = modifier,
        label = { Text("Theme color") },
        itemContent = { Text(it) }
    )
}

@Preview
@Composable
fun SettingDialogPreview() {
    RandomTriviaTheme {
        SettingDialog(onDismissRequest = {})
    }
}