package com.korn.portfolio.randomtrivia.data

import androidx.room.Dao
import androidx.room.Query
import com.korn.portfolio.randomtrivia.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao : BaseDao<Category> {
    @Query("SELECT * FROM Category")
    fun getAll(): Flow<List<Category>>

    @Query("DELETE FROM Category")
    suspend fun deleteAll()
}