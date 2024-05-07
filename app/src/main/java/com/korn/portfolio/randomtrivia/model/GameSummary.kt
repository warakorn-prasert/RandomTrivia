package com.korn.portfolio.randomtrivia.model

data class GameSummary(
    val category: String,
    val difficulty: String,
    val total: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int
) {
    companion object {
        fun List<QuestionAnswer>.summary(): GameSummary {
            val category = first().question.category
            val difficulty = first().question.difficulty
            val total = size
            val correctAnswers = fold(0) { acc, it ->
                acc + if (it.answer == it.question.correctAnswer) 1 else 0
            }
            val incorrectAnswers = total - correctAnswers
            return GameSummary(category, difficulty, total, correctAnswers, incorrectAnswers)
        }
    }
}