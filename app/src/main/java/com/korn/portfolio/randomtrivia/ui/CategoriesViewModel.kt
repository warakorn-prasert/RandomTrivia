package com.korn.portfolio.randomtrivia.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.TriviaApplication
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class CategoryDisplayData(
    val name: String,
    val totalQuestion: Int,
    val isPlayed: Boolean,
    val id: Int
)

enum class CategoryFilter(
    val displayText: String,
    val function: (List<CategoryDisplayData>) -> List<CategoryDisplayData>
) {
    ALL("All", { it }),
    PLAYED("Played", { all -> all.filter { it.isPlayed } }),
    NOT_PLAY("Not played", { all -> all.filter { !it.isPlayed } })
}

enum class CategorySort(
    val displayText: String,
    val function: (List<CategoryDisplayData>) -> List<CategoryDisplayData>
) {
    NAME("Name (A-Z)", { all -> all.sortedBy { it.name.lowercase() } }),
    TOTAL_QUESTIONS("Total questions (low-high)", { all -> all.sortedBy { it.totalQuestion } })
}

sealed interface CategoryFetchStatus {
    data object Loading : CategoryFetchStatus
    data class Error(val message: String) : CategoryFetchStatus
    data object Success : CategoryFetchStatus
}



class CategoriesViewModel(
    private val triviaRepository: TriviaRepository
) : ViewModel() {

    var fetchStatus: CategoryFetchStatus by mutableStateOf(CategoryFetchStatus.Success)
        private set

    init {
        if (neverFetch) fetchCategories()
    }

    val searchWord: StateFlow<String> get() = mutableSearchWord
    val filter: StateFlow<CategoryFilter> get() = mutableFilter
    val sort: StateFlow<CategorySort> get() = mutableSort
    val reverseSort: StateFlow<Boolean> get() = mutableReverseSort
    private val mutableSearchWord = MutableStateFlow("")
    private val mutableFilter = MutableStateFlow(CategoryFilter.ALL)
    private val mutableSort = MutableStateFlow(CategorySort.NAME)
    private val mutableReverseSort = MutableStateFlow(false)

    fun setSearchWord(searchWord: String) {
        viewModelScope.launch {
            if (searchWord != this@CategoriesViewModel.searchWord.value) mutableSearchWord.emit(searchWord)
        }
    }

    fun setFilter(filter: CategoryFilter) {
        viewModelScope.launch {
            if (filter != this@CategoriesViewModel.filter.value) mutableFilter.emit(filter)
        }
    }

    fun setSort(sort: CategorySort) {
        viewModelScope.launch {
            if (sort != this@CategoriesViewModel.sort.value) mutableSort.emit(sort)
        }
    }

    fun setReverseSort(reverseSort: Boolean) {
        viewModelScope.launch {
            if (reverseSort != this@CategoriesViewModel.reverseSort.value) mutableReverseSort.emit(reverseSort)
        }
    }

    val categories: Flow<List<CategoryDisplayData>>
        get () = playedCategories
            .combine(notPlayedCategories) { played, notPlayed ->
                played.map { (category, questionCount) ->
                    CategoryDisplayData(category.name, questionCount.total, true, category.id)
                } + notPlayed.map { (category, questionCount) ->
                    CategoryDisplayData(category.name, questionCount.total, false, category.id)
                }
            }.combine(filter) { all, newFilter ->
                newFilter.function(all)
            }.combine(searchWord) { all, newSearchWord ->
                all.filter {
                    it.name.lowercase().contains(newSearchWord.lowercase())
                }
            }.combine(sort) { all, newSort ->
                newSort.function(all)
            }.combine(reverseSort) { all, newReverse ->
                if (newReverse) all.reversed()
                else all
            }


    fun fetchCategories() {
        viewModelScope.launch {
            fetchStatus = CategoryFetchStatus.Loading
            delay(1000L)  // make progress indicator not look flickering
            fetchStatus = try {
                withContext(Dispatchers.IO) {
                    triviaRepository.fetchCategories()
                }
                CategoryFetchStatus.Success
            } catch (_: Exception) {
                CategoryFetchStatus.Error("Failed to load new categories")
            }
        }
    }

    private val neverFetch: Boolean
        get() = triviaRepository.remoteCategories.value.isNullOrEmpty()

    // local categories that have saved questions
    private val playedCategories: Flow<List<Pair<Category, QuestionCount>>>
        get() = triviaRepository.localCategories
            .map { categories ->
                categories.filter { (_, questionCount) ->
                    questionCount.run {
                        easy + medium + hard > 0
                    }
                }
            }

    // remote categories that are not played
    private val notPlayedCategories: Flow<List<Pair<Category, QuestionCount>>>
        get() = triviaRepository.remoteCategories.asFlow()
            .combine(playedCategories) { remote, played ->
                remote.filterNot { (r, _) ->
                    played.any { (p, _) ->
                        r.name == p.name
                    }
                }
            }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TriviaApplication
                CategoriesViewModel(application.triviaRepository)
            }
        }
    }
}