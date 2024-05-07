package com.korn.portfolio.randomtrivia.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Category(
    @PrimaryKey
    val id: Int,
    val name: String
) {
    companion object Column {
        const val ID = "id"
        const val NAME = "name"
    }
}