package com.korn.portfolio.database.converter

import androidx.room.TypeConverter
import java.util.Date

class DateConverters {
    @TypeConverter
    fun toDate(value: Long): Date = Date(value)

    @TypeConverter
    fun fromDate(value: Date): Long = value.time
}