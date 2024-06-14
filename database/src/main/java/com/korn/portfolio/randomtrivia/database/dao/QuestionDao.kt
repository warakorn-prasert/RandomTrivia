package com.korn.portfolio.randomtrivia.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface QuestionDao : BaseDao<Question> {
    @Query("SELECT * FROM Question WHERE categoryId IS NULL")
    fun getUncategorized(): Flow<List<Question>>

    @Query("""
        SELECT * FROM Question
        WHERE categoryId = :categoryId
        AND difficulty = :difficulty
        ORDER BY RANDOM()
        LIMIT :amount
        """)
    suspend fun getBy(categoryId: UUID, difficulty: Difficulty, amount: Int): List<Question>

    @Query("DELETE FROM Question")
    suspend fun deleteAll()

    @Query("DELETE FROM Question WHERE categoryId = :categoryId")
    suspend fun deleteByCategory(categoryId: UUID)

    @Query("DELETE FROM Question WHERE categoryId IS NULL")
    suspend fun deleteUncategorized()
}