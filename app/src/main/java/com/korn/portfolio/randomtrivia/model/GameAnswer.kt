package com.korn.portfolio.randomtrivia.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.UUID

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = GameDetail::class,
            parentColumns = arrayOf("gameId"),
            childColumns = arrayOf("gameId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Question::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("questionId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId"), Index("questionId")],
    primaryKeys = ["gameId", "questionId"]
)
data class GameAnswer(
    val gameId: UUID,
    val questionId: UUID,
    val answer: String,
)