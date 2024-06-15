package com.korn.portfolio.randomtrivia.network.model.response

import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FetchQuestionCount(
    @SerialName("category_id")
    val categoryId: Int,
    @SerialName("category_question_count")
    val questionCount: QuestionCount
)