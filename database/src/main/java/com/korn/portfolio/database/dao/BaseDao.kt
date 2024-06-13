package com.korn.portfolio.database.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

interface BaseDao<T> {
    @Insert
    suspend fun insert(vararg item: T)

    @Update
    suspend fun update(vararg item: T)

    @Delete
    suspend fun delete(vararg item: T)
}