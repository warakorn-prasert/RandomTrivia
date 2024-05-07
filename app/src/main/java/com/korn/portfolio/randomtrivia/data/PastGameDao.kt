package com.korn.portfolio.randomtrivia.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.Upsert
import com.korn.portfolio.randomtrivia.model.QuestionAnswer
import com.korn.portfolio.randomtrivia.model.PastGame
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Date
import java.util.UUID

@Dao
interface PastGameDao {
    @Query("SELECT * FROM PastGame")
    fun getAllPastGames(): Flow<List<PastGame>>

    @Upsert
    suspend fun upsertPastGame(pastGame: PastGame)

    @Query("SELECT EXISTS (SELECT * FROM PastGame WHERE id = :id)")
    suspend fun pastGameExists(id: UUID): Boolean

    @Query("DELETE FROM PastGame WHERE id = :id")
    suspend fun deletePastGame(id: UUID)
}

class QuestionAnswersConverters {
    @TypeConverter
    fun fromString(str: String): List<QuestionAnswer> {
        return Json.decodeFromString(str)
    }

    @TypeConverter
    fun toString(list: List<QuestionAnswer>): String {
        return Json.encodeToString(list)
    }
}

class DateTimeConverters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}