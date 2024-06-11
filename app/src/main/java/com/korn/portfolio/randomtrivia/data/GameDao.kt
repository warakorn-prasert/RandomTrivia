package com.korn.portfolio.randomtrivia.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.korn.portfolio.randomtrivia.model.Game
import com.korn.portfolio.randomtrivia.model.GameAnswer
import com.korn.portfolio.randomtrivia.model.GameDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Transaction
    @Query("SELECT * FROM GameDetail")
    fun getAll(): Flow<List<Game>>

    @Insert
    suspend fun insertDetail(vararg detail: GameDetail)

    @Insert
    suspend fun insertAnswer(vararg answer: GameAnswer)

    @Query("DELETE FROM GameDetail")
    suspend fun deleteAllDetails()

    @Query("DELETE FROM GameAnswer")
    suspend fun deleteAllAnswers()
}