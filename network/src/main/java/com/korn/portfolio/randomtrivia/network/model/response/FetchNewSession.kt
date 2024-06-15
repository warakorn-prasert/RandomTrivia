package com.korn.portfolio.randomtrivia.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FetchNewSession(
    @SerialName("response_code")
    val responseCode: Int,
    @SerialName("response_message")
    val responseMessage: String,
    val token: String
)