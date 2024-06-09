package com.korn.portfolio.randomtrivia.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.data.CategoryDao
import com.korn.portfolio.randomtrivia.data.QuestionDao
import com.korn.portfolio.randomtrivia.data.TriviaDatabase
import com.korn.portfolio.randomtrivia.model.Category
import com.korn.portfolio.randomtrivia.model.CategoryWithQuestions
import com.korn.portfolio.randomtrivia.model.Question
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class TriviaViewModel(
    private val categoryDao: CategoryDao,
    private val questionDao: QuestionDao
) : ViewModel() {
    val categoriesWithQuestions: Flow<List<CategoryWithQuestions>>
        get() = categoryDao.getCategoriesWithQuestions()

    val uncategorizedQuestions: Flow<List<Question>>
        get() = questionDao.getUncategorized()

    fun insertMockData() {
        viewModelScope.launch {
            suspend fun insert(data: CategoryWithQuestions) {
                val newCategory = data.category.copy(id = UUID.randomUUID())
                categoryDao.insert(newCategory)
                data.questions.forEach {
                    questionDao.insert(it.copy(
                        id = UUID.randomUUID(),
                        categoryId = newCategory.id
                    ))
                }
            }
            insert(mockCategoryWithQuestions1)
            insert(mockCategoryWithQuestions2)
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

    fun deleteAllCategories() {
        viewModelScope.launch {
            categoryDao.deleteAll()
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
                TriviaViewModel(triviaDatabase.categoryDao(), triviaDatabase.questionDao())
            }
        }
    }
}