package com.korn.portfolio.randomtrivia.data

import androidx.core.text.HtmlCompat
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.korn.portfolio.randomtrivia.model.Categories
import com.korn.portfolio.randomtrivia.model.Category
import com.korn.portfolio.randomtrivia.model.CategoryCount
import com.korn.portfolio.randomtrivia.model.PastGame
import com.korn.portfolio.randomtrivia.model.Question
import com.korn.portfolio.randomtrivia.model.QuestionAnswer
import com.korn.portfolio.randomtrivia.model.Questions
import com.korn.portfolio.randomtrivia.network.TriviaApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import java.util.UUID

class TriviaRepository(
    private val categoryDao: CategoryDao,
    private val pastGameDao: PastGameDao,
    scope: CoroutineScope
) : TriviaApiService {
    init {
        scope.launch {
            // Clear cache
            categoryDao.clearTableCategory()
            categoryDao.clearTableCategoryCount()
        }
    }

    private val baseUrl = "https://opentdb.com"

    private val retrofit: Retrofit = Retrofit.Builder()
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .baseUrl(baseUrl)
        .build()

    private val retrofitService: TriviaApiService by lazy {
        retrofit.create(TriviaApiService::class.java)
    }

    override suspend fun getCategories(): Categories {
        return if (categoryDao.countCategories() > 0) {
            Categories(list = categoryDao.getCategories())
        } else {
            retrofitService.getCategories().apply result@ {
                categoryDao.upsertCategories(*this@result.list
                    .map { it.decodeHtml() }
                    .toTypedArray()
                )
            }
        }
    }

    override suspend fun getDetail(categoryId: Int): CategoryCount {
        return if (categoryDao.categoryCountExists(categoryId)) {
            categoryDao.getCategoryCount(categoryId)
        } else {
            retrofitService.getDetail(categoryId).apply result@ {
                categoryDao.upsertCategoryCount(this@result)
            }
        }
    }

    override suspend fun getQuestions(
        amount: Int,
        categoryId: Int,
        difficulty: String
    ): Questions {
        val questions = retrofitService.getQuestions(
            amount,
            categoryId,
            difficulty
        )
        return questions.copy(questions = questions.questions.map { it.decodeHtml() })
    }

    fun getAllPastGames(): Flow<List<PastGame>> = pastGameDao.getAllPastGames()
    suspend fun upsertPastGame(pastGame: PastGame) {
        if (!pastGameDao.pastGameExists(pastGame.id)) {
            pastGameDao.upsertPastGame(pastGame.decodeHtml())
        }
    }
    suspend fun deletePastGame(id: UUID) = pastGameDao.deletePastGame(id)

    private fun String.decodeHtml(): String =
        HtmlCompat.fromHtml(
            this,
            HtmlCompat.FROM_HTML_MODE_LEGACY
        ).toString()

    private fun Category.decodeHtml(): Category =
        copy(name = name.decodeHtml())

    private fun Question.decodeHtml(): Question =
        copy(
            question = question.decodeHtml(),
            category = category.decodeHtml(),
            correctAnswer = correctAnswer.decodeHtml(),
            incorrectAnswers = incorrectAnswers.map { it.decodeHtml() }
        )

    private fun QuestionAnswer.decodeHtml(): QuestionAnswer =
        copy(question = question.decodeHtml(), answer = answer.decodeHtml())

    private fun PastGame.decodeHtml(): PastGame =
        copy(questionAnswers = questionAnswers.map { it.decodeHtml() })
}