package com.korn.portfolio.randomtrivia

import android.app.Application
import com.korn.portfolio.randomtrivia.data.TriviaDatabase
import com.korn.portfolio.randomtrivia.data.TriviaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MyApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private lateinit var triviaDatabase: TriviaDatabase
    lateinit var triviaRepository: TriviaRepository
        private set
    override fun onCreate() {
        super.onCreate()
        triviaDatabase = TriviaDatabase.getDatabase(this)
        triviaRepository = TriviaRepository(
            triviaDatabase.categoryDao(),
            triviaDatabase.pastGameDao(),
            applicationScope
        )
    }
}