package com.korn.portfolio.randomtrivia.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionAnswer(
    @Embedded
    val question: Question,
    @ColumnInfo(ANSWER)
    @SerialName(ANSWER)
    var answer: String
) {
    companion object Column {
        const val ANSWER = "answer"
    }
}