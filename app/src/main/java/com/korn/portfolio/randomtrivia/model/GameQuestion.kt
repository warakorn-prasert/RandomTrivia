package com.korn.portfolio.randomtrivia.model

import androidx.room.Embedded
import androidx.room.Relation

data class GameQuestion(
    @Relation(
        entity = Question::class,
        parentColumn = "questionId",
        entityColumn = "id"
    )
    val question: QuestionAndCategory,
    @Embedded
    val answer: GameAnswer
    // TODO : Split `QuestionAndCategory`
)