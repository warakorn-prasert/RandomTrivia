@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.theme

import android.annotation.SuppressLint
import android.os.Build
import android.graphics.Color as ColorTool
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.SchemeContent
import com.google.android.material.color.utilities.TonalPalette
import com.korn.portfolio.randomtrivia.model.ContrastLevel
import com.korn.portfolio.randomtrivia.model.IsDark
import com.korn.portfolio.randomtrivia.model.SourceColor
import com.korn.portfolio.randomtrivia.ui.viewmodel.ThemeViewModel
import kotlin.math.roundToInt
import kotlin.random.Random

private fun colorFromHue(hue: Float): Int {
    val inc = (((hue % 60) / 60) * 255).roundToInt()
    val dec = (255 - inc)
    return when (hue % 360) {
        in 0f..<60f -> Color(red = 255, green = inc, blue = 0)
        in 60f..<120f -> Color(red = dec, green = 255, blue = 0)
        in 120f..<180f -> Color(red = 0, green = 255, blue = inc)
        in 180f..<240f -> Color(red = 0, green = dec, blue = 255)
        in 240f..<300f -> Color(red = inc, green = 0, blue = 255)
        in 300f..<360f -> Color(red = 255, green = 0, blue = dec)
        else -> Color.Red
    }.toArgb()
}

@Preview
@Composable
private fun DynamicColorPreview() {
    val themeViewModel: ThemeViewModel = viewModel(factory = ThemeViewModel.Factory)
    val context = LocalContext.current

    val isDark by themeViewModel.getIsDark(context).collectAsState(IsDark.Default)
    val contrastLevel by themeViewModel.getContrastLevel(context).collectAsState(ContrastLevel.Default)
    val sourceColor by themeViewModel.getSourceColor(context).collectAsState(SourceColor.Default)

    val isDarkValue = themeViewModel.getIsDarkValue(context, isDark)
    val contrastLevelValue = themeViewModel.getContrastLevelValue(context, contrastLevel)
    val sourceColorValue = themeViewModel.getSourceColorValue(sourceColor)

    RandomTriviaTheme {
        Surface(Modifier.fillMaxSize()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TextButton(
                        onClick = { themeViewModel.setIsDark(context, IsDark.Custom(!isDarkValue)) },
                        content = { Text("Toggle dark") }
                    )
                    TextButton(
                        onClick = {
                            themeViewModel.setIsDark(context, IsDark.Default)
                            themeViewModel.setContrastLevel(context, ContrastLevel.Default)
                            themeViewModel.setSourceColor(context, SourceColor.Default)
                        },
                        content = { Text("System default") }
                    )
                    TextButton(
                        onClick = {
                            themeViewModel.setSourceColor(context, SourceColor.Wallpaper)
                        },
                        enabled = Build.VERSION.SDK_INT >= 31,
                        content = { Text("Color from Wallpaper") }
                    )
                }
                ContrastPicker(
                    contrastLevel = contrastLevelValue,
                    saveAction = { themeViewModel.setContrastLevel(context, ContrastLevel.Custom(it)) }
                )
                HorizontalColorBox(
                    prefix = "Source color",
                    color = Color(sourceColorValue)
                )
                SourceColorPicker(
                    sourceColor = sourceColorValue,
                    saveAction = { themeViewModel.setSourceColor(context, SourceColor.Custom(it)) }
                )
                ColorSchemeDisplay(
                    sourceColor = sourceColorValue,
                    isDark = isDarkValue,
                    contrastLevel = contrastLevelValue
                )
            }
        }
    }
}

@Composable
private fun ContrastPicker(
    contrastLevel: Float,
    saveAction: (Float) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Contrast (system = ${getSystemContrast(LocalContext.current)})")
        RadioButton(
            selected = (contrastLevel * 10).roundToInt() == 0,
            onClick = { saveAction(0f) }
        )
        Text("0")
        RadioButton(
            selected = (contrastLevel * 10).roundToInt() == 5,
            onClick = { saveAction(0.5f) }
        )
        Text("0.5")
        RadioButton(
            selected = (contrastLevel * 10).roundToInt() == 10,
            onClick = { saveAction(1f) }
        )
        Text("1")
    }
}

@Composable
private fun SourceColorPicker(
    sourceColor: Int,
    saveAction: (Int) -> Unit
) {
    var tab by remember { mutableIntStateOf(0) }
    TabRow(selectedTabIndex = tab) {
        Tab(selected = tab == 0, onClick = { tab = 0 }) {
            Text("Hue")
        }
        Tab(selected = tab == 1, onClick = { tab = 1 }) {
            Text("RGB")
        }
    }
    if (tab == 0) HuePicker(sourceColor, saveAction)
    else RgbPicker(sourceColor, saveAction)
}

@Composable
private fun HuePicker(
    sourceColor: Int,
    saveAction: (Int) -> Unit
) {
    val hue = FloatArray(3).let {
        ColorTool.colorToHSV(sourceColor, it)
        it[0]
    }
    Text("Hue ${hue.roundToInt()}")
    Row {
        repeat(360 * 2) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .background(Color(colorFromHue(it / 2f))),
                contentAlignment = Alignment.Center
            ) {
                if (hue.roundToInt() == (it / 2f).roundToInt()) {
                    Box(Modifier.background(Color.Black).size(8.dp))
                }
            }
        }
    }
    Slider(
        value = hue,
        onValueChange = { saveAction(colorFromHue(it)) },
        valueRange = 0f..359f,
        steps = 360 * 2 - 1
    )
    Button({
        saveAction(colorFromHue(Random.nextFloat() * 359))
    }) {
        Text("Random")
    }
}

