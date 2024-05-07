package com.korn.portfolio.randomtrivia.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.korn.portfolio.randomtrivia.model.QuestionAnswer

@Composable
fun GameScreen(showTopAppBar: MutableState<Boolean>) {
    var gameState: GameState by remember { mutableStateOf(GameState.Set) }
    when (val s = gameState) {
        is GameState.Set ->
            SettingScreen { questions ->
                gameState = GameState.Play(questions.map { QuestionAnswer(it, "") })
            }
        is GameState.Play ->
            PlayScreen(showTopAppBar, s.questions) {
                gameState = GameState.Result(it)
            }
        is GameState.Result ->
            ResultScreen(s.questionAnswers, {  })
    }
}