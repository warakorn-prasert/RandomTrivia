package com.korn.portfolio.randomtrivia.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.entity.GameAnswer
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
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