package com.korn.portfolio.randomtrivia.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.korn.portfolio.randomtrivia.TriviaApplication
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.GameQuestion
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.repository.TriviaRepository
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import kotlin.concurrent.timer

class PlayingViewModel(
    game: Game,
    private val triviaRepository: TriviaRepository
) : ViewModel() {
    var second: Int by mutableIntStateOf(0)
        private set
    private val timer = timer(initialDelay = 2000L, period = 1000L) {
        second++
    }

    private val mutableQuestions = SnapshotStateList<GameQuestion>().apply {
        addAll(game.questions)
    }
    val questions: List<GameQuestion> get() = mutableQuestions

    var currentIdx by mutableIntStateOf(0)
        private set

    fun selectQuestion(questionIdx: Int) {
        this.currentIdx = questionIdx
    }

    fun answer(answer: String) {
        mutableQuestions[currentIdx] = mutableQuestions[currentIdx].let {
            it.copy(
                answer = it.answer.copy(
                    answer = answer
                )
            )
        }
    }

    val submittable: Boolean
        get() = questions.all {
            it.answer.answer in it.question.run {
                incorrectAnswers + correctAnswer
            }
        }

    fun exit(action: () -> Unit) {
        timer.cancel()
        action()
    }

    fun submit(action: (Game) -> Unit) {
        timer.cancel()
        val newGameId = UUID.randomUUID()
        val playedGame = Game(
            detail = GameDetail(Date(), second, newGameId),
            questions = questions.map {
                it.copy(
                    answer = it.answer.copy(
                        gameId = newGameId
                    )
                )
            }
        )
        viewModelScope.launch {
            triviaRepository.saveGame(playedGame)
        }
        action(playedGame)
    }

    class Factory(private val game: Game) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras
        ): T {
            val application = checkNotNull(extras[APPLICATION_KEY]) as TriviaApplication
            return PlayingViewModel(game, application.triviaRepository) as T
        }
    }
}