package com.korn.portfolio.randomtrivia.model

import androidx.room.Embedded
import androidx.room.Ignore
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
) {
    @Ignore
    val easy: Int = questions.count { it.difficulty == Difficulty.EASY }
    @Ignore
    val medium: Int = questions.count { it.difficulty == Difficulty.MEDIUM }
    @Ignore
    val hard: Int = questions.count { it.difficulty == Difficulty.HARD }
}
