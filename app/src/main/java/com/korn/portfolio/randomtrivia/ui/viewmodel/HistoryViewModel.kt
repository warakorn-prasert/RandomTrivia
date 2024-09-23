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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.UUID

enum class HistoryFilter(
    val displayText: String,
    val function: (List<Game>) -> List<Game>
) {
    ALL("All", { it }),
    TODAY("Today", { games ->
        fun Calendar.toTimeString() = "${get(Calendar.YEAR)}:${get(Calendar.MONTH)}:${get(Calendar.DAY_OF_MONTH)}"
        val calendar = Calendar.getInstance()
        val today = calendar.toTimeString()
        games.filter { game ->
            calendar.run {
                timeInMillis = game.detail.timestamp.time
                today == toTimeString()
            }
        }
    })
}

enum class HistorySort(
    val displayText: String,
    val function: (List<Game>) -> List<Game>
) {
    MOST_RECENT("Most recent", { games ->
        games.sortedByDescending { it.detail.timestamp }
    })
}

class HistoryViewModel(private val triviaRepository: TriviaRepository) : ViewModel() {
    val filter: StateFlow<HistoryFilter> get() = mutableFilter
    private val mutableFilter = MutableStateFlow(HistoryFilter.ALL)
    fun setFilter(newValue: HistoryFilter) {
        mutableFilter.value = newValue
    }

    val sort: StateFlow<HistorySort> get() = mutableSort
    private val mutableSort = MutableStateFlow(HistorySort.MOST_RECENT)
    fun setSort(newValue: HistorySort) {
        mutableSort.value = newValue
    }

    val reverseSort: StateFlow<Boolean> get() = mutableReverseSort
    private val mutableReverseSort = MutableStateFlow(false)
    fun setReverseSort(newValue: Boolean) {
        mutableReverseSort.value = newValue
    }

    val games: Flow<List<Game>>
        get() = combine(triviaRepository.savedGames, filter, sort, reverseSort) { games, f, s, rs ->
            s.function(f.function(games)).let {
                if (rs) it.reversed()
                else it
            }
        }

    fun deleteGame(gameId: UUID) {
        viewModelScope.launch {
            triviaRepository.deleteGame(gameId)
        }
    }

    var gameToReplay: Game = Game(GameDetail(Date(), 0), emptyList())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TriviaApplication
                HistoryViewModel(application.triviaRepository)
            }
        }
    }
}