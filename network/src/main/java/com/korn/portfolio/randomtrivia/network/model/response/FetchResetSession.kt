package com.korn.portfolio.randomtrivia.network.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FetchResetSession(
    @SerialName("response_code")
    val responseCode: Int,
    val token: String
)