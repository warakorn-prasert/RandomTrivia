package com.korn.portfolio.randomtrivia.model

data class GameOption(
    val category: Category,
    val difficulty: Difficulty,
    val amount: Int
)