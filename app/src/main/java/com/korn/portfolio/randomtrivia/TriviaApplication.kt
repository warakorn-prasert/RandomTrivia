package com.korn.portfolio.randomtrivia

import android.app.Application
import com.korn.portfolio.randomtrivia.database.TriviaDatabase
import com.korn.portfolio.randomtrivia.network.TriviaApiClient
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import com.korn.portfolio.randomtrivia.repository.TriviaRepositoryImpl

class TriviaApplication : Application() {
    private val triviaDatabase by lazy { TriviaDatabase.getDatabase(applicationContext) }
    lateinit var triviaRepository: TriviaRepository
        private set
    override fun onCreate() {
        super.onCreate()
        triviaRepository = TriviaRepositoryImpl(
            categoryDao = triviaDatabase.categoryDao(),
            questionDao = triviaDatabase.questionDao(),
            gameDao = triviaDatabase.gameDao(),
            triviaApiClient = TriviaApiClient()
        )
    }
}