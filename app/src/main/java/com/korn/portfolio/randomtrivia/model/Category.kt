package com.korn.portfolio.randomtrivia.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
    val name: String,
    val downloadable: Boolean,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)