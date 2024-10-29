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
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel(private val triviaRepository: TriviaRepository) : ViewModel() {
    var showSplashScreen by mutableStateOf(true)

    var categoriesFetchStatus: FetchStatus by mutableStateOf(FetchStatus.Success)
        private set

    private val neverFetch: Boolean
        get() = triviaRepository.remoteCategories.value.isNullOrEmpty()

    init {
        if (neverFetch) fetchCategories()
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

    var onlineMode = false
    var settings = emptyList<GameSetting>()
    var game = Game(GameDetail(Date(), 0), emptyList())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TriviaApplication
                MainViewModel(application.triviaRepository)
            }
        }
    }
}