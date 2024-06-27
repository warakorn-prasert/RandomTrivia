package com.korn.portfolio.randomtrivia.network.model.response

import com.korn.portfolio.randomtrivia.network.model.Overall
import kotlinx.serialization.Serializable

@Serializable
internal data class FetchOverall(
    val overall: Overall,
    val categories: Map<String, Overall>
)