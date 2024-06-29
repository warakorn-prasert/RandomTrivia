package com.korn.portfolio.randomtrivia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.korn.portfolio.randomtrivia.database.dao.CategoryDao
import com.korn.portfolio.randomtrivia.database.dao.GameDao
import com.korn.portfolio.randomtrivia.database.dao.QuestionDao
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.GameQuestion
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.entity.GameAnswer
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.network.TriviaApiClient
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.network.model.ResponseCode
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import java.util.Date

/*
    Note: ('updates by' ~ '--')
    - Category -- fetchCategories()
    - Question -- saveGame()
    - QuestionCount
        - remote
            - total -- fetchCategories() (initially Int.MIN_VALUE)
            - easy/medium/hard -- fetchQuestionCount() (initially Int.MIN_VALUE)
        - local -- questions in local database
 */

// TODO : Test to list every possible exceptions

interface TriviaRepository {
    val remoteCategories: LiveData<List<Pair<Category, QuestionCount>>>
    val categories: Flow<List<Pair<Category, QuestionCount>>>
    val savedGames: Flow<List<Game>>
    suspend fun fetchCategories()
    suspend fun fetchQuestionCount(categoryId: Int)
    suspend fun fetchNewGame(options: List<GameOption>, offline: Boolean = false): Pair<ResponseCode, Game>
    suspend fun saveGame(game: Game)
}

class TriviaRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val questionDao: QuestionDao,
    private val gameDao: GameDao,
    private val triviaApiClient: TriviaApiClient
) : TriviaRepository {
    /* TODO
        - Handle case of server add/rename/delete a category.
            - If local id does not exist in remote, change its id to -1 and keeps decrementing.
        - Don't save duplicate questions.
        - Online & Offline.
     */

    override val remoteCategories: LiveData<List<Pair<Category, QuestionCount>>>
        get() = _remoteCategories

    override val categories: Flow<List<Pair<Category, QuestionCount>>>
        get() = categoryDao.getCategoriesWithQuestions()
            .map { categoriesWithQuestions ->
                categoriesWithQuestions.map { (category, questions) ->
                    var (easy, medium, hard) = listOf(0, 0, 0)
                    questions.forEach {
                        when (it.difficulty) {
                            Difficulty.EASY -> easy++
                            Difficulty.MEDIUM -> medium++
                            Difficulty.HARD -> hard++
                        }
                    }
                    category to QuestionCount(easy + medium + hard, easy, medium, hard)
                }
            }

    override val savedGames: Flow<List<Game>>
        get() = gameDao.getAll()

    override suspend fun fetchCategories() {
        val resp: List<Pair<Category, Int>> = triviaApiClient.getCategories().toList()
        // refresh remote
        _remoteCategories.postValue(resp.map { (category, total) ->
            category to QuestionCount(total, 0, 0, 0)
        })
        // update local (3 cases)
        val savedCategories = categories.single()
        // case: new category
        resp.filter { (category, _) ->
            !savedCategories.any { (savedCategory, _) ->
                category.id == savedCategory.id
            }
        }.forEach { (category, _) ->
            categoryDao.insert(Category(
                name = category.name,
                downloadable = true,
                id = category.id
            ))
        }
        // case: rename or not
        resp.filter { (category, _) ->
            savedCategories.any { (savedCategory, _) ->
                category.id == savedCategory.id
            }
        }.forEach { (category, _) ->
            categoryDao.update(category.copy(downloadable = true))
        }
        // case: delete
        savedCategories.filter { (savedCategory, _) ->
            !resp.any { (category, _) ->
                savedCategory.id == category.id
            }
        }.forEach { (category, _) ->
            categoryDao.insert(category.copy(
                downloadable = false,
                id = categoryDao.getMinId().coerceAtMost(0) - 1
            ))
            categoryDao.delete(category)
        }
    }

    override suspend fun fetchQuestionCount(categoryId: Int) {
        val remote = remoteCategories.value?.toMutableList() ?: mutableListOf()
        val idx = remote.indexOfFirst { it.first.id == categoryId }
        if (idx >= 0) {
            val questionCount = triviaApiClient.getQuestionCount(categoryId)
            _remoteCategories.postValue(remote.apply {
                set(idx, remote[idx].first to questionCount)
            })
        }
    }

    override suspend fun fetchNewGame(options: List<GameOption>, offline: Boolean): Pair<ResponseCode, Game> {
        val questions = mutableListOf<GameQuestion>()
        val game = Game(
            detail = GameDetail(timestamp = Date(), totalTimeSecond = 0),
            questions = questions
        )
        return if (offline) fetchOfflineGame(options, questions, game)
        else fetchOnlineGame(options, questions, game)
    }

    private suspend fun fetchOnlineGame(
        options: List<GameOption>,
        questions: MutableList<GameQuestion>,
        game: Game
    ): Pair<ResponseCode, Game> {
        // get token
        val (respCode1, token) = triviaApiClient.getToken()
        if (respCode1 != ResponseCode.SUCCESS) return respCode1 to game
        // get questions
        for (option in options) {
            val (respCode2, fetchedQuestions) = triviaApiClient.getQuestions(
                amount = option.amount,
                categoryId = option.category?.id,
                difficulty = option.difficulty,
                type = option.type,
                token = token
            )
            if (respCode2 != ResponseCode.SUCCESS) return respCode1 to game
            questions.addAll(fetchedQuestions.map { question ->
                GameQuestion(
                    question = question,
                    answer = GameAnswer(
                        gameId = game.detail.gameId,
                        questionId = question.id,
                        answer = "",
                        categoryId = question.categoryId
                    ),
                    category = question.categoryId?.let { categoryDao.getById(it) }
                )
            })
            delay(6000)  // rate limit = 1 query per 5 seconds
        }
        return ResponseCode.SUCCESS to game
    }

    private suspend fun fetchOfflineGame(
        options: List<GameOption>,
        questions: MutableList<GameQuestion>,
        game: Game
    ): Pair<ResponseCode, Game> {
        for (option in options) {
            val fetchedQuestions = when {
                option.category == null && option.difficulty == null ->
                    questionDao.getBy(
                        amount = option.amount,
                        excluded = questions.map { it.question.id }
                    )
                option.category == null && option.difficulty != null ->
                    questionDao.getBy(
                        difficulty = option.difficulty,
                        amount = option.amount,
                        excluded = questions.map { it.question.id }
                    )
                option.category != null && option.difficulty == null ->
                    questionDao.getBy(
                        categoryId = option.category.id,
                        amount = option.amount,
                        excluded = questions.map { it.question.id }
                    )
                option.category != null && option.difficulty != null ->
                    questionDao.getBy(
                        categoryId = option.category.id,
                        difficulty = option.difficulty,
                        amount = option.amount,
                        excluded = questions.map { it.question.id }
                    )
                // TODO (Later) : Include `option.type` in search
                else -> emptyList()
            }
            if (fetchedQuestions.size != option.amount) return ResponseCode.NO_RESULTS to game
            questions.addAll(fetchedQuestions.map { question ->
                GameQuestion(
                    question = question,
                    answer = GameAnswer(
                        gameId = game.detail.gameId,
                        questionId = question.id,
                        answer = "",
                        categoryId = question.categoryId
                    ),
                    category = question.categoryId?.let { categoryDao.getById(it) }
                )
            })
        }
        return ResponseCode.SUCCESS to game
    }

    override suspend fun saveGame(game: Game) {
        val (detail, questions) = game
        gameDao.insert(detail)
        questions.forEach { question ->
            if (!questionDao.exists(question.question.id, question.question.question)) {
                questionDao.insert(question.question)
            }
            gameDao.insertAnswer(question.answer)
        }
    }

    private val _remoteCategories = MutableLiveData<List<Pair<Category, QuestionCount>>>(emptyList())
}