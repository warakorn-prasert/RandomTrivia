package com.korn.portfolio.randomtrivia.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.korn.portfolio.randomtrivia.model.Category
import com.korn.portfolio.randomtrivia.model.Question

@Database(
    entities = [Category::class, Question::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(StringListConverters::class)
abstract class TriviaDatabase : RoomDatabase() {
    abstract fun categoryDao() : CategoryDao
    abstract fun questionDao() : QuestionDao

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