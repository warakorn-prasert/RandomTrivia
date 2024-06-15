package com.korn.portfolio.randomtrivia.network.model

import kotlinx.serialization.Serializable

@Serializable
data class Category(
    val id: Int,
    val name: String
)