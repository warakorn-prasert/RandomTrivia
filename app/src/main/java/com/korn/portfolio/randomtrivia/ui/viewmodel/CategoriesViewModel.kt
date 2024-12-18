package com.korn.portfolio.randomtrivia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.TriviaApplication
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import kotlinx.coroutines.flow.Flow
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

class CategoriesViewModel(private val triviaRepository: TriviaRepository) : ViewModel() {
    // local categories that have saved questions
    private val playedCategories: Flow<List<Pair<Category, QuestionCount>>> =
        triviaRepository.localCategories.map { categories ->
            categories.filter { (_, questionCount) ->
                questionCount.run {
                    easy + medium + hard > 0
                }
            }
        }

    val categories: Flow<List<CategoryDisplay>> =
        combine(playedCategories, triviaRepository.remoteCategories) { played, remote ->
            remote.map { r ->
                val playedCat = played.firstOrNull { it.first.id == r.first.id }
                r.asDisplay(playedQuestions = playedCat?.second?.total ?: 0)
            } + played
                .filter { p -> !remote.any { it.first.id == p.first.id } }
                .map { p ->
                    p.asDisplay(playedQuestions = p.second.total)
                }
        }

    fun getQuestions(categoryId: Int, onDone: (List<Question>) -> Unit) {
        viewModelScope.launch {
            triviaRepository.getLocalQuestions(categoryId).let(onDone)
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