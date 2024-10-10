package com.korn.portfolio.randomtrivia.ui.viewmodel

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class CategoryDisplay(
    val name: String,
    val totalQuestions: Int,
    val playedQuestions: Int,
    val id: Int,
    val isPlayed: Boolean = playedQuestions > 0,
)

private fun Pair<Category, QuestionCount>.asDisplay(playedQuestions: Int) =
    CategoryDisplay(
        name = first.name,
        totalQuestions = second.total,
        playedQuestions = playedQuestions,
        id = first.id
    )

enum class CategoryFilter(
    val displayText: String,
    val invoke: (List<CategoryDisplay>) -> List<CategoryDisplay>
) {
    ALL("All", { it }),
    PLAYED("Played", { all -> all.filter { it.isPlayed } }),
    NOT_PLAY("Not played", { all -> all.filter { !it.isPlayed } })
}

enum class CategorySort(
    val displayText: String,
    val invoke: (List<CategoryDisplay>) -> List<CategoryDisplay>
) {
    NAME("Name (A-Z)", { all -> all.sortedBy { it.name.lowercase() } }),
    TOTAL_QUESTIONS("Total questions (low-high)", { all -> all.sortedBy { it.totalQuestions } })
}

class CategoriesViewModel(
    private val triviaRepository: TriviaRepository
) : ViewModel() {

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
            mutableSearchWord.emit(searchWord)
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

    val categories: Flow<List<CategoryDisplay>>
        get() =
            combine(playedCategories, triviaRepository.remoteCategories.asFlow()) { played, remote ->
                remote.map { r ->
                    val playedCat = played.firstOrNull { it.first.id == r.first.id }
                    r.asDisplay(playedQuestions = playedCat?.second?.total ?: 0)
                } + played
                    .filter { p -> !remote.any { it.first.id == p.first.id } }
                    .map { p ->
                        p.asDisplay(playedQuestions = p.second.total)
                    }
            }.combine(filter) { cats, newFilter ->
                newFilter.invoke(cats)
            }.combine(searchWord) { all, newSearchWord ->
                all.filter {
                    it.name.lowercase().contains(newSearchWord.lowercase())
                }
            }.combine(sort) { all, newSort ->
                newSort.invoke(all)
            }.combine(reverseSort) { all, newReverse ->
                if (newReverse) all.reversed()
                else all
            }

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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TriviaApplication
                CategoriesViewModel(application.triviaRepository)
            }
        }
    }
}