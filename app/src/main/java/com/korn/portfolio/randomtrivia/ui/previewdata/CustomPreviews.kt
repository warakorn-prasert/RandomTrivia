package com.korn.portfolio.randomtrivia.ui.previewdata

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowSizeClass

/**
 * Previews based on [androidx.compose.ui.tooling.preview.Devices]
 * and default [androidx.compose.ui.tooling.preview.Preview].
 * ```
 * -------------------------------------------------------------------------------
 * |             |               (width, height)             |                   |
 * |             |-------------------------------------------|     Reference     |
 * |             |       portrait      |      landscape      |                   |
 * |-------------|---------------------|---------------------|-------------------|
 * | phone Small | (compact, medium)   | (medium, compact)   | Samsung Galaxy A6 |
 * | phone       | (compact, medium)   | (expanded, compact) | Default @Preview  |
 * | phone Large | (compact, expanded) | (expanded, medium)  | Devices.PHONE     |
 * | foldable    | (medium,  medium)   | (expanded, medium)  | Devices.FOLDABLE  |
 * | tablet      | (medium,  expanded) | (expanded, medium)  | Devices.TABLET    |
 * -------------------------------------------------------------------------------
 * ```
 *
 * Reference :
 *
 * - From [androidx.window.core.layout.WindowWidthSizeClass].
 * ```
 * width : compact < 600dp ≤ medium < 840dp ≤ expanded
 * ```
 * - From [androidx.window.core.layout.WindowHeightSizeClass].
 * ```
 * height : compact < 400dp ≤ medium < 900dp ≤ expanded
 * ```
 */
@Preview(name = "Phone Small", device = "spec:width=392dp,height=805dp,dpi=294")
@Preview(name = "Phone Small - Landscape", device = "spec:width=805dp,height=392dp,dpi=294")
@Preview(name = "Phone - Landscape", device = "spec:width=850dp,height=392dp,dpi=440")
@Preview(name = "Phone Large", device = "spec:width=411dp,height=911dp,dpi=420")
@Preview(name = "Phone Large - Landscape", device = "spec:width=911dp,height=411dp,dpi=420")
@Preview(name = "Foldable", device = "spec:width=673dp,height=841dp,dpi=420")
@Preview(name = "Tablet", device = "spec:width=800dp,height=1280dp,dpi=240")
@Preview(name = "Tablet - Landscape", device = "spec:width=1280dp,height=800dp,dpi=240")
annotation class PreviewWindowSizes

/**
 * For previewing composable only.
 * Meant to replace `currentWindowAdaptiveInfo()`.
 * Because its `currentWindowSize()` returns zero in `@Preview`.
 */
@Composable
fun windowSizeForPreview(): WindowSizeClass {
    val config = LocalConfiguration.current
    val w = config.screenWidthDp.toFloat()
    val h = config.screenHeightDp.toFloat()
    return WindowSizeClass.compute(w, h)
}