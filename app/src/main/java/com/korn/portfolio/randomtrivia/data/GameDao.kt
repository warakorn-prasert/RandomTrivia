package com.korn.portfolio.randomtrivia.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.korn.portfolio.randomtrivia.model.Game
import com.korn.portfolio.randomtrivia.model.GameAnswer
import com.korn.portfolio.randomtrivia.model.GameDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao : BaseDao<GameDetail> {
    @Transaction
    @Query("SELECT * FROM GameDetail")
    fun getAll(): Flow<List<Game>>

    @Insert
    suspend fun insertAnswer(vararg answer: GameAnswer)

    @Update
    suspend fun updateAnswer(vararg answer: GameAnswer)

    @Delete
    suspend fun deleteAnswer(vararg answer: GameAnswer)

    @Query("DELETE FROM GameDetail")
    suspend fun deleteAll()
}