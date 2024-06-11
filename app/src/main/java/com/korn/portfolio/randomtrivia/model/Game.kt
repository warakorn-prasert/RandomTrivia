package com.korn.portfolio.randomtrivia.model

import androidx.room.Embedded
import androidx.room.Relation

data class Game(
    @Embedded
    val detail: GameDetail,
    @Relation(
        entity = GameAnswer::class,
        parentColumn = "gameId",
        entityColumn = "gameId",
    )
    val questions: List<GameQuestion>
)