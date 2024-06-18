package com.korn.portfolio.randomtrivia.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.network.TriviaApiClient
import com.korn.portfolio.randomtrivia.network.model.Category
import com.korn.portfolio.randomtrivia.network.model.Difficulty
import com.korn.portfolio.randomtrivia.network.model.Question
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.network.model.ResponseCode
import com.korn.portfolio.randomtrivia.network.model.Type
import kotlinx.coroutines.launch

class ResponseCodeException(responseCode: ResponseCode) : Exception() {
    override val message: String = when (responseCode) {
        ResponseCode.SUCCESS -> "Success."
        ResponseCode.NO_RESULTS -> "Not enough available questions."
        ResponseCode.INVALID_PARAMS -> "Invalid parameters."
        ResponseCode.TOKEN_NOT_EXIST -> "Token does not exist."
        ResponseCode.TOKEN_EMPTY -> "Token has returned all possible questions."
        ResponseCode.RATE_LIMIT -> "Too many requests. (Max: 1 per 5 seconds)"
        ResponseCode.UNSUPPORTED -> "Unsupported response code."
    }
}

data class ViewModelData<T>(
    val data: T,
    val uiState: UiState
)

sealed interface UiState {
    data object Loading : UiState
    data class Error(val error: Throwable) : UiState
    data object Success : UiState
}

private infix fun <T> ViewModelData<T>.with(data: T) = copy(data = data)
private infix fun <T> ViewModelData<T>.with(uiState: UiState) = copy(uiState = uiState)

class TriviaViewModel(private val triviaApiClient: TriviaApiClient) : ViewModel() {
    var categories: ViewModelData<Map<Category, Int>> by mutableStateOf(ViewModelData(emptyMap(), UiState.Success))
        private set
    var questionCounts: ViewModelData<Map<Category, QuestionCount>> by mutableStateOf(ViewModelData(emptyMap(), UiState.Success))
        private set
    var token: ViewModelData<String?> by mutableStateOf(ViewModelData(null, UiState.Success))
        private set
    var questions: ViewModelData<List<Question>> by mutableStateOf(ViewModelData(emptyList(), UiState.Success))
        private set

    fun getCategories() {
        viewModelScope.launch {
            categories = categories with UiState.Loading
            categories = try {
                categories with triviaApiClient.getCategories() with UiState.Success
            } catch (e: Exception) {
                categories with UiState.Error(e)
            }
        }
    }

    fun getQuestionCount(categoryId: Int) {
        viewModelScope.launch {
            questionCounts = questionCounts with UiState.Loading
            val category = categories.data.keys.firstOrNull { it.id == categoryId }
            questionCounts = if (category == null) {
                questionCounts with UiState.Error(NoSuchElementException("Invalid categoryId"))
            } else {
                try {
                    val data = category to triviaApiClient.getQuestionCount(categoryId)
                    questionCounts with UiState.Success with questionCounts.data + data
                } catch (e: Exception) {
                    questionCounts with UiState.Error(e)
                }
            }
        }
    }

    fun getToken() {
        viewModelScope.launch {
            token = token with UiState.Loading
            token = try {
                val newToken = triviaApiClient.getToken()
                    .takeIf { it.responseCode == ResponseCode.SUCCESS }?.token
                token with UiState.Success with newToken
            } catch (e: Exception) {
                token with UiState.Error(e) with null
            }
        }
    }

    fun clearToken() {
        token = token with null
    }

    fun getQuestions(amount: Int, categoryId: Int?, difficulty: Difficulty?, type: Type?) {
        viewModelScope.launch {
            questions = questions with UiState.Loading
            questions = try {
                triviaApiClient.getQuestions(amount, categoryId, difficulty, type, token.data)
                    .let { (responseCode, data) ->
                        if (responseCode != ResponseCode.SUCCESS)
                            throw ResponseCodeException(responseCode)
                        questions with UiState.Success with questions.data + data
                    }
            } catch (e: Exception) {
                questions with UiState.Error(e)
            }
        }
    }

    fun deleteAllQuestions() {
        viewModelScope.launch {
            questions = questions with emptyList()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                TriviaViewModel(TriviaApiClient.getClient())
            }
        }
    }
}