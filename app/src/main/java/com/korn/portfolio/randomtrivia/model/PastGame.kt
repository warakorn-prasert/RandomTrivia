package com.korn.portfolio.randomtrivia.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class PastGame(
    @PrimaryKey
    val id: UUID = UUID.randomUUID(),
    @ColumnInfo(DATETIME)
    val datetime: Date,
    @ColumnInfo(QUESTION_ANSWERS)
    val questionAnswers: List<QuestionAnswer>
) {
    companion object Column {
        const val DATETIME = "datetime"
        const val QUESTION_ANSWERS = "question_answers"
    }
}