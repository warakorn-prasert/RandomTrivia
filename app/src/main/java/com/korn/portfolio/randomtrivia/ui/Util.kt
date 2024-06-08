package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ToggleArrowIcon(expanded: Boolean) {
    Icon(
        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
        contentDescription = null
    )
}

@Composable
fun PlainTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    numberOnly: Boolean = false,
    roundness: Dp = 8.dp,
    leadingIcon: @Composable () -> Unit = {}
) {
    Row(modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        leadingIcon()
        Spacer(Modifier.width(4.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = if (numberOnly) KeyboardType.Number else KeyboardType.Text
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(Modifier
                    .height(IntrinsicSize.Min)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(roundness))
                    .clip(RoundedCornerShape(roundness))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    innerTextField()
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlainTextFieldPreview() {
    Column {
        PlainTextField("", {}) {
            Text("Prefix")
        }
        Spacer(Modifier.height(4.dp))
        PlainTextField("Something", {}) {
            Text("Prefix")
        }
    }
}

