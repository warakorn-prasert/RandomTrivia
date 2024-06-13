package com.korn.portfolio.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.korn.portfolio.database.converter.DateConverters
import com.korn.portfolio.database.converter.StringListConverters
import com.korn.portfolio.database.model.entity.Category
import com.korn.portfolio.database.model.entity.GameAnswer
import com.korn.portfolio.database.model.entity.GameDetail
import com.korn.portfolio.database.model.entity.Question

@Database(
    entities = [Category::class, Question::class, GameDetail::class, GameAnswer::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverters::class, DateConverters::class)
abstract class TriviaDatabase : RoomDatabase() {
    abstract fun categoryDao() : com.korn.portfolio.database.dao.CategoryDao
    abstract fun questionDao() : com.korn.portfolio.database.dao.QuestionDao
    abstract fun gameDao() : com.korn.portfolio.database.dao.GameDao

    companion object {
        @Volatile
        private var INSTANCE: TriviaDatabase? = null

        fun getDatabase(context: Context): TriviaDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    TriviaDatabase::class.java,
                    "trivia_database"
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}