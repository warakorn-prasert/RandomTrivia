package com.korn.portfolio.randomtrivia.ui

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.MyApplication
import com.korn.portfolio.randomtrivia.data.TriviaRepository
import com.korn.portfolio.randomtrivia.model.Category
import com.korn.portfolio.randomtrivia.model.Count
import com.korn.portfolio.randomtrivia.model.PastGame
import com.korn.portfolio.randomtrivia.model.Question
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

sealed interface UiState <T> {
    data class Idle<T>(private val idle: Boolean = true) : UiState<T>
    data class Loading<T>(private val loading: Boolean = true) : UiState<T>
    data class Success<T>(val data: T) : UiState<T>
    data class Error<T>(val exception: Exception) : UiState<T>
}

class TriviaViewModel(private val triviaRepository: TriviaRepository) : ViewModel() {

    private fun <T> updateUiState(uiState: MutableState<UiState<T>>, action: suspend () -> T) {
        viewModelScope.launch {
            uiState.value = UiState.Loading()
            uiState.value = try {
                UiState.Success(action())
            } catch (e: IOException) {  // network IO
                UiState.Error(e)
            } catch (e: HttpException) {  // non-2XX status
                UiState.Error(e)
            }
        }
    }

    fun getCategories(uiState: MutableState<UiState<List<Category>>>) =
        updateUiState(uiState, { triviaRepository.getCategories().list.sortedBy { it.name } })

    fun getCount(categoryId: Int, uiState: MutableState<UiState<Count>>) =
        updateUiState(uiState, { triviaRepository.getDetail(categoryId).count })

    fun getQuestions(
        amount: Int,
        categoryId: Int,
        difficulty: String,
        uiState: MutableState<UiState<List<Question>>>
    ) =
        updateUiState(uiState, { triviaRepository.getQuestions(amount, categoryId, difficulty).questions })

    fun getAllPastGames() = triviaRepository.getAllPastGames()
    fun savePastGame(pastGame: PastGame, uiState: MutableState<UiState<Boolean>>) {
        viewModelScope.launch {
            uiState.value = UiState.Loading()
            uiState.value = try {
                triviaRepository.upsertPastGame(pastGame)
                UiState.Success(true)
            } catch (e: Exception) {
                UiState.Error(e)
            }
        }
    }
    fun deletePastGame(id: UUID) {
        viewModelScope.launch {
            triviaRepository.deletePastGame(id)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as MyApplication)
                val triviaRepository = application.triviaRepository
                TriviaViewModel(triviaRepository)
            }
        }
    }
}