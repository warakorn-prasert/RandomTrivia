package com.korn.portfolio.randomtrivia.repository

import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.network.model.Type

data class GameOption(
    val category: Category?,
    val difficulty: Difficulty?,
    val type: Type?,
    val amount: Int
)