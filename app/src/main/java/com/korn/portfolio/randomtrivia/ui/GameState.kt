package com.korn.portfolio.randomtrivia.ui

import com.korn.portfolio.randomtrivia.model.QuestionAnswer

sealed interface GameState {
    data object Set : GameState
    data class Play(val questions: List<QuestionAnswer>) : GameState
    data class Result(val questionAnswers: List<QuestionAnswer>) : GameState
}