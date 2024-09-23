@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PaddedDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (show)
        Dialog(onDismissRequest = onDismissRequest) {
            Card(
                modifier = Modifier.widthIn(min = 280.dp, max = 560.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Box(Modifier.padding(24.dp)) {
                    CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleMedium) {
                        title()
                    }
                }
                Column(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 24.dp)
                ) {
                    content()
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    actions()
                }
            }
        }
}

@Preview
@Composable
private fun PaddedDialogPreview() {
    PaddedDialog(
        show = true,
        onDismissRequest = {},
        title = { Text("Title") },
        actions = {
            TextButton({}) { Text("CANCEL") }
            TextButton({}) { Text("ADD") }
        }
    ) {
        Text("Content")
    }
}