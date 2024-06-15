package com.korn.portfolio.randomtrivia.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.korn.portfolio.randomtrivia.network.model.Category
import com.korn.portfolio.randomtrivia.network.model.Question
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.network.model.response.FetchNewSession
import com.korn.portfolio.randomtrivia.network.model.response.FetchOverall
import com.korn.portfolio.randomtrivia.network.model.response.FetchResetSession
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class TriviaApiClient(private val triviaApiService: TriviaApiService) {
    suspend fun getCategories(): List<Category> {
        return triviaApiService.getCategories().categories
    }

    suspend fun getQuestionCount(categoryId: Int): QuestionCount {
        return triviaApiService.getQuestionCount(categoryId).questionCount
    }

    suspend fun getOverall(): FetchOverall {
        return triviaApiService.getOverall()
    }

    suspend fun getNewSession(): FetchNewSession {
        return triviaApiService.getNewSession()
    }

    suspend fun resetSession(token: String): FetchResetSession {
        return triviaApiService.resetSession(token)
    }

    suspend fun getQuestions(amount: Int, categoryId: Int, difficulty: String, type: String, token: String): List<Question> {
        return triviaApiService.getQuestions(amount, categoryId, difficulty, type, token).results
    }

    companion object {
        private const val BASE_URL = "https://opentdb.com"
        @Volatile
        private var INSTANCE: TriviaApiClient? = null

        fun getClient(): TriviaApiClient {
            return INSTANCE ?: synchronized(this) {
                val okHttpClient = OkHttpClient().newBuilder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .build()

                val retrofit = Retrofit.Builder()
                    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .build()

                val retrofitService = retrofit.create(TriviaApiService::class.java)

                TriviaApiClient(retrofitService).also { INSTANCE = it }
            }
        }
    }
}