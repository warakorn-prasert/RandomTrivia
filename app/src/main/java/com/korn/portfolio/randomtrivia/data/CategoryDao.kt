package com.korn.portfolio.randomtrivia.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.korn.portfolio.randomtrivia.model.Category
import com.korn.portfolio.randomtrivia.model.CategoryCount

@Dao
interface CategoryDao {
    @Query("SELECT * FROM Category")
    suspend fun getCategories(): List<Category>

    @Query("SELECT * FROM CategoryCount WHERE ${CategoryCount.CATEGORY_ID} = :categoryId LIMIT 1")
    suspend fun getCategoryCount(categoryId: Int): CategoryCount

    @Upsert
    suspend fun upsertCategories(vararg category: Category)

    @Upsert
    suspend fun upsertCategoryCount(detail: CategoryCount)

    @Query("SELECT COUNT(*) FROM Category")
    suspend fun countCategories(): Int

    @Query("SELECT EXISTS (SELECT * FROM CategoryCount WHERE ${CategoryCount.CATEGORY_ID} = :categoryId)")
    suspend fun categoryCountExists(categoryId: Int): Boolean

    @Query("DELETE FROM Category")
    suspend fun clearTableCategory()

    @Query("DELETE FROM CategoryCount")
    suspend fun clearTableCategoryCount()
}