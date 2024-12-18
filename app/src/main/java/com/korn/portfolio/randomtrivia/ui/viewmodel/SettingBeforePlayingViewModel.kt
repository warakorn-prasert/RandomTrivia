package com.korn.portfolio.randomtrivia.ui.viewmodel

import android.os.Parcelable
import androidx.compose.runtime.mutableStateListOf
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
import com.korn.portfolio.randomtrivia.ui.common.CategoryParceler
import com.korn.portfolio.randomtrivia.ui.common.GameSettingSerializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import kotlinx.serialization.Serializable

@Parcelize  // For rememberSaveable
@Serializable(with = GameSettingSerializer::class)  // For navigation argument
data class GameSetting(
    val category: @WriteWith<CategoryParceler> Category?,  // null = random
    val difficulty: Difficulty?,  // null = random
    val amount: Int
) : Parcelable {
    companion object {
        const val MAX_AMOUNT = 50
        const val MIN_AMOUNT = 1
    }
}

class SettingBeforePlayingViewModel(triviaRepository: TriviaRepository) : ViewModel() {
    // Can't use mutableListOf() because Compose can't snapshot it.
    val settings = mutableStateListOf<GameSetting>()

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

/**
 * Need to state in UI that,
 * - After adding random category and random difficulty setting, categories becomes empty.
 * - After adding random category and non-random difficulty setting,
 *     - cannot add random category and random difficulty setting.
 *     - that difficulty and random difficulty will be excluded from every category choice.
 * - After adding non-random category and random difficulty setting, that category will be excluded.
 *
 * Therefore, recommended steps are,
 * 1. Add non-random category and non-random difficulty settings.
 * 2. Add non-random category and random difficulty settings.
 * 3. Add random category and random difficulty setting, or random category and non-random difficulty settings.
 */
class GameSettingChoiceGetter(
    private val catsWithCounts: List<Pair<Category, QuestionCount>>,
    private val settings: List<GameSetting>
) {
    private val totalQuestionCount = run {
        var easy = 0
        var medium = 0
        var hard = 0
        catsWithCounts.forEach { (_, questionCount) ->
            easy += questionCount.easy
            medium += questionCount.medium
            hard += questionCount.hard
        }
        QuestionCount(easy + medium + hard, easy, medium, hard)
    }

    @Suppress("RecursivePropertyAccessor")
    private val QuestionCount?.difficultiesWithQuestions: List<Difficulty?>
        get() =
            if (this == null) totalQuestionCount.difficultiesWithQuestions
            else mutableListOf<Difficulty?>().apply {
                if (easy > 0) add(Difficulty.EASY)
                if (medium > 0) add(Difficulty.MEDIUM)
                if (hard > 0) add(Difficulty.HARD)
                if (isNotEmpty()) add(0, null)
            }

    // Returns empty list when settings has random category with random difficulty.
    // Also exclude any category in settings with random difficulty.
    @Suppress("ConvertArgumentToSet")
    val categories: List<Category?> =
        if (
            catsWithCounts.isEmpty()
            || settings.any { it.category == null && it.difficulty == null }
            || settings.sumOf { it.amount } == totalQuestionCount.total
        ) emptyList()

        else (catsWithCounts + null)
            .filter { catWithCount ->
                val usedSettings = settings.filter { it.category?.id == catWithCount?.first?.id }

                // Exclude any category in settings with random difficulty.
                if (usedSettings.any { it.difficulty == null }) return@filter false

                val possibleDiffs = catWithCount?.second.difficultiesWithQuestions
                val usedDiffs = usedSettings.map { it.difficulty }
                val unusedDiffs = possibleDiffs - usedDiffs

                // Exclude categories in settings whose all difficulties are used.
                if (unusedDiffs.isEmpty()) return@filter false

                // Or no more questions left to use.
                // This is required when random difficulty is an option.
                val usedAmount = usedSettings.sumOf { it.amount }
                val maxAmount = catWithCount.let { it?.second ?: totalQuestionCount }.total
                maxAmount > usedAmount
            }
            .map { it?.first }
            .run {  // Sort and bring null in front
                filterNotNull().sortedBy { it.name } + (if (contains(null)) listOf(null) else emptyList())
            }

    // Exclude any difficulty that's in settings with random category. Also exclude random difficulty.
    @Suppress("ConvertArgumentToSet")
    fun getDifficulties(category: Category?): List<Difficulty?>  {
        val allDiffs = (catsWithCounts + null)
            .first { it?.first?.id == category?.id }
            ?.second
            .difficultiesWithQuestions
        val usedDiffs = settings.filter { it.category?.id == category?.id }.map { it.difficulty }
        val nullCatDiffs = settings.filter { s -> s.category == null }.map { it.difficulty }
        return (allDiffs - usedDiffs)
            .filter { it !in nullCatDiffs }
            .let {
                if (nullCatDiffs.isNotEmpty()) it.filterNotNull()
                else it
            }
            .let {
                if (null in it) it.filterNotNull().sortedBy { diff -> diff.ordinal } + null
                else it.sortedBy { diff -> diff!!.ordinal }
            }
    }

    fun getMaxAmount(category: Category?, difficulty: Difficulty?): Int {
        val maxAmount = (catsWithCounts + null)
            .first { it?.first?.id == category?.id }
            .let { it?.second ?: totalQuestionCount }
            .run {
                when (difficulty) {
                    Difficulty.EASY -> easy
                    Difficulty.MEDIUM -> medium
                    Difficulty.HARD -> hard
                    else -> total
                }
            }
        val usedAmount: Int = when {
            category == null && difficulty == null -> settings.sumOf { it.amount }
            category == null && difficulty != null ->
                when (difficulty) {
                    Difficulty.EASY -> settings.filter { it.difficulty == Difficulty.EASY }.sumOf { it.amount }
                    Difficulty.MEDIUM -> settings.filter { it.difficulty == Difficulty.MEDIUM }.sumOf { it.amount }
                    Difficulty.HARD -> settings.filter { it.difficulty == Difficulty.HARD }.sumOf { it.amount }
                }
            category != null && difficulty == null ->
                settings.filter { it.category?.id == category.id }.sumOf { it.amount }
            else ->
                settings
                    .filter { it.category?.id == category!!.id && it.difficulty == difficulty }
                    .sumOf { it.amount }
        }
        return (maxAmount - usedAmount).let { finalMaxAmount ->
            // Fix: Having random category or random difficulty in settings doesn't decrease above usedAmount.
            // This may be a better method than figuring out its exact logic.
            val sumUsedAmount = settings.sumOf { it.amount }
            if (finalMaxAmount + sumUsedAmount > totalQuestionCount.total)
                totalQuestionCount.total - sumUsedAmount
            else
                finalMaxAmount
        }
    }
}