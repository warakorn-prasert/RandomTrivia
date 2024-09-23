@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.ui.common.GameFetchStatus
import com.korn.portfolio.randomtrivia.ui.common.IconButtonWithText
import com.korn.portfolio.randomtrivia.ui.common.M3ProgressBar
import com.korn.portfolio.randomtrivia.ui.previewdata.getCategory
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.GameSetting
import com.korn.portfolio.randomtrivia.ui.viewmodel.LoadingBeforePlayingViewModel

@Composable
fun LoadingBeforePlaying(
    onlineMode: Boolean,
    settings: List<GameSetting>,
    onCancel: () -> Unit,
    onStart: (Game) -> Unit
) {
    val viewModel: LoadingBeforePlayingViewModel = viewModel(factory = LoadingBeforePlayingViewModel.Factory(onlineMode, settings))
    BackHandler(true) {
        viewModel.cancel(onCancel)
    }
    viewModel.startGameWhenReady(onStart)
    Column(Modifier.fillMaxSize()) {
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
            Icon(
                painter = painterResource(R.drawable.ic_android),
                contentDescription = "App icon",
                modifier = Modifier.size(108.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            M3ProgressBar(
                progress = viewModel.progress,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .width(216.dp)
            )
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.labelLarge) {
                when (viewModel.fetchStatus) {
                    is GameFetchStatus.Error -> {
                        Text(
                            text = viewModel.statusText,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        IconButtonWithText(
                            onClick = { viewModel.fetch() },
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Button to retry fetching new game.",
                            text = "Retry"
                        )
                    }
                    GameFetchStatus.Loading ->
                        Text(
                            text = viewModel.statusText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    is GameFetchStatus.Success ->
                        Text(
                            text = viewModel.statusText,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun LoadingBeforePlayingPreview() {
    RandomTriviaTheme {
        Surface {
            LoadingBeforePlaying(
                onlineMode = true,
                settings = listOf(GameSetting(getCategory(0), Difficulty.HARD, 5)),
                onCancel = {},
                onStart = {}
            )
        }
    }
}