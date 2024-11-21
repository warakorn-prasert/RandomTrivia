@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

@Composable
fun FetchStatusBar(
    fetchStatus: FetchStatus,
    onRetry: () -> Unit
) {
    if (fetchStatus !is FetchStatus.Success)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (fetchStatus == FetchStatus.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size((48f / 3).dp),
                        strokeWidth = (4f / 3).dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Loading new categories.",
                        style = MaterialTheme.typography.labelLarge
                    )
                } else if (fetchStatus is FetchStatus.Error) {
                    Text(fetchStatus.message, style = MaterialTheme.typography.labelLarge)
                    IconButtonWithText(
                        onClick = onRetry,
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry button for fetching categories.",
                        text = "Retry"
                    )
                }
            }
        }
}

@Preview
@Composable
private fun FetchStatusBarPreview() {
    RandomTriviaTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FetchStatusBar(
                fetchStatus = FetchStatus.Loading,
                onRetry = {}
            )
            FetchStatusBar(
                fetchStatus = FetchStatus.Error("Some error message"),
                onRetry = {}
            )
        }
    }
}