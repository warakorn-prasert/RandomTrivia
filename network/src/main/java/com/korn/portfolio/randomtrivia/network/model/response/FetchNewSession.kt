package com.korn.portfolio.randomtrivia.network.model.response

import com.korn.portfolio.randomtrivia.network.model.ResponseCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class FetchNewSession(
    @SerialName("response_code")
    val responseCode: ResponseCode,
    @SerialName("response_message")
    val responseMessage: String,
    val token: String
)