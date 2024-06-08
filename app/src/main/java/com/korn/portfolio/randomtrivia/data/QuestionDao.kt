package com.korn.portfolio.randomtrivia.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.TypeConverter
import com.korn.portfolio.randomtrivia.model.Question
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Dao
interface QuestionDao : BaseDao<Question> {
    @Query("SELECT * FROM Question")
    fun getAll(): Flow<List<Question>>

    @Query("DELETE FROM Question")
    suspend fun deleteAll()

    @Query("DELETE FROM Question WHERE categoryId = :categoryId")
    suspend fun deleteByCategory(categoryId: Int)
}

class StringListConverters {
    @TypeConverter
    fun fromString(value: String): List<String> = Json.decodeFromString(value)

    @TypeConverter
    fun fromList(value: List<String>): String = Json.encodeToString(value)
}