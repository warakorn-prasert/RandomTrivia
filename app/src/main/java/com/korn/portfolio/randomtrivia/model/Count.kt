package com.korn.portfolio.randomtrivia.model

import androidx.room.ColumnInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Count(
    @ColumnInfo(TOTAL)
    @SerialName(TOTAL)
    val total: Int,
    @ColumnInfo(EASY)
    @SerialName(EASY)
    val easy: Int,
    @ColumnInfo(MEDIUM)
    @SerialName(MEDIUM)
    val medium: Int,
    @ColumnInfo(HARD)
    @SerialName(HARD)
    val hard: Int
) {
    companion object Column {
        const val TOTAL = "total_question_count"
        const val EASY = "total_easy_question_count"
        const val MEDIUM = "total_medium_question_count"
        const val HARD = "total_hard_question_count"
    }
}