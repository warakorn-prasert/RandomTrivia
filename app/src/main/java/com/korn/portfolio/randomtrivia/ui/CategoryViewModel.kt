package com.korn.portfolio.randomtrivia.ui

import androidx.compose.runtime.MutableState
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.TriviaApplication
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

sealed interface NetworkUiState {
    data object Loading : NetworkUiState
    data class Error(val error: Exception) : NetworkUiState
    data object Success : NetworkUiState
}

// If no internet, loading animation will end too soon.
private const val fakeLoadingTimeMillis = 500L

class CategoryViewModel(private val triviaRepository: TriviaRepository) : ViewModel() {
    val remoteCategories: LiveData<List<Pair<Category, QuestionCount>>>
        get() = triviaRepository.remoteCategories

    val localCategories: Flow<List<Pair<Category, QuestionCount>>>
        get() = triviaRepository.localCategories

    fun fetchCategory(uiState: MutableState<NetworkUiState>) {
        viewModelScope.launch {
            uiState.value = NetworkUiState.Loading
            delay(fakeLoadingTimeMillis)
            uiState.value = try {
                triviaRepository.fetchCategories()
                NetworkUiState.Success
            } catch (e: Exception) {
                NetworkUiState.Error(e)
            }
        }
    }

    fun fetchQuestionCount(categoryId: Int, uiState: MutableState<NetworkUiState>) {
        viewModelScope.launch {
            uiState.value = NetworkUiState.Loading
            delay(fakeLoadingTimeMillis)
            uiState.value = try {
                triviaRepository.fetchQuestionCount(categoryId)
                NetworkUiState.Success
            } catch (e: Exception) {
                NetworkUiState.Error(e)
            }
        }
    }

    fun deleteLocalCategories(vararg id: Int) {
        viewModelScope.launch {
            triviaRepository.deleteLocalCategories(*id)
        }
    }

    fun deleteAllLocalCategories() {
        viewModelScope.launch {
            triviaRepository.deleteAllLocalCategories()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TriviaApplication
                CategoryViewModel(application.triviaRepository)
            }
        }
    }
}