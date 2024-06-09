package com.korn.portfolio.randomtrivia.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.korn.portfolio.randomtrivia.model.Category
import com.korn.portfolio.randomtrivia.model.CategoryWithQuestions
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao : BaseDao<Category> {
    @Query("SELECT * FROM Category")
    fun getAll(): Flow<List<Category>>

    @Query("DELETE FROM Category")
    suspend fun deleteAll()

    @Query("SELECT * FROM Category ORDER BY id DESC LIMIT 1")
    suspend fun getLatest(): Category

    @Transaction
    @Query("SELECT * FROM Category")
    fun getCategoriesWithQuestions(): Flow<List<CategoryWithQuestions>>
}