package com.korn.portfolio.randomtrivia.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Type {
    @SerialName("multiple")
    MULTIPLE,
    @SerialName("boolean")
    BOOLEAN;

    override fun toString(): String = javaClass
        .getField(name)
        .getAnnotation(SerialName::class.java)!!
        .value
}