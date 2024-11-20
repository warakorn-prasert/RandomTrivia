package com.korn.portfolio.randomtrivia.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.TriviaApplication
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import com.korn.portfolio.randomtrivia.ui.common.FetchStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

data class GameSetting(
    val category: Category?,  // null = random
    val difficulty: Difficulty?,  // null = random
    val amount: Int
) {
    companion object {
        const val MAX_AMOUNT = 50
        const val MIN_AMOUNT = 1
    }
}

class SettingBeforePlayingViewModel(
    private val triviaRepository: TriviaRepository
) : ViewModel() {
    val onlineMode: StateFlow<Boolean> get() = mutableOnlineMode
    private val mutableOnlineMode = MutableStateFlow(false).apply {
        // value = !neverFetch
        value = !triviaRepository.remoteCategories.value.isNullOrEmpty()
    }

    fun changeOnlineMode(online: Boolean) {
        mutableOnlineMode.value = online
    }

    // If online, un-fetched questionCount's easy, medium, hard always equal 0.
    // Else, every questionCount's total = easy + medium + hard & total > 0
    val categoriesWithQuestionCounts: Flow<List<Pair<Category, QuestionCount>>> =
        combine(
            onlineMode,
            triviaRepository.remoteCategories.asFlow(),
            triviaRepository.localCategories
        ) { online, remote, local ->
            (if (online) remote else local)
                .filter { it.second.total > 0 }
                .sortedBy { it.first.name }
        }

    private var questionCountFetchJob: Job = Job().apply { complete() }
    private class CancelToRestartException : CancellationException()
    private class CancelToGoOfflineException : CancellationException()

    fun fetchQuestionCountIfNotAlready(
        categoryId: Int?,
        onQuestionCountFetchStatusChange: (FetchStatus) -> Unit
    ) {
        viewModelScope.launch {
            if (questionCountFetchJob.isActive) {
                questionCountFetchJob.cancel(CancelToRestartException())
                questionCountFetchJob.join()
            }
            questionCountFetchJob = viewModelScope.launch(viewModelScope.coroutineContext) {
                onQuestionCountFetchStatusChange(
                    try {
                        val alreadyFetched = categoryId == null
                                || categoriesWithQuestionCounts.first()
                            .first { it.first.id == categoryId }
                            .second.run {
                                total == easy + medium + hard
                            }
                        if (categoryId != null && onlineMode.value && !alreadyFetched) {
                            onQuestionCountFetchStatusChange(FetchStatus.Loading)
                            delay(1000L)  // make progress indicator not look flickering
                            triviaRepository.fetchQuestionCount(categoryId)
                        }
                        FetchStatus.Success
                    } catch (_: CancelToRestartException) {
                        FetchStatus.Loading
                    } catch (_: CancelToGoOfflineException) {
                        FetchStatus.Success
                    } catch (_: Exception) {
                        FetchStatus.Error("Failed to load.")
                        // TODO : Handle "Unable to resolve host ..."
                    }
                )
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as TriviaApplication
                SettingBeforePlayingViewModel(application.triviaRepository)
            }
        }
    }
}