package com.korn.portfolio.randomtrivia.ui

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.TriviaApplication
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.network.model.ResponseCode
import com.korn.portfolio.randomtrivia.repository.GameOption
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import com.korn.portfolio.randomtrivia.ui.model.NetworkUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.Date

private val emptyGame: Game
    get() = Game(
        detail = GameDetail(timestamp = Date(), totalTimeSecond = 0),
        questions = emptyList()
    )

class PlayViewModel(private val triviaRepository: TriviaRepository) : ViewModel() {
    val remoteCategories: LiveData<List<Pair<Category, QuestionCount>>>
        get() = triviaRepository.remoteCategories

    val localCategories: Flow<List<Pair<Category, QuestionCount>>>
        get() = triviaRepository.localCategories

    var game = mutableStateOf(emptyGame)

    var timerSecond by mutableIntStateOf(0)
        private set

    private var timer: Job = Job().also { it.cancel() }

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
                val (respCode, newGame) = triviaRepository.fetchNewGame(options, offline)
                if (respCode == ResponseCode.SUCCESS) {
                    game.value = newGame
                    NetworkUiState.Success
                }
                else NetworkUiState.Error(Exception(respCode.name))
            } catch (e: Exception) {
                NetworkUiState.Error(e)
            }
        }
    }

    fun resetGame() {
        game.value = emptyGame
    }

    fun saveGame() {
        viewModelScope.launch {
            triviaRepository.saveGame(game.value)
        }
    }

    fun setTimer(state: Boolean) {
        if (state && !timer.isActive) {
            timer = viewModelScope.launch {
                timerSecond = 0
                flow {
                    var count = 0
                    var refTime = System.currentTimeMillis()
                    while (true) {
                        if (System.currentTimeMillis() - refTime > 1000) {
                            count += ((System.currentTimeMillis() - refTime) / 1000).toInt()
                            emit(count)
                            refTime = System.currentTimeMillis()
                        }
                        delay(300)
                    }
                }.collect {
                    timerSecond = it
                }
            }
        } else if (!state) {
            timer.cancel()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TriviaApplication
                PlayViewModel(application.triviaRepository)
            }
        }
    }
}