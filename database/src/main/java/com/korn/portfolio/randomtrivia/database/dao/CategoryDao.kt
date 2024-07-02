package com.korn.portfolio.randomtrivia.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.korn.portfolio.randomtrivia.database.model.CategoryWithQuestions
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao : BaseDao<Category> {
    @Query("DELETE FROM Category")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM Category")
    fun getCategoriesWithQuestions(): Flow<List<CategoryWithQuestions>>

    @Query("SELECT MIN(id) FROM Category")
    suspend fun getMinId(): Int

    @Query("SELECT * FROM Category WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Category?
}