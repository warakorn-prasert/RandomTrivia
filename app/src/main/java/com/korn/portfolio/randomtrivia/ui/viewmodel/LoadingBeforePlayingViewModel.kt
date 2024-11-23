package com.korn.portfolio.randomtrivia.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.korn.portfolio.randomtrivia.TriviaApplication
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.network.model.ResponseCode
import com.korn.portfolio.randomtrivia.repository.GameOption
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import com.korn.portfolio.randomtrivia.ui.common.GameFetchStatus
import com.korn.portfolio.randomtrivia.ui.common.displayName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

private const val PREPARE_TEXT = "Preparing to fetch"

private val ResponseCode.message: String
    get() = when (this) {
        ResponseCode.SUCCESS -> "Successful."
        ResponseCode.NO_RESULTS -> "Asked for too many questions."
        ResponseCode.INVALID_PARAMS -> "Outdated network request format."
        ResponseCode.TOKEN_NOT_EXIST -> "Taken too long."
        ResponseCode.TOKEN_EMPTY -> "Asked for too many questions."
        ResponseCode.RATE_LIMIT -> "Fetch too soon. Wait for 5 seconds and retry."
        ResponseCode.UNSUPPORTED -> "Unhandled result code."
    }

private fun List<GameSetting>.toGameOptions(): List<GameOption> =
    map {
        GameOption(
            category = it.category,
            difficulty = it.difficulty,
            type = null,  // random
            amount = it.amount
        )
    }

class LoadingBeforePlayingViewModel(
    private val onDone: (Game) -> Unit,
    private val onlineMode: Boolean,
    private val settings: List<GameSetting>,
    private val triviaRepository: TriviaRepository
) : ViewModel() {
    val fetchStatus: StateFlow<GameFetchStatus> get() = mutableFetchStatus
    private val mutableFetchStatus = MutableStateFlow<GameFetchStatus>(GameFetchStatus.Loading)

    var statusText: String by mutableStateOf(PREPARE_TEXT)
        private set

    // 0f..1f
    var progress: Float by mutableFloatStateOf(0f)
        private set

    private var fetchJob: Job = Job().apply { complete() }

    fun cancelFetch() {
        fetchJob.cancel()
    }

    fun fetch() {
        viewModelScope.launch {
            if (fetchJob.isActive) {
                fetchJob.cancel()
                fetchJob.join()
            }
            fetchJob = viewModelScope.launch {
                try {
                    mutableFetchStatus.emit(GameFetchStatus.Loading)
                    progress = 0f
                    statusText = PREPARE_TEXT
                    mutableFetchStatus.emit(
                        try {
                            var currentSettingDisplayName = ""
                            val (responseCode, game) = triviaRepository.fetchNewGame(
                                settings.toGameOptions(),
                                !onlineMode
                            ) { currentIdx ->
                                val s = settings[currentIdx]
                                progress = currentIdx.toFloat() / settings.size
                                currentSettingDisplayName = "${s.amount}x (${s.difficulty.displayName}) ${s.category.displayName}"
                                statusText =
                                    "${currentIdx + 1}/${settings.size} - fetching $currentSettingDisplayName"
                            }
                            if (responseCode == ResponseCode.SUCCESS)
                                GameFetchStatus.Success(game)
                            else
                                GameFetchStatus.Error(/*"[${responseCode.name}]*/ "\"${responseCode.message}\" on $currentSettingDisplayName")
                        } catch (e: retrofit2.HttpException) {
                            GameFetchStatus.Error(
                                if (e.code() == 429) ResponseCode.RATE_LIMIT.message
//                                else "Unhandled network request error (${e.message})"
                                else "Failed to load."
                            )
                        } catch (_: CancellationException) {
                            GameFetchStatus.Error("Canceled.")
                        } catch (_: Exception) {
//                            GameFetchStatus.Error("Unhandled error (${e.message})")
                            GameFetchStatus.Error("Failed to load.")
                            // TODO : Handle "Unable to resolve host ..."
                        }
                    )
                    when (val f = fetchStatus.value) {
                        is GameFetchStatus.Error -> statusText = f.message
                        GameFetchStatus.Loading -> {}
                        is GameFetchStatus.Success -> {
                            progress = 1f
                            if (onlineMode) {
                                statusText = "Saving questions."
                                triviaRepository.saveQuestions(f.game)
                            }
                            delay(1000)
                            statusText = "Finished."
                            delay(1000)
                            withContext(Dispatchers.Main) {
                                onDone(f.game)
                            }
                        }
                    }
                } catch (_: CancellationException) {}
            }
        }
    }

    init {
        fetch()
    }

    class Factory(
        private val onlineMode: Boolean,
        private val settings: List<GameSetting>,
        private val onDone: (Game) -> Unit
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            val application = checkNotNull(extras[APPLICATION_KEY]) as TriviaApplication
            return LoadingBeforePlayingViewModel(
                onDone,
                onlineMode,
                settings,
                application.triviaRepository,
            ) as T
        }
    }
}