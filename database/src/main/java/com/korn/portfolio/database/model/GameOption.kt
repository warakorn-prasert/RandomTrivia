package com.korn.portfolio.database.model

import com.korn.portfolio.database.model.entity.Category

data class GameOption(
    val category: Category,
    val difficulty: Difficulty,
    val amount: Int
)