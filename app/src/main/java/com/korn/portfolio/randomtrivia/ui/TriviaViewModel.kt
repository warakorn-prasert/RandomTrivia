package com.korn.portfolio.randomtrivia.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.database.dao.CategoryDao
import com.korn.portfolio.database.dao.GameDao
import com.korn.portfolio.database.dao.QuestionDao
import com.korn.portfolio.database.TriviaDatabase
import com.korn.portfolio.database.model.entity.Category
import com.korn.portfolio.database.model.CategoryWithQuestions
import com.korn.portfolio.database.model.Game
import com.korn.portfolio.database.model.entity.GameAnswer
import com.korn.portfolio.database.model.entity.GameDetail
import com.korn.portfolio.database.model.GameOption
import com.korn.portfolio.database.model.entity.Question
import com.korn.portfolio.randomtrivia.data.MockData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class TriviaViewModel(
    private val categoryDao: CategoryDao,
    private val questionDao: QuestionDao,
    private val gameDao: GameDao
) : ViewModel() {
    val categoriesWithQuestions: Flow<List<CategoryWithQuestions>>
        get() = categoryDao.getCategoriesWithQuestions()

    val uncategorizedQuestions: Flow<List<Question>>
        get() = questionDao.getUncategorized()

    val games: Flow<List<Game>>
        get() = gameDao.getAll()

    fun insertMockData() {
        viewModelScope.launch {
            with(MockData()) {
                categoryDao.insert(category1)
                categoryDao.insert(category2)
                questionDao.insert(question11)
                questionDao.insert(question12)
                questionDao.insert(question21)
                gameDao.insert(gameDetail)
                gameDao.insertAnswer(qameAnswer11)
                gameDao.insertAnswer(qameAnswer12)
                gameDao.insertAnswer(qameAnswer21)
            }
        }
    }

    fun insertCategories(vararg category: Category) {
        viewModelScope.launch {
            categoryDao.insert(*category)
        }
    }

    fun insertQuestions(vararg question: Question) {
        viewModelScope.launch {
            questionDao.insert(*question)
        }
    }

    fun insertGame(options: List<GameOption>) {
        viewModelScope.launch {
            val gameDetail = GameDetail(
                timestamp = Date(),
                totalTimeSecond = 1234
            )
            val gameAnswers = mutableListOf<GameAnswer>()
            options.forEach { option ->
                val questions = questionDao.getBy(
                    categoryId = option.category.id,
                    difficulty = option.difficulty,
                    amount = option.amount
                )
                val answers = questions.map { question ->
                    GameAnswer(
                        gameId = gameDetail.gameId,
                        questionId = question.id,
                        answer = "(Not answered)",
                        categoryId = option.category.id
                    )
                }
                answers.forEach(gameAnswers::add)
            }
            gameDao.insert(gameDetail)
            gameDao.insertAnswer(*gameAnswers.toTypedArray())
        }
    }

    fun updateCategories(vararg category: Category) {
        viewModelScope.launch {
            categoryDao.update(*category)
        }
    }

    fun updateQuestions(vararg question: Question) {
        viewModelScope.launch {
            questionDao.update(*question)
        }
    }

    fun updateAnswer(vararg answer: GameAnswer) {
        viewModelScope.launch {
            gameDao.updateAnswer(*answer)
        }
    }

    fun deleteCategories(vararg category: Category) {
        viewModelScope.launch {
            categoryDao.delete(*category)
        }
    }

    fun deleteQuestions(vararg question: Question) {
        viewModelScope.launch {
            questionDao.delete(*question)
        }
    }

    fun deleteGames(vararg games: Game) {
        viewModelScope.launch {
            games.forEach { game ->
                gameDao.delete(game.detail)
                game.questions.forEach { question ->
                    gameDao.deleteAnswer(question.answer)
                }
            }
        }
    }

    fun deleteAllCategories() {
        viewModelScope.launch {
            categoryDao.deleteAll()
        }
    }

    fun deleteAllGames() {
        viewModelScope.launch {
            gameDao.deleteAll()
        }
    }

    fun deleteByCategory(categoryId: UUID?) {
        viewModelScope.launch {
            if (categoryId == null) {
                questionDao.deleteUncategorized()
            } else {
                questionDao.deleteByCategory(categoryId)
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application: Application = this[APPLICATION_KEY]!!
                val triviaDatabase = TriviaDatabase.getDatabase(application.applicationContext)
                TriviaViewModel(
                    triviaDatabase.categoryDao(),
                    triviaDatabase.questionDao(),
                    triviaDatabase.gameDao()
                )
            }
        }
    }
}