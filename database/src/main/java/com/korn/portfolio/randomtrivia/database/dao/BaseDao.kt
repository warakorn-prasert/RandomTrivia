package com.korn.portfolio.randomtrivia.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Upsert

interface BaseDao<T> {
    @Insert
    suspend fun insert(vararg item: T)

    @Update
    suspend fun update(vararg item: T)

    @Delete
    suspend fun delete(vararg item: T)

    @Upsert
    suspend fun upsert(vararg item: T)
}