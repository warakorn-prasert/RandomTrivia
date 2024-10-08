@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.korn.portfolio.randomtrivia.R
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.ui.common.IconButtonWithText
import com.korn.portfolio.randomtrivia.ui.hhmmssFrom
import com.korn.portfolio.randomtrivia.ui.previewdata.getGame
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.ResultViewModel
import kotlinx.coroutines.delay

@Composable
fun Result(
    game: Game,
    onExit: () -> Unit,
    onReplay: (Game) -> Unit,
    onInspect: (Game) -> Unit
) {
    val viewModel: ResultViewModel = viewModel(factory = ResultViewModel.Factory(game))
    BackHandler(onBack = onExit)
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        topBar = {
            Box(
                modifier = Modifier
                    .padding(start = 4.dp)
                    .height(64.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButtonWithText(
                    onClick = { viewModel.exit(onExit) },
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Button to exit to main screen.",
                    text = "Exit"
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .height(80.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButtonWithText(
                    onClick = { onInspect(game) },
                    imageVector = Icons.Default.Search,
                    contentDescription = "Button to inspect previous game.",
                    text = "Inspect"
                )
                IconButtonWithText(
                    onClick = { viewModel.replay(onReplay) },
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Replay button.",
                    text = "Replay"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // blinking
            var animState by remember { mutableStateOf(false) }
            val duration = 300L
            val color by animateColorAsState(
                targetValue =
                    if (animState) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onBackground,
                animationSpec = tween(duration.toInt())
            )
            LaunchedEffect(Unit) {
                repeat(5) {
                    delay(duration)
                    animState = true
                    delay(duration)
                    animState = false
                }
            }

            Text(
                text = "${viewModel.score} / ${viewModel.maxScore}",
                color = color,
                style = MaterialTheme.typography.displayMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_timer),
                        contentDescription = "Timer icon",
                        tint = color
                    )
                    Text(
                        text = hhmmssFrom(viewModel.totalTimeSecond),
                        color = color
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ResultPreview() {
    RandomTriviaTheme {
        Result(
            game = getGame(42),
            onExit = {},
            onReplay = {},
            onInspect = {}
        )
    }
}