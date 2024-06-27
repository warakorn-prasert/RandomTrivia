package com.korn.portfolio.randomtrivia

import android.app.Application
import com.korn.portfolio.randomtrivia.network.TriviaApiClient

class TriviaApplication : Application() {
    lateinit var triviaApiClient: TriviaApiClient
    override fun onCreate() {
        super.onCreate()
        triviaApiClient = TriviaApiClient()
    }
}