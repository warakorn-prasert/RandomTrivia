package com.korn.portfolio.randomtrivia.network.model.response

import com.korn.portfolio.randomtrivia.network.model.Question
import com.korn.portfolio.randomtrivia.network.model.ResponseCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FetchQuestions(
    @SerialName("response_code")
    val responseCode: ResponseCode,
    val results: List<Question>
)