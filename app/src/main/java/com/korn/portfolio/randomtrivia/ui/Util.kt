@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@Composable
fun <T> CustomDropdown(
    selection: MutableState<T>,
    options: List<T>,
    modifier: Modifier = Modifier,
    toString: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier.wrapContentSize().clickable { expanded = !expanded },
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(Modifier.padding(4.dp).padding(start = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(toString(selection.value))
            ToggleArrowIcon(expanded)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            content =  {
                options.forEach {
                    DropdownMenuItem(
                        text = { Text(toString(it)) },
                        onClick = {
                            selection.value = it
                            expanded = false
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun HorizontalDividerWithText(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth(), Alignment.Center) {
        HorizontalDivider()
        Text(text, Modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
        )
    }
}