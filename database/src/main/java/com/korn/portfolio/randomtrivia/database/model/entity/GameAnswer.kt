package com.korn.portfolio.randomtrivia.database.model.entity

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
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("categoryId"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("gameId"), Index("questionId"), Index("categoryId")],
    primaryKeys = ["gameId", "questionId"]
)
data class GameAnswer(
    val gameId: UUID,
    val questionId: UUID,
    val answer: String,
    val categoryId: UUID?  // Redundant to questionId, but less nested class.
)