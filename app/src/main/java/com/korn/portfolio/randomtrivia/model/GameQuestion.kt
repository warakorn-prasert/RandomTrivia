package com.korn.portfolio.randomtrivia.model

import androidx.room.Embedded
import androidx.room.Relation

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