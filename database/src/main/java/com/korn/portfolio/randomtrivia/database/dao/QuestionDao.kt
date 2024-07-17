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
        AND id NOT IN (:excluded)
        ORDER BY RANDOM()
        LIMIT :amount
        """)
    suspend fun getBy(categoryId: Int, difficulty: Difficulty, amount: Int, excluded: List<UUID> = emptyList()): List<Question>

    @Query("""
        SELECT * FROM Question
        WHERE categoryId = :categoryId
        ORDER BY RANDOM()
        AND id NOT IN (:excluded)
        LIMIT :amount
        """)
    suspend fun getBy(categoryId: Int, amount: Int, excluded: List<UUID> = emptyList()): List<Question>

    @Query("""
        SELECT * FROM Question
        WHERE difficulty = :difficulty
        AND id NOT IN (:excluded)
        ORDER BY RANDOM()
        LIMIT :amount
        """)
    suspend fun getBy(difficulty: Difficulty, amount: Int, excluded: List<UUID> = emptyList()): List<Question>

    @Query("""
        SELECT * FROM Question
        WHERE id NOT IN (:excluded)
        ORDER BY RANDOM()
        LIMIT :amount
        """)
    suspend fun getBy(amount: Int, excluded: List<UUID> = emptyList()): List<Question>

    @Query("SELECT * FROM Question WHERE question = :question LIMIT 1")
    suspend fun getOneBy(question: String): Question?

    @Query("DELETE FROM Question")
    suspend fun deleteAll()

    @Query("DELETE FROM Question WHERE categoryId = :categoryId")
    suspend fun deleteByCategory(categoryId: Int)

    @Query("DELETE FROM Question WHERE categoryId IS NULL")
    suspend fun deleteUncategorized()
}