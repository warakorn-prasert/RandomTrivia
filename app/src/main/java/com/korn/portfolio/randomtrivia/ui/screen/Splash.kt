@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import kotlinx.coroutines.delay

// Animation breakdown (sequential)
private const val ANIM_SHRINK_ICON = 500
private const val ANIM_EXPAND_NAME = 1000

// Overall time (sequential)
private const val PRE_TIME = 500
private const val ANIM_TIME = ANIM_SHRINK_ICON + ANIM_EXPAND_NAME
private const val WAIT_TIME = 500

@Composable
fun Splash(onDone: () -> Unit) {
    var start by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(PRE_TIME.toLong())
        start = true
        delay((ANIM_TIME + WAIT_TIME).toLong())
        onDone()
    }

    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedIcon(start)
        AnimatedVisibility(
            visible = start,
            enter = fadeIn(tween(ANIM_EXPAND_NAME, delayMillis = ANIM_SHRINK_ICON)) +
                    expandHorizontally(tween(ANIM_EXPAND_NAME, delayMillis = ANIM_SHRINK_ICON))
        ) {
            Column(Modifier.padding(start = 12.dp)) {
                Text(
                    text = "Random",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Trivia",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}

private const val PRIMARY_COLOR_WEIGHT = 0.5f  // affects contrast and brightness constants

// Ref. : https://en.wikipedia.org/wiki/Grayscale#Converting_color_to_grayscale
private const val R_GRAYSCALE = 0.299f
private const val G_GRAYSCALE = 0.587f
private const val B_GRAYSCALE = 0.114f

@Composable
private fun AnimatedIcon(start: Boolean) {
    val iconSize by animateDpAsState(
        targetValue = if (start) 56.dp else 160.dp,
        animationSpec = tween(ANIM_SHRINK_ICON)
    )

    // animate color matrix (grayscale + average with primary color)
    val anim by animateFloatAsState(
        targetValue = if (start) 1f else 0f,
        animationSpec = tween(ANIM_SHRINK_ICON)
    )

    val (pR, pG, pB) = MaterialTheme.colorScheme.primary
    val colorMatrix by remember {
        derivedStateOf {
            // grayscale
            val gR = anim * R_GRAYSCALE
            val gG = anim * G_GRAYSCALE
            val gB = anim * B_GRAYSCALE
            val srcR = 1f - anim + gR
            val srcG = 1f - anim + gG
            val srcB = 1f - anim + gB
            val mat1 = ColorMatrix(floatArrayOf(
                srcR, gG, gB, 0f, 0f,
                gR, srcG, gB, 0f, 0f,
                gR, gG, srcB, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))

            // average with primary color
            val weight = anim * PRIMARY_COLOR_WEIGHT
            val mat2 = ColorMatrix(floatArrayOf(
                1f - weight, 0f, 0f, 0f, pR * weight * 255f,
                0f, 1f - weight, 0f, 0f, pG * weight * 255f,
                0f, 0f, 1f - weight, 0f, pB * weight * 255f,
                0f, 0f, 0f, 1f, 0f
            ))

            // adjust brightness to be near primary color
            // (tested on primary colors from themes of app color (0xFFCAD49E) and M3Purple (0xFF6750A4))
            val target = 255f * (pR * R_GRAYSCALE + pG * G_GRAYSCALE + pB * B_GRAYSCALE)
            val contrast = 1f + anim * 2f  // brighter target (ex. dark theme's primary) -> brighter result
            val brightness = anim * -1f * target  // reduce brightness from contrast
            mat2.values[0] *= contrast
            mat2.values[6] *= contrast
            mat2.values[12] *= contrast
            mat2.values[4] += brightness
            mat2.values[9] += brightness
            mat2.values[14] += brightness

            // combine mat2 and mat1 before apply
            //  - transform : mat2 * (mat1 * rgbaVector)
            //  - to : mat21 * rgbaVector
            mat2.multiplyRgb(mat1)
        }
    }

    AdaptiveIcon(iconSize, colorMatrix)
}

// Ref. : https://gist.github.com/tkuenneth/ddf598663f041dc79960cda503d14448
@Composable
private fun AdaptiveIcon(size: Dp, colorMatrix: ColorMatrix) {
    val drawable = ResourcesCompat.getDrawable(
        LocalContext.current.resources,
        R.mipmap.ic_launcher,
        LocalContext.current.theme
    )!!
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth, drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "App icon",
        modifier = Modifier
            .size(size)
            .clip(CircleShape),
        colorFilter = ColorFilter.colorMatrix(colorMatrix)
    )
}

// Multiplication of top-left 3x3 matrices.
private fun ColorMatrix.multiplyRgb(other: ColorMatrix) = ColorMatrix(
    floatArrayOf(
        // top-left
        this.values[0] * other.values[0] + this.values[1] * other.values[5] + this.values[2] * other.values[10],
        // top-center
        this.values[0] * other.values[1] + this.values[1] * other.values[6] + this.values[2] * other.values[11],
        // top-right
        this.values[0] * other.values[2] + this.values[1] * other.values[7] + this.values[2] * other.values[12],

        this.values[3], this.values[4],

        // center-left
        this.values[5] * other.values[0] + this.values[6] * other.values[5] + this.values[7] * other.values[10],
        // center
        this.values[5] * other.values[1] + this.values[6] * other.values[6] + this.values[7] * other.values[11],
        // center-right
        this.values[5] * other.values[2] + this.values[6] * other.values[7] + this.values[7] * other.values[12],

        this.values[8], this.values[9],

        // bottom-left
        this.values[10] * other.values[0] + this.values[11] * other.values[5] + this.values[12] * other.values[10],
        // bottom-center
        this.values[10] * other.values[1] + this.values[11] * other.values[6] + this.values[12] * other.values[11],
        // bottom-right
        this.values[10] * other.values[2] + this.values[11] * other.values[7] + this.values[12] * other.values[12],

        this.values[13], this.values[14],
        this.values[15], this.values[16], this.values[17], this.values[18], this.values[19]
    )
)

@Preview
@Composable
private fun SplashPreview() {
    RandomTriviaTheme {
        Splash { }
    }
}