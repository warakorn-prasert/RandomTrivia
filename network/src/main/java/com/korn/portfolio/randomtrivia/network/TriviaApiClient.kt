package com.korn.portfolio.randomtrivia.network

import androidx.core.text.HtmlCompat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.korn.portfolio.randomtrivia.network.model.Category
import com.korn.portfolio.randomtrivia.database.model.entity.Category as DbCategory
import com.korn.portfolio.randomtrivia.database.model.entity.Question as DbQuestion
import com.korn.portfolio.randomtrivia.database.model.Difficulty as DbDifficulty
import com.korn.portfolio.randomtrivia.network.model.Difficulty
import com.korn.portfolio.randomtrivia.network.model.Question
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.network.model.ResponseCode
import com.korn.portfolio.randomtrivia.network.model.Type
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

private fun String.decodeHtml(): String =
    HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY).toString()

private fun Category.toDbCategory(): DbCategory =
    DbCategory(
        name = name.decodeHtml(),
        id = id
    )

private fun DbDifficulty.toDifficulty(): Difficulty =
    when (this) {
        DbDifficulty.EASY -> Difficulty.EASY
        DbDifficulty.MEDIUM -> Difficulty.MEDIUM
        DbDifficulty.HARD -> Difficulty.HARD
    }

private fun Difficulty.toDbDifficulty(): DbDifficulty =
    when (this) {
        Difficulty.EASY -> DbDifficulty.EASY
        Difficulty.MEDIUM -> DbDifficulty.MEDIUM
        Difficulty.HARD -> DbDifficulty.HARD
    }

private fun Question.toDbQuestion(getId: (String) -> Int? ): DbQuestion =
    DbQuestion(
        question = question.decodeHtml(),
        difficulty = difficulty.toDbDifficulty(),
        categoryId = getId(category.decodeHtml()),
        correctAnswer = correctAnswer.decodeHtml(),
        incorrectAnswers = incorrectAnswers.map { it.decodeHtml() }
    )

class TriviaApiClient(
    connectTimeout: Long = 30L,
    readTimeout: Long = 30L,
    writeTimeout: Long = 30L
) {
    private val triviaApiService: TriviaApiService
    init {
        val baseUrl = "https://opentdb.com"
        val okHttpClient = OkHttpClient().newBuilder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .build()
        val retrofit = Retrofit.Builder()
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .build()
        triviaApiService = retrofit.create(TriviaApiService::class.java)
    }

    private var categories: List<Category> = emptyList()

    @OptIn(DelicateCoroutinesApi::class)
    private val categoriesContext = newSingleThreadContext("categories")

    private suspend fun cacheCategories() {
        categoriesContext.use {
            categories = triviaApiService.getCategories().categories.map {
                it.copy(name = it.name.decodeHtml())
            }
        }
    }

    suspend fun getToken(): Pair<ResponseCode, String> =
        withContext(Dispatchers.IO) {
            triviaApiService.getNewSession().run {
                responseCode to token
            }
        }

    suspend fun getCategories(): Map<DbCategory, Int> {
        cacheCategories()
        val totalQuestions = withContext(Dispatchers.IO) {
            triviaApiService.getOverall().categories
        }
        // In case of totalQuestions has fewer categories
        return categoriesContext.use {
            totalQuestions
                .filterKeys { categoryId ->
                    categories.any { it.id == categoryId.toIntOrNull() }
                }
                .mapKeys { (categoryId, _) ->
                    categories
                        .first { it.id == categoryId.toInt() }
                        .toDbCategory()
                }
                .mapValues { it.value.verified }
        }
    }

    suspend fun getQuestionCount(categoryId: Int): QuestionCount =
        withContext(Dispatchers.IO) {
            triviaApiService.getQuestionCount(categoryId).questionCount
        }

    suspend fun getQuestions(
        amount: Int,
        categoryId: Int?,
        difficulty: DbDifficulty?,
        type: Type?,
        token: String?
    ): Pair<ResponseCode, List<DbQuestion>> {
        if (categories.isEmpty()) cacheCategories()
        return withContext(Dispatchers.IO) {
            triviaApiService.getQuestions(
                amount = amount,
                categoryId = categoryId,
                difficulty = difficulty?.toDifficulty(),
                type = type,
                token = token
            )
        }.run {
            categoriesContext.use {
                responseCode to results.map { question ->
                    question.toDbQuestion(getId = { category ->
                        categories.firstOrNull { it.name == category }?.id
                    })
                }
            }
        }
    }
}