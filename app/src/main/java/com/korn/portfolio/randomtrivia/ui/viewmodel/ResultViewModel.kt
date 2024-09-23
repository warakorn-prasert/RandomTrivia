package com.korn.portfolio.randomtrivia.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.korn.portfolio.randomtrivia.database.model.Game

class ResultViewModel(val game: Game) : ViewModel() {
    val totalTimeSecond = game.detail.totalTimeSecond
    val score = game.questions.fold(0) { score, question ->
        if (question.answer.answer == question.question.correctAnswer)
            score + 1
        else
            score
    }
    val maxScore = game.questions.size

    var inspect by mutableStateOf(false)
        private set
    fun enterInspect() {
        inspect = true
    }
    fun exitInspect() {
        inspect = false
    }

    fun exit(action: () -> Unit) {
        action()
    }

    fun replay(action: (Game) -> Unit) {
        action(game)
    }

    class Factory(private val game: Game) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ResultViewModel(game) as T
        }
    }
}