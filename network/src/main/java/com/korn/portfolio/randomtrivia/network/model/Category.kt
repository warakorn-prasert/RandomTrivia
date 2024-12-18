package com.korn.portfolio.randomtrivia.network.model

import kotlinx.serialization.Serializable

@Serializable
internal data class Category(
    val id: Int,
    val name: String
)