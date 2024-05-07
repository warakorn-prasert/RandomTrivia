package com.korn.portfolio.randomtrivia.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Question(
    @PrimaryKey
    val question: String,
    val type: String,
    val difficulty: String,
    val category: String,
    @ColumnInfo(CORRECT_ANSWER)
    @SerialName(CORRECT_ANSWER)
    val correctAnswer: String,
    @ColumnInfo(INCORRECT_ANSWERS)
    @SerialName(INCORRECT_ANSWERS)
    val incorrectAnswers: List<String>
) {
    companion object Column {
        const val CORRECT_ANSWER = "correct_answer"
        const val INCORRECT_ANSWERS = "incorrect_answers"
    }
}