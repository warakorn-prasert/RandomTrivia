package com.korn.portfolio.randomtrivia.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Categories(
    @SerialName("trivia_categories")
    val list: List<Category>
)