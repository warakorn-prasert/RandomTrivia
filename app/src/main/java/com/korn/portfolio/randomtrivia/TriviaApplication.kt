package com.korn.portfolio.randomtrivia

import android.app.Application
import com.korn.portfolio.randomtrivia.database.TriviaDatabase
import com.korn.portfolio.randomtrivia.network.TriviaApiClient
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import com.korn.portfolio.randomtrivia.repository.TriviaRepositoryImpl

class TriviaApplication : Application() {
    private val triviaDatabase by lazy {
        TriviaDatabase.getDatabase(this)
    }
    private val triviaApiClient by lazy {
        TriviaApiClient()
    }
    lateinit var triviaRepository: TriviaRepository
    override fun onCreate() {
        super.onCreate()
        triviaRepository = TriviaRepositoryImpl(
            triviaDatabase.categoryDao(),
            triviaDatabase.questionDao(),
            triviaDatabase.gameDao(),
            triviaApiClient
        )
    }
}