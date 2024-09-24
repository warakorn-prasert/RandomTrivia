package com.korn.portfolio.randomtrivia.database.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
    val name: String,
    @PrimaryKey
    val id: Int
)