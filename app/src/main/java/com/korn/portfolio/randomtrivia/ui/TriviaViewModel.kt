package com.korn.portfolio.randomtrivia.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.data.CategoryDao
import com.korn.portfolio.randomtrivia.data.TriviaDatabase
import com.korn.portfolio.randomtrivia.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TriviaViewModel(private val categoryDao: CategoryDao) : ViewModel() {
    val categories: Flow<List<Category>>
        get() = categoryDao.getAll()

    fun insertCategories(vararg category: Category) {
        viewModelScope.launch {
            categoryDao.insert(*category)
        }
    }

    fun updateCategories(vararg category: Category) {
        viewModelScope.launch {
            categoryDao.update(*category)
        }
    }

    fun deleteCategories(vararg category: Category) {
        viewModelScope.launch {
            categoryDao.delete(*category)
        }
    }

    fun deleteAllCategories() {
        viewModelScope.launch {
            categoryDao.deleteAll()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application: Application = this[APPLICATION_KEY]!!
                val triviaDatabase = TriviaDatabase.getDatabase(application.applicationContext)
                TriviaViewModel(triviaDatabase.categoryDao())
            }
        }
    }
}