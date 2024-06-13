package com.korn.portfolio.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StringListConverters {
    @TypeConverter
    fun fromString(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromList(value: List<String>): String = Json.encodeToString(value)
}