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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

const val MAX_AMOUNT = 50
const val MIN_AMOUNT = 1

data class GameSetting(
    val category: Category?,  // null = random  // TODO : Change to categoryId
    val difficulty: Difficulty?,  // null = random
    val amount: Int
) {
    // TODO (maybe) : Remove downloadable property from Category to use default equals()
    // Ignore category name and downloadable
    override fun equals(other: Any?): Boolean {
        return if (other !is GameSetting) super.equals(other)
        else {
            val cat = category?.id == other.category?.id
            val diff = difficulty == other.difficulty
            val amount = amount == other.amount
            cat && diff && amount
        }
    }

    override fun hashCode(): Int {
        var result = category?.id.hashCode()
        result = 31 * result + (difficulty?.hashCode() ?: 0)
        result = 31 * result + amount
        return result
    }
}

private val allDifficulties: List<Difficulty?> = listOf(null) + Difficulty.entries

private val QuestionCount?.difficultiesWithQuestions: List<Difficulty?>
    get() =
        if (this == null) allDifficulties
        else mutableListOf<Difficulty?>(null).apply {
            if (easy > 0) add(Difficulty.EASY)
            if (medium > 0) add(Difficulty.MEDIUM)
            if (hard > 0) add(Difficulty.HARD)
        }

private val Pair<Category, QuestionCount>?.difficultiesWithQuestions: List<Difficulty?>
    get() = if (this == null) allDifficulties else second.difficultiesWithQuestions

private interface SettingBeforePlaying {
    val categoriesFetchStatus: StateFlow<FetchStatus>
    val questionCountFetchStatus: StateFlow<FetchStatus>
    fun fetchCategories()
    fun fetchQuestionCountIfNeedTo()

    val settings: StateFlow<List<GameSetting>>
    val canAddMoreSetting: Flow<Boolean>
    fun addSetting()
    fun removeSetting(setting: GameSetting)
    fun submit(action: (onlineMode: Boolean, settings: List<GameSetting>) -> Unit)

    val category: StateFlow<Category?>
    val difficulty: StateFlow<Difficulty?>
    val amount: StateFlow<Int>
    val categories: Flow<List<Category?>>
    val difficulties: Flow<List<Difficulty?>>
    val maxAmount: Flow<Int>
    fun selectCategory(category: Category?)
    fun selectDifficulty(difficulty: Difficulty?)
    fun selectAmount(amount: Int)

    val canStartGame: Flow<Boolean>
    val onlineMode: StateFlow<Boolean>
    fun changeOnlineMode(value: Boolean)
}

class SettingBeforePlayingViewModel(
    private val triviaRepository: TriviaRepository
) : ViewModel(), SettingBeforePlaying {
    /*
     * Part 1/4 : Fetching categories and question counts
     */

    private var categoriesFetchJob: Job = Job().apply { complete() }
    private var questionCountFetchJob: Job = Job().apply { complete() }
    private class CancellationToRestartException : CancellationException()
    private class CancellationToGoOfflineException : CancellationException()

    override val categoriesFetchStatus: StateFlow<FetchStatus> get() = mutableCategoriesFetchStatus
    private val mutableCategoriesFetchStatus = MutableStateFlow<FetchStatus>(FetchStatus.Success)

    override val questionCountFetchStatus: StateFlow<FetchStatus> get() = mutableQuestionCountFetchStatus
    private val mutableQuestionCountFetchStatus = MutableStateFlow<FetchStatus>(FetchStatus.Success)

    override fun fetchCategories() {
        viewModelScope.launch {
            if (categoriesFetchJob.isActive) {
                categoriesFetchJob.cancel(CancellationToRestartException())
                categoriesFetchJob.join()
            }
            categoriesFetchJob = viewModelScope.launch {
                mutableCategoriesFetchStatus.emit(
                    try {
                        mutableCategoriesFetchStatus.emit(FetchStatus.Loading)
                        mutableSettings.emit(emptyList())
                        delay(1000L)  // make progress indicator not look flickering
                        triviaRepository.fetchCategories()
                        FetchStatus.Success
                    } catch (_: CancellationToRestartException) {
                        FetchStatus.Loading
                    } catch (_: CancellationToGoOfflineException) {
                        FetchStatus.Success
                    } catch (_: Exception) {  // TODO : Handle network exceptions.
                        FetchStatus.Error("Failed to load.")
                    }
                )
            }
        }
    }

    override fun fetchQuestionCountIfNeedTo() {
        viewModelScope.launch {
            fetchQuestionCountIfNeedTo(category.first()?.id)
        }
    }

    // Use this function after updating category to ensure it uses the new category id.
    private suspend fun fetchQuestionCountIfNeedTo(categoryId: Int?) {
        if (questionCountFetchJob.isActive) {
            questionCountFetchJob.cancel(CancellationToRestartException())
            questionCountFetchJob.join()
        }
        questionCountFetchJob = viewModelScope.launch(viewModelScope.coroutineContext) {
            mutableQuestionCountFetchStatus.emit(
                try {
                    val alreadyFetched = categoryId == null
                            || categoriesWithQuestionCounts.first()
                        .first { it.first.id == categoryId }
                        .second.run {
                            total == easy + medium + hard
                        }
                    if (categoryId != null && onlineMode.value && !alreadyFetched) {
                        mutableQuestionCountFetchStatus.emit(FetchStatus.Loading)
                        delay(1000L)  // make progress indicator not look flickering
                        triviaRepository.fetchQuestionCount(categoryId)
                    }
                    FetchStatus.Success
                } catch (_: CancellationToRestartException) {
                    FetchStatus.Loading
                } catch (_: CancellationToGoOfflineException) {
                    FetchStatus.Success
                } catch (e: Exception) {
                    FetchStatus.Error("Failed to load.")
                    // TODO : Handle "Unable to resolve host ..."
                }
            )
        }
    }

    /*
     * Part 2/4 : Game settings
     */

    override val settings: StateFlow<List<GameSetting>> get() = mutableSettings
    private val mutableSettings = MutableStateFlow(emptyList<GameSetting>())

    override val canAddMoreSetting: Flow<Boolean>
        get() = combine(categories, categoriesFetchStatus) { cats, fet, ->
            fet == FetchStatus.Success && cats.isNotEmpty()
        }

    override fun addSetting() {
        viewModelScope.launch {
            mutableSettings.emit(settings.value + GameSetting(
                category = category.first(),
                difficulty = difficulty.first(),
                amount = amount.first()
            ))
        }
    }

    override fun removeSetting(setting: GameSetting) {
        viewModelScope.launch {
            val idx = settings.value.indexOfFirst {
                it.category?.id == setting.category?.id
                        && it.difficulty == setting.difficulty
                        && it.amount == setting.amount
            }
            mutableSettings.emit(settings.value.run { subList(0, idx) + drop(idx + 1) })
        }
    }

    override fun submit(action: (onlineMode: Boolean, settings: List<GameSetting>) -> Unit) {
        action(onlineMode.value, settings.value)
    }

    /*
     * Part 3/4 : Dialog options
     */

    override val category: StateFlow<Category?> get() = mutableCategory
    private val mutableCategory = MutableStateFlow<Category?>(null)

    override val difficulty: StateFlow<Difficulty?> get() = mutableDifficulty
    private val mutableDifficulty = MutableStateFlow<Difficulty?>(null)

    override val amount: StateFlow<Int> get() = mutableAmount
    private val mutableAmount = MutableStateFlow(MIN_AMOUNT)

    // If online, un-fetched questionCount's easy, medium, hard always equal 0.
    // Else, every questionCount's total = easy + medium + hard & total > 0
    private val categoriesWithQuestionCounts: Flow<List<Pair<Category, QuestionCount>>>
        get() = combine(onlineMode, triviaRepository.remoteCategories.asFlow(), triviaRepository.localCategories) { online, remote, local ->
            (if (online) remote else local)
                .filter { it.second.total > 0 }
                .sortedBy { it.first.name }
        }

    // categoriesWithQuestionCounts minus settings
    override val categories: Flow<List<Category?>>
        get() = combine(categoriesWithQuestionCounts, settings) { cats, sets ->
            if (cats.isEmpty()) emptyList()
            else {
                // Exclude category in settings whose all difficulties are used.
                val catIdsToExclude: List<Int?> = sets
                    .filter { set ->
                        val usedCombinations = sets.count { it.category?.id == set.category?.id }
                        val possibleCombinations =
                            // Random category has all difficulties
                            if (set.category == null) allDifficulties.size
                            else cats
                                .first { it.first.id == set.category.id }
                                .difficultiesWithQuestions
                                .size
                        usedCombinations == possibleCombinations
                    }
                    .map { it.category?.id }  // contains duplicates
                // Return random category + starting categories - excluded categories
                (listOf(null) + cats.sortedBy { it.first.name })
                    .filter { all ->
                        all?.first?.id !in catIdsToExclude
                    }
                    .map { it?.first }
            }
        }

    override val difficulties: Flow<List<Difficulty?>>
        get() = combine(category, settings, questionCountFetchStatus) { cat, sets, _ ->
            val allDiffs =
                if (cat == null) allDifficulties
                else categoriesWithQuestionCounts.first()
                    .first { it.first.id == cat.id }
                    .difficultiesWithQuestions
            val usedDiffs = sets.filter { it.category?.id == cat?.id }.map { it.difficulty }
            allDiffs - usedDiffs
        }

    override val maxAmount: Flow<Int>
        get() = difficulty.map { diff ->
            val cat = category.first()
            val cats = categoriesWithQuestionCounts.first()
            if (cat == null) MAX_AMOUNT
            else cats
                .first { it.first.id == cat.id }
                .second
                .run {
                    when (diff) {
                        Difficulty.EASY -> easy
                        Difficulty.MEDIUM -> medium
                        Difficulty.HARD -> hard
                        else -> total
                    }
                }
                .coerceAtMost(MAX_AMOUNT)
        }

    override fun selectCategory(category: Category?) {
        viewModelScope.launch {
            mutableCategory.emit(category)
            fetchQuestionCountIfNeedTo(category?.id)
        }
    }

    override fun selectDifficulty(difficulty: Difficulty?) {
        viewModelScope.launch {
            mutableDifficulty.emit(difficulty)
        }
    }

    override fun selectAmount(amount: Int) {
        viewModelScope.launch {
            mutableAmount.emit(amount)
        }
    }

    /*
     * Part 4/4 : Other UI statuses
     */

    override val canStartGame: Flow<Boolean>
        get() = settings.map { it.isNotEmpty() }

    override val onlineMode: StateFlow<Boolean> get() = mutableOnlineMode
    private val mutableOnlineMode = MutableStateFlow(!triviaRepository.remoteCategories.value.isNullOrEmpty())

    override fun changeOnlineMode(value: Boolean) {
        viewModelScope.launch {
            // Change mode
            mutableOnlineMode.emit(value)
            // Clear settings
            mutableSettings.emit(emptyList())
            // Fetch categories if needed
            if (!value) {
                categoriesFetchJob.cancel(CancellationToGoOfflineException())
                questionCountFetchJob.cancel(CancellationToGoOfflineException())
                categoriesFetchJob.join()
                questionCountFetchJob.join()
                // In case of switching from failed fetch to offline.
                if (categoriesFetchStatus.value !is FetchStatus.Error) {
                    mutableCategoriesFetchStatus.emit(FetchStatus.Success)
                }
            } else if (triviaRepository.remoteCategories.asFlow().first().isEmpty()) {
                // Fetch and wait until finish
                fetchCategories()
            }
        }
    }

    init {
        viewModelScope.launch {
            onlineMode.collectLatest { _ ->
                mutableCategory.emit(null)
                mutableDifficulty.emit(null)
                mutableAmount.emit(MIN_AMOUNT)
            }
        }
        viewModelScope.launch {
            categories.collectLatest { cats ->
                val cat = mutableCategory.value
                val newCat = when {
                    cats.isEmpty() -> null
                    cat?.id !in cats.map { it?.id } -> cats.first()
                    else -> cat
                }
                mutableCategory.emit(newCat)
                fetchQuestionCountIfNeedTo(newCat?.id)
            }
        }
        viewModelScope.launch {
            difficulties.collectLatest { diffs ->
                mutableDifficulty.emit(diffs.firstOrNull())
            }
        }
        viewModelScope.launch {
            difficulty.collectLatest { _ ->
                mutableAmount.emit(MIN_AMOUNT)
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