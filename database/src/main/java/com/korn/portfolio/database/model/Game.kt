package com.korn.portfolio.database.model

import androidx.room.Embedded
import androidx.room.Relation
import com.korn.portfolio.database.model.entity.GameAnswer
import com.korn.portfolio.database.model.entity.GameDetail

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