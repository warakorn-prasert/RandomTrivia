package com.korn.portfolio.randomtrivia.database.model

import com.korn.portfolio.randomtrivia.database.model.entity.Category

data class GameOption(
    val category: Category,
    val difficulty: Difficulty,
    val amount: Int
)