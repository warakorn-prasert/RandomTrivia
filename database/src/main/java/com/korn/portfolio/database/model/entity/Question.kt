package com.korn.portfolio.database.model.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.korn.portfolio.database.model.Difficulty
import java.util.UUID

@Entity(
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("categoryId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.SET_NULL
    )],
    indices = [Index("categoryId")]
)
data class Question(
    val question: String,
    val difficulty: Difficulty,
    val categoryId: UUID?,
    val correctAnswer: String,
    val incorrectAnswers: List<String>,
    @PrimaryKey
    val id: UUID = UUID.randomUUID()
)