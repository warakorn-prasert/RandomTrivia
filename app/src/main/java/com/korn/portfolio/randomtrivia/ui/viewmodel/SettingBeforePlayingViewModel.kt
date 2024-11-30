package com.korn.portfolio.randomtrivia.ui.viewmodel

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.korn.portfolio.randomtrivia.TriviaApplication
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import com.korn.portfolio.randomtrivia.ui.common.GameSettingSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.Serializable

@Parcelize  // For rememberSaveable
@Serializable(with = GameSettingSerializer::class)  // For navigation argument
data class GameSetting(
    val category: @RawValue Category?,  // null = random
    val difficulty: Difficulty?,  // null = random
    val amount: Int
) : Parcelable {
    companion object {
        const val MAX_AMOUNT = 50
        const val MIN_AMOUNT = 1
    }
}

class SettingBeforePlayingViewModel(triviaRepository: TriviaRepository) : ViewModel() {
    val onlineMode: StateFlow<Boolean> get() = mutableOnlineMode
    private val mutableOnlineMode = MutableStateFlow(triviaRepository.remoteCategories.value.isNotEmpty())

    fun changeOnlineMode(online: Boolean) {
        mutableOnlineMode.value = online
    }

    // If online, un-fetched questionCount's easy, medium, hard always equal 0.
    // Else, every questionCount's total = easy + medium + hard & total > 0
    val categoriesWithQuestionCounts: Flow<List<Pair<Category, QuestionCount>>> =
        combine(
            onlineMode,
            triviaRepository.remoteCategories,
            triviaRepository.localCategories
        ) { online, remote, local ->
            (if (online) remote else local)
                .filter { it.second.total > 0 }
                .sortedBy { it.first.name }
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