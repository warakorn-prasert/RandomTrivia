package com.korn.portfolio.randomtrivia.network.model.response

import com.korn.portfolio.randomtrivia.network.model.Question
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FetchQuestions(
    @SerialName("response_code")
    val responseCode: Int,
    val results: List<Question>
)