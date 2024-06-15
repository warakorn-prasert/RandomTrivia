package com.korn.portfolio.randomtrivia.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Overall(
    @SerialName("total_num_of_questions")
    val total: Int,
    @SerialName("total_num_of_pending_questions")
    val pending: Int,
    @SerialName("total_num_of_verified_questions")
    val verified: Int,
    @SerialName("total_num_of_rejected_questions")
    val rejected: Int
)