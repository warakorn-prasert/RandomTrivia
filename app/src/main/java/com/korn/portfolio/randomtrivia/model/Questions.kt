package com.korn.portfolio.randomtrivia.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Questions(
    @SerialName("response_code")
    val responseCode: Int,
    @SerialName("results")
    val questions: List<Question>
)