@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenDialog(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    if (expanded) {
        BasicAlertDialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(title, style = MaterialTheme.typography.titleMedium)
                        },
                        navigationIcon = {
                            IconButton({ onDismissRequest() }) {
                                Icon(Icons.Default.Close, "Close setting menu")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(Modifier.padding(paddingValues)) {
                    content()
                }
            }
        }
    }
}