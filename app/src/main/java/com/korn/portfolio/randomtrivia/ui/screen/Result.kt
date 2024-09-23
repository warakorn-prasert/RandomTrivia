@file:Suppress("FunctionName")

package com.korn.portfolio.randomtrivia.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.korn.portfolio.randomtrivia.ui.preview.getGame
import com.korn.portfolio.randomtrivia.ui.theme.RandomTriviaTheme
import com.korn.portfolio.randomtrivia.ui.viewmodel.ResultViewModel

@Composable
fun Result(
    game: Game,
    onExit: () -> Unit,
    onReplay: (Game) -> Unit,
) {
    val viewModel: ResultViewModel = viewModel(factory = ResultViewModel.Factory(game))
    Scaffold(
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
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButtonWithText(
                    onClick = { viewModel.enterInspect() },
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
            Text("${viewModel.score} / ${viewModel.maxScore}", style = MaterialTheme.typography.displayMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(R.drawable.ic_timer), "Timer icon")
                    Text(hhmmssFrom(viewModel.totalTimeSecond))
                }
            }
            if (viewModel.inspect)
                InspectDialog(
                    onDismissRequest = viewModel::exitInspect,
                    replayAction = { viewModel.replay(onReplay) },
                    game = viewModel.game
                )
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
            onReplay = {}
        )
    }
}