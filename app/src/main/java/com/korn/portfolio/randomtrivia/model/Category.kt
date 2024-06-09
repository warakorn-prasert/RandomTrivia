package com.korn.portfolio.randomtrivia.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity
data class Category(
    val name: String,
    val downloadable: Boolean,
    @PrimaryKey
    val id: UUID = UUID.randomUUID()
)