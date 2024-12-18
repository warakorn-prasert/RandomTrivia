@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private const val strokeWidthDp = 4

@Suppress("AnimateAsStateLabel")
@Composable
fun M3ProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val weight by animateFloatAsState(
        targetValue = progress.coerceIn(minimumValue = 0f, maximumValue = 1f),
        animationSpec = TweenSpec()
    )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(strokeWidthDp.dp)) {
            if (weight > 0f) StartBar(weight)
            if (weight < 1f) EndBar(1f - weight)
        }
        Box(
            Modifier
                .clip(CircleShape)
                .size(strokeWidthDp.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
private fun RowScope.StartBar(weight: Float) {
    Box(
        Modifier
            .clip(CircleShape)
            .weight(weight)
            .height(strokeWidthDp.dp)
            .background(MaterialTheme.colorScheme.primary)
    )
}

@Composable
private fun RowScope.EndBar(weight: Float) {
    Box(
        Modifier
            .clip(CircleShape)
            .weight(weight)
            .height(strokeWidthDp.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer)
    )
}

@Preview(showBackground = true)
@Composable
private fun M3ProgressBarPreview() {
    Column {
        M3ProgressBar(0f, Modifier.padding(4.dp))
        M3ProgressBar(0.005f, Modifier.padding(4.dp))
        M3ProgressBar(0.01f, Modifier.padding(4.dp))
        M3ProgressBar(0.1f, Modifier.padding(4.dp))
        M3ProgressBar(0.5f, Modifier.padding(4.dp))
        M3ProgressBar(0.9f, Modifier.padding(4.dp))
        M3ProgressBar(0.99f, Modifier.padding(4.dp))
        M3ProgressBar(0.995f, Modifier.padding(4.dp))
        M3ProgressBar(1f, Modifier.padding(4.dp))
    }
}