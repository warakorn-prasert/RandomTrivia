@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun IconButtonWithText(
    onClick: () -> Unit,
    imageVector: ImageVector,
    contentDescription: String?,
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val color = ButtonDefaults.textButtonColors().run {
        if (enabled) contentColor
        else disabledContentColor
    }
    Row(
        modifier = modifier
            .let {
                if (enabled) it
                    .clip(CircleShape)
                    .clickable { onClick() }
                else it
            }
            .heightIn(40.dp)
            .padding(start = 12.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector, contentDescription, tint = color)
        Text(text, color = color, style = MaterialTheme.typography.labelLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun IconButtonWithTextEnabledPreview() {
    IconButtonWithText(
        onClick = {},
        imageVector = Icons.Default.PlayArrow,
        contentDescription = null,
        text = "Some text"
    )
}

@Preview(showBackground = true)
@Composable
private fun IconButtonWithTextDisabledPreview() {
    IconButtonWithText(
        onClick = {},
        imageVector = Icons.Default.PlayArrow,
        contentDescription = null,
        text = "Some text",
        enabled = false
    )
}