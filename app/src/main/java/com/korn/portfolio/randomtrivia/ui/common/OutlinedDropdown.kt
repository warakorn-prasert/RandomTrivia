@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> OutlinedDropdown(
    selected: T,
    onSelect: (T) -> Unit,
    items: Collection<T>,
    modifier: Modifier = Modifier,
    toString: (T) -> String = { it.toString() },
    label: (@Composable () -> Unit)? = null,
    itemLeadingIcon: (@Composable (T) -> Unit)? = null,
    itemContent: @Composable (T) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocus by interactionSource.collectIsFocusedAsState()
    Column(modifier) {
        BasicTextField(
            value = toString(selected),
            onValueChange = {},
            modifier = if (label != null) Modifier.padding(top = 8.dp) else Modifier,
            enabled = true,
            readOnly = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            interactionSource = interactionSource,
            singleLine = false,
            decorationBox = @Composable { innerTextField ->
                OutlinedTextFieldDefaults.DecorationBox(
                    value = toString(selected),
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    label = label,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.run {
                                if (isFocus) KeyboardArrowUp
                                else KeyboardArrowDown
                            },
                            contentDescription = null
                        )
                    },
                    singleLine = false,
                    enabled = true,
                    isError = false,
                    interactionSource = interactionSource,
                    container = {
                        OutlinedTextFieldDefaults.ContainerBox(
                            enabled = true,
                            isError = false,
                            interactionSource = interactionSource,
                            colors = OutlinedTextFieldDefaults.colors(),
                            focusedBorderThickness = 2.dp,
                            unfocusedBorderThickness = 1.dp
                        )
                    }
                )
            }
        )

        val focusManager = LocalFocusManager.current
        DropdownMenu(
            expanded = isFocus,
            onDismissRequest = { focusManager.clearFocus() }
        ) {
            items.forEach {
                DropdownMenuItem(
                    text = { itemContent(it) },
                    onClick = {
                        focusManager.clearFocus()
                        onSelect(it)
                    },
                    leadingIcon = itemLeadingIcon?.run { { invoke(it) } }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedDropdownPreview() {
    RandomTriviaTheme {
        var selected: String? by remember { mutableStateOf(null) }
        OutlinedDropdown(
            selected = selected,
            onSelect = { selected = it },
            items = listOf("it1", "it2", "it3"),
            label = { Text("Label") }
        ) {
            Text(it ?: "")
        }
    }
}