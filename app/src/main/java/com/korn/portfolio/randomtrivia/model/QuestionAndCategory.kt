package com.korn.portfolio.randomtrivia.model

import androidx.room.Embedded
import androidx.room.Relation

data class QuestionAndCategory(
    @Embedded
    val question: Question,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: Category?
)