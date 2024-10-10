package com.korn.portfolio.randomtrivia.ui.viewmodel

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class MainViewModel(private val triviaRepository: TriviaRepository) : ViewModel() {
    val categoriesFetchStatus: StateFlow<FetchStatus> get() = mutableCategoriesFetchStatus
    private val mutableCategoriesFetchStatus = MutableStateFlow<FetchStatus>(FetchStatus.Success)

    private val neverFetch: Boolean
        get() = triviaRepository.remoteCategories.value.isNullOrEmpty()

    init {
        if (neverFetch) fetchCategories()
    }

    fun fetchCategories() {
        viewModelScope.launch {
            mutableCategoriesFetchStatus.emit(FetchStatus.Loading)
            delay(1000L)  // make progress indicator not look flickering
            mutableCategoriesFetchStatus.emit(
                try {
                    triviaRepository.fetchCategories()
                    FetchStatus.Success
                } catch (_: Exception) {
                    FetchStatus.Error("Failed to load new categories")
                }
            )
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