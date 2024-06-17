package com.korn.portfolio.randomtrivia.network

import androidx.core.text.HtmlCompat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.korn.portfolio.randomtrivia.network.model.Category
import com.korn.portfolio.randomtrivia.network.model.Difficulty
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.network.model.Type
import com.korn.portfolio.randomtrivia.network.model.response.FetchQuestions
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class TriviaApiClient(private val triviaApiService: TriviaApiService) {
    suspend fun getCategories(): Map<Category, Int> {
        val categories = triviaApiService.getCategories().categories
        val totalQuestions = triviaApiService.getOverall().categories
        // In case of totalQuestions has fewer categories
        return totalQuestions
            .filterKeys { categoryId ->
                categories.any { it.id == categoryId.toIntOrNull() }
            }
            .mapKeys { (categoryId, _) ->
                categories
                    .first { it.id == categoryId.toInt() }
                    .run { copy(name = name.decodeHtml()) }
            }
            .mapValues { it.value.verified }
    }

    suspend fun getQuestionCount(categoryId: Int): QuestionCount {
        return triviaApiService.getQuestionCount(categoryId).questionCount
    }

    suspend fun getQuestions(amount: Int, categoryId: Int?, difficulty: Difficulty?, type: Type?, token: String?): FetchQuestions {
        return triviaApiService.getQuestions(amount, categoryId, difficulty, type, token).run {
            copy(results = results.map {
                it.copy(
                    category = it.category.decodeHtml(),
                    question = it.question.decodeHtml(),
                    correctAnswer = it.correctAnswer.decodeHtml(),
                    incorrectAnswers = it.incorrectAnswers.map { ans -> ans.decodeHtml() }
                )
            })
        }
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

    private fun String.decodeHtml(): String =
        HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()
}