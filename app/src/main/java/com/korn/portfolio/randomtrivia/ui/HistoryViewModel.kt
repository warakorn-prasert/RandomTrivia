package com.korn.portfolio.randomtrivia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.TriviaApplication
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class HistoryViewModel(private val triviaRepository: TriviaRepository) : ViewModel() {
    val games: Flow<List<Game>>
        get() = triviaRepository.savedGames

    fun deleteGame(gameId: UUID) {
        viewModelScope.launch {
            triviaRepository.deleteGame(gameId)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as TriviaApplication
                HistoryViewModel(application.triviaRepository)
            }
        }
    }
}