package com.korn.portfolio.randomtrivia.ui

import androidx.compose.runtime.MutableState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.TriviaApplication
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.network.model.ResponseCode
import com.korn.portfolio.randomtrivia.repository.GameOption
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import com.korn.portfolio.randomtrivia.ui.model.NetworkUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PlayViewModel(private val triviaRepository: TriviaRepository) : ViewModel() {
    val remoteCategories: LiveData<List<Pair<Category, QuestionCount>>>
        get() = triviaRepository.remoteCategories

    val localCategories: Flow<List<Pair<Category, QuestionCount>>>
        get() = triviaRepository.localCategories

    val game: LiveData<Game?>
        get() = _game

    fun fetchCategories(uiState: MutableState<NetworkUiState>) {
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

    fun fetchNewGame(options: List<GameOption>, offline: Boolean, uiState: MutableState<NetworkUiState>) {
        viewModelScope.launch {
            uiState.value = NetworkUiState.Loading
            delay(fakeLoadingTimeMillis)
            uiState.value = try {
                val (respCode, game) = triviaRepository.fetchNewGame(options, offline)
                if (respCode == ResponseCode.SUCCESS) {
                    _game.postValue(game)
                    NetworkUiState.Success
                }
                else NetworkUiState.Error(Exception(respCode.name))
            } catch (e: Exception) {
                NetworkUiState.Error(e)
            }
        }
    }

    fun exitGame() {
        _game.postValue(null)
    }

    private val _game = MutableLiveData<Game?>(null)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TriviaApplication
                PlayViewModel(application.triviaRepository)
            }
        }
    }
}