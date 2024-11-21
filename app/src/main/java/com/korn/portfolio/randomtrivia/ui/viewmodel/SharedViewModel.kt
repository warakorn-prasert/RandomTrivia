package com.korn.portfolio.randomtrivia.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.TriviaApplication
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import com.korn.portfolio.randomtrivia.ui.common.FetchStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class SharedViewModel(private val triviaRepository: TriviaRepository) : ViewModel() {
    var categoriesFetchStatus: FetchStatus by mutableStateOf(FetchStatus.Success)
        private set

    init {
        // if never fetch
        if (triviaRepository.remoteCategories.value.isNullOrEmpty())
            fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            categoriesFetchStatus = FetchStatus.Loading
            delay(1000L)  // make progress indicator not look flickering
            categoriesFetchStatus = try {
                triviaRepository.fetchCategories()
                FetchStatus.Success
            } catch (_: Exception) {
                FetchStatus.Error("Failed to load new categories")
            }
        }
    }

    var game = Game(GameDetail(Date(), 0), emptyList())

    fun saveGame() {
        viewModelScope.launch {
            triviaRepository.saveGame(game)
        }
    }

    val pastGames: Flow<List<Game>> = triviaRepository.savedGames

    fun deleteGame(gameId: UUID) {
        viewModelScope.launch {
            triviaRepository.deleteGame(gameId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TriviaApplication
                SharedViewModel(application.triviaRepository)
            }
        }
    }
}