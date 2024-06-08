package com.korn.portfolio.randomtrivia.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Category(
    val name: String,
    val easy: Int,
    val medium: Int,
    val hard: Int,
    val downloadable: Boolean,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)