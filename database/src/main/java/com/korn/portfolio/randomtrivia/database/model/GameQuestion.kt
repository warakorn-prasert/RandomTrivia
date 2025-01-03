package com.korn.portfolio.randomtrivia.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.entity.GameAnswer
import com.korn.portfolio.randomtrivia.database.model.entity.Question

data class GameQuestion(
    @Relation(
        parentColumn = "questionId",
        entityColumn = "id"
    )
    val question: Question,
    @Embedded
    val answer: GameAnswer,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category?,
)