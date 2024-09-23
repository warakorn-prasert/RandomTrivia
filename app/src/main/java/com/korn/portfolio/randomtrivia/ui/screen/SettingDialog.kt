@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.model.IsDark
import com.korn.portfolio.randomtrivia.model.SourceColor
import com.korn.portfolio.randomtrivia.ui.common.OutlinedDropdown
import com.korn.portfolio.randomtrivia.ui.theme.M3Purple
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.ThemeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingDialog(onDismissRequest: () -> Unit) {
    val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory)
    val context = LocalContext.current
    val isDark by themeViewModel.getIsDark(context).collectAsState(IsDark.Default)
    val sourceColor by themeViewModel.getSourceColor(context).collectAsState(SourceColor.Default)
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("Setting", style = MaterialTheme.typography.titleMedium)
                    },
                    navigationIcon = {
                        IconButton({ onDismissRequest() }) {
                            Icon(Icons.Default.Close, "Close setting menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                Spacer(Modifier.height(24.dp))
                val menuModifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                DarkThemeMenu(
                    isDark = isDark,
                    setIsDark = { themeViewModel.setIsDark(context, it) },
                    modifier = menuModifier
                )
                ThemeColorMenu(
                    sourceColor = sourceColor,
                    setSourceColor = { themeViewModel.setSourceColor(context, it) },
                    modifier = menuModifier
                )
            }
        }
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
    val options = mutableListOf("App color", "Preset 1").apply {
        if (Build.VERSION.SDK_INT >= 31) add(1,"Wallpaper-based")
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Theme color")
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
            itemContent = { Text(it) }
        )
    }
}

@Preview
@Composable
fun SettingDialogPreview() {
    RandomTriviaTheme {
        SettingDialog(onDismissRequest = {})
    }
}