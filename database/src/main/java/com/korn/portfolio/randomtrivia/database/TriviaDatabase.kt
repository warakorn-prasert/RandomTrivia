package com.korn.portfolio.randomtrivia.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.korn.portfolio.randomtrivia.database.converter.DateConverters
import com.korn.portfolio.randomtrivia.database.converter.StringListConverters
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.entity.GameAnswer
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import com.korn.portfolio.randomtrivia.database.dao.CategoryDao
import com.korn.portfolio.randomtrivia.database.dao.GameDao
import com.korn.portfolio.randomtrivia.database.dao.QuestionDao

@Database(
    entities = [Category::class, Question::class, GameDetail::class, GameAnswer::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverters::class, DateConverters::class)
abstract class TriviaDatabase : RoomDatabase() {
    abstract fun categoryDao() : CategoryDao
    abstract fun questionDao() : QuestionDao
    abstract fun gameDao() : GameDao

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