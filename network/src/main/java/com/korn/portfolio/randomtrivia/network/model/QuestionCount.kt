package com.korn.portfolio.randomtrivia.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuestionCount(
    @SerialName("total_question_count")
    val total: Int,
    @SerialName("total_easy_question_count")
    val easy: Int,
    @SerialName("total_medium_question_count")
    val medium: Int,
    @SerialName("total_hard_question_count")
    val hard: Int
)