package com.korn.portfolio.randomtrivia.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class Question(
    val type: String,
    val difficulty: Difficulty,
    val category: String,
    val question: String,
    @SerialName("correct_answer")
    val correctAnswer: String,
    @SerialName("incorrect_answers")
    val incorrectAnswers: List<String>
)