@Composable
private fun RgbPicker(
    sourceColor: Int,
    saveAction: (Int) -> Unit
) {
    sourceColor.run {
        Text("R $red, G $green, B $blue")
        Slider(
            value = red.toFloat(),
            onValueChange = { saveAction(ColorTool.argb(alpha, it.roundToInt(), green, blue)) },
            valueRange = 0f..255f,
            steps = 255
        )
        Slider(
            value = green.toFloat(),
            onValueChange = { saveAction(ColorTool.argb(alpha, red, it.roundToInt(), blue)) },
            valueRange = 0f..255f,
            steps = 255
        )
        Slider(
            value = blue.toFloat(),
            onValueChange = { saveAction(ColorTool.argb(alpha, red, green, it.roundToInt())) },
            valueRange = 0f..255f,
            steps = 255
        )
        Button({
            saveAction(ColorTool.argb(
                alpha,
                Random.nextInt(from = 0, until = 255),
                Random.nextInt(from = 0, until = 255),
                Random.nextInt(from = 0, until = 255)
            ))
        }) {
            Text("Random")
        }
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun ColumnScope.ColorSchemeDisplay(
    sourceColor: Int,
    isDark: Boolean,
    contrastLevel: Float
) {
    val scheme: DynamicScheme = SchemeContent(Hct.fromInt(sourceColor), isDark, contrastLevel.toDouble())
    var tab by remember { mutableIntStateOf(0) }
    TabRow(selectedTabIndex = tab) {
        Tab(selected = tab == 0, onClick = { tab = 0 }) {
            Text("Key Palette")
        }
        Tab(selected = tab == 1, onClick = { tab = 1 }) {
            Text("Scheme")
        }
    }
    if (tab == 0) {
        Text("Tonal palette")
        Column(Modifier.weight(1f)) {
            Palette("P", scheme.primaryPalette)
            Palette("S", scheme.secondaryPalette)
            Palette("T", scheme.tertiaryPalette)
            Palette("N", scheme.neutralPalette)
            Palette("NV", scheme.neutralVariantPalette)
        }
        Text("Key color (independent to dark mode)")
        MaterialDynamicColors().let {
            Column {
                Row {
                    HorizontalColorBox("Primary", Color(it.primaryPaletteKeyColor().getArgb(scheme)))
                    HorizontalColorBox("Secondary", Color(it.secondaryPaletteKeyColor().getArgb(scheme)))
                    HorizontalColorBox("Tertiary", Color(it.tertiaryPaletteKeyColor().getArgb(scheme)))
                }
                Row {
                    HorizontalColorBox("Neutral", Color(it.neutralPaletteKeyColor().getArgb(scheme)))
                    HorizontalColorBox("Neutral Variant", Color(it.neutralVariantPaletteKeyColor().getArgb(scheme)))
                }
            }
        }
    } else {
        Scheme(dynamicColorScheme(sourceColor, isDark, contrastLevel.toDouble()))
    }
}

@SuppressLint("RestrictedApi")
@Composable
private fun ColumnScope.Palette(initial: String, palette: TonalPalette) {
    Row(Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(initial, Modifier.weight(1f))
            repeat(11) {
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(palette.tone(it * 10)))
                )
            }
        }
    }
}

@Composable
private fun HorizontalColorBox(prefix: String, color: Color) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Max)
            .padding(start = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(prefix)
        Box(
            Modifier
                .padding(start = 8.dp)
                .width(42.dp)
                .height(24.dp)
                .background(color)
        ) {
            if (color.alpha == 0f) {
                Text("Wallpaper")
            }
        }
    }
}

@Composable
private fun Scheme(scheme: ColorScheme) {
    Column {
        Row(Modifier.weight(1f)) {
            BoxWithText("Primary", scheme.primary)
            BoxWithText("On primary", scheme.onPrimary)
            BoxWithText("Primary container", scheme.primaryContainer)
            BoxWithText("On primary container", scheme.onPrimaryContainer)
        }
        Row(Modifier.weight(1f)) {
            BoxWithText("Secondary", scheme.secondary)
            BoxWithText("On secondary", scheme.onSecondary)
            BoxWithText("Secondary container", scheme.secondaryContainer)
            BoxWithText("On secondary container", scheme.onSecondaryContainer)
        }
        Row(Modifier.weight(1f)) {
            BoxWithText("Tertiary", scheme.tertiary)
            BoxWithText("On tertiary", scheme.onTertiary)
            BoxWithText("Tertiary container", scheme.tertiaryContainer)
            BoxWithText("On tertiary container", scheme.onTertiaryContainer)
        }
        Row(Modifier.weight(1f)) {
            BoxWithText("Error", scheme.error)
            BoxWithText("On error", scheme.onError)
            BoxWithText("Error container", scheme.errorContainer)
            BoxWithText("On error container", scheme.onErrorContainer)
        }
        Row(Modifier.weight(1f)) {
            BoxWithText("Background", scheme.background)
            BoxWithText("On background", scheme.onBackground)
            BoxWithText("Surface", scheme.surface)
            BoxWithText("On surface", scheme.onSecondary)
        }
        Row(Modifier.weight(1f)) {
            BoxWithText("Surface variant", scheme.surfaceVariant)
            BoxWithText("On surface variant", scheme.onSurfaceVariant)
            BoxWithText("Outline", scheme.outline)
        }
    }
}

@Composable
private fun RowScope.BoxWithText(text: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .weight(1f)
            .fillMaxHeight()
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color(
                red = 1 - color.red,
                green = 1 - color.green,
                blue = 1 - color.blue,
            )
        )
    }
}