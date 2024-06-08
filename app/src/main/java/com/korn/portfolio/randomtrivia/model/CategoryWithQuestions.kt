package com.korn.portfolio.randomtrivia.model

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithQuestions(
    @Embedded
    val category: Category,
    @Relation(
        entity = Question::class,
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val questions: List<Question>

)
