package com.korn.portfolio.randomtrivia.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal enum class Difficulty {
    @SerialName("easy")
    EASY,
    @SerialName("medium")
    MEDIUM,
    @SerialName("hard")
    HARD;

    override fun toString(): String = javaClass
        .getField(name)
        .getAnnotation(SerialName::class.java)!!
        .value
}