package com.korn.portfolio.randomtrivia.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.korn.portfolio.randomtrivia.TriviaApplication
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class QuestionsViewModel(
    categoryId: Int,
    private val triviaRepository: TriviaRepository
) : ViewModel() {
    var categoryName by mutableStateOf("")
        private set

    var questions by mutableStateOf(emptyList<Question>())
        private set

    init {
        viewModelScope.launch {
            categoryName = triviaRepository.localCategories.first()
                .firstOrNull { it.first.id == categoryId }
                ?.first
                ?.name
                ?: "Error (Not found)"
            questions = triviaRepository.getLocalQuestions(categoryId)
        }
    }

    class Factory(private val categoryId: Int) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            val application = checkNotNull(extras[APPLICATION_KEY]) as TriviaApplication
            return QuestionsViewModel(categoryId, application.triviaRepository) as T
        }
    }
}