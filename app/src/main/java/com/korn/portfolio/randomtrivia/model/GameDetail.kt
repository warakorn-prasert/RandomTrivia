package com.korn.portfolio.randomtrivia.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class GameDetail(
    val timestamp: Date,
    val totalTimeSecond: Int,
    @PrimaryKey
    val gameId: UUID = UUID.randomUUID()
)