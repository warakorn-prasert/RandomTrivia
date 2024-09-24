@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

@Composable
fun TextChip(
    selected: Boolean,
    onClick: () -> Unit,
    text: String
) {
    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelLarge.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(text) },
            minHeight = 32.dp,
            horizontalPadding = 16.dp,
            cornerSize = 8.dp
        )
    }
}

@Composable
fun IconChip(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = icon,
        minHeight = 32.dp,
        horizontalPadding = 8.dp,
        cornerSize = 4.dp
    )
}

@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    horizontalPadding: Dp,
    minHeight: Dp,
    cornerSize: Dp
) {
    val contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    val containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    CompositionLocalProvider(LocalContentColor provides contentColor) {
        Box(
            modifier = Modifier
                .heightIn(min = minHeight)
                .let {
                    if (selected)
                        it
                    else
                        it.border(
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(cornerSize)
                        )
                }
                .clip(RoundedCornerShape(cornerSize))
                .background(containerColor)
                .clickable(onClick = onClick)
                .padding(horizontal = horizontalPadding),
            contentAlignment = Alignment.Center
        ) {
            label()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChipPreview() {
    RandomTriviaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(true, false).forEach { selected ->
                TextChip(
                    selected = selected,
                    onClick = {},
                    text = "Label"
                )
                IconChip(
                    selected = selected,
                    onClick = {},
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}