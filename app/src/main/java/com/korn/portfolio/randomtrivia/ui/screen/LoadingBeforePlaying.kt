@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.ui.common.GameFetchStatus
import com.korn.portfolio.randomtrivia.ui.common.IconButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.M3ProgressBar
import com.korn.portfolio.randomtrivia.ui.previewdata.getGame
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme

@Composable
fun LoadingBeforePlaying(
    progress: Float,
    statusText: String,
    fetchStatus: GameFetchStatus,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onCancel() }

    Column(modifier) {
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButtonWithText(
                onClick = { onCancel() },
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Button to cancel loading game.",
                text = "Cancel"
            )
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val infiniteTransition = rememberInfiniteTransition()
            val degree by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1000,
                        delayMillis = 300
                    ),
                    repeatMode = RepeatMode.Restart
                )
            )
            var displayDegree by remember { mutableFloatStateOf(0f) }
            if (progress < 1f || progress == 1f && degree != 0f)
                displayDegree = degree
            Icon(
                painter = painterResource(R.drawable.ic_android),
                contentDescription = "App icon",
                modifier = Modifier.size(108.dp).rotate(displayDegree),
                tint = MaterialTheme.colorScheme.primary
            )
            M3ProgressBar(
                progress = progress,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .width(216.dp)
            )
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelLarge) {
                when (fetchStatus) {
                    is GameFetchStatus.Error -> {
                        Text(
                            text = statusText,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        IconButtonWithText(
                            onClick = onRetry,
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Button to retry fetching new game.",
                            text = "Retry"
                        )
                    }
                    GameFetchStatus.Loading ->
                        Text(
                            text = statusText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    is GameFetchStatus.Success ->
                        Text(
                            text = statusText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                }
            }
        }
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    RandomTriviaTheme {
        Surface {
            LoadingBeforePlaying(
                progress = 0.3f,
                statusText = "Loading",
                fetchStatus = GameFetchStatus.Loading,
                onCancel = {},
                onRetry = {}
            )
        }
    }
}

@Preview
@Composable
private fun ErrorPreview() {
    RandomTriviaTheme {
        Surface {
            LoadingBeforePlaying(
                progress = 0.3f,
                statusText = "Display error message",
                fetchStatus = GameFetchStatus.Error("Error message"),
                onCancel = {},
                onRetry = {}
            )
        }
    }
}

@Preview
@Composable
private fun SuccessPreview() {
    RandomTriviaTheme {
        Surface {
            LoadingBeforePlaying(
                progress = 1f,
                statusText = "Success message",
                fetchStatus = GameFetchStatus.Success(getGame(0)),
                onCancel = {},
                onRetry = {}
            )
        }
    }
}