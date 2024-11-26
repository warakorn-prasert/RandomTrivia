package com.korn.portfolio.randomtrivia.ui.previewdata

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.window.core.layout.WindowSizeClass

/**
 * Previews based on [androidx.compose.ui.tooling.preview.Devices].
 * ```
 * --------------------------------------------------------
 * |          |             (width, height)               |
 * |          |-------------------------------------------|
 * |          |      portrait       |        landscape    |
 * |----------|---------------------|---------------------|
 * | phone    | (compact, expanded) | (expanded, compact) |
 * | foldable | (medium,  medium)   | (expanded, medium)  |
 * | tablet   | (medium,  expanded) | (expanded, medium)  |
 * --------------------------------------------------------
 * ```
 *
 * Reference :
 *
 * - From [androidx.window.core.layout.WindowWidthSizeClass].
 * ```
 *         compact <         ≤ medium <         ≤ expanded
 *  width ----------- 600dp ------------ 840dp ------------
 * ```
 * - From [androidx.window.core.layout.WindowHeightSizeClass].
 * ```
 *          compact <         ≤ medium <         ≤ expanded
 *  height ----------- 400dp ------------ 900dp ------------
 * ```
 */
@Preview(name = "Phone", device = Devices.PHONE)
@Preview(
    name = "Phone - Landscape",
    device = "spec:id=reference_phone,shape=Normal,width=891,height=411,unit=dp,dpi=420"
)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(
    name = "Foldable - Landscape",
    device = "spec:id=reference_foldable,shape=Normal,width=841,height=673,unit=dp,dpi=420"
)
@Preview(
    name = "Tablet",
    device = "spec:id=reference_tablet,shape=Normal,width=800,height=1280,unit=dp,dpi=240"
)
@Preview(name = "Tablet - Landscape", device = Devices.TABLET)
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