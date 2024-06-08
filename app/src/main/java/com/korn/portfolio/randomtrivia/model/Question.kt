package com.korn.portfolio.randomtrivia.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("categoryId"),
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("categoryId")]
)
data class Question(
    val question: String,
    val difficulty: Difficulty,
    val categoryId: Int,
    val correctAnswer: String,
    val incorrectAnswers: List<String>,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)