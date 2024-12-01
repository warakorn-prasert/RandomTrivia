package com.korn.portfolio.randomtrivia.repository

import com.korn.portfolio.randomtrivia.database.dao.CategoryDao
import com.korn.portfolio.randomtrivia.database.dao.GameDao
import com.korn.portfolio.randomtrivia.database.dao.QuestionDao
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.Game
import com.korn.portfolio.randomtrivia.database.model.GameQuestion
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.entity.GameAnswer
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import com.korn.portfolio.randomtrivia.network.TriviaApiClient
import com.korn.portfolio.randomtrivia.network.model.QuestionCount
import com.korn.portfolio.randomtrivia.network.model.ResponseCode
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.UUID
import kotlin.time.measureTime

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

interface TriviaRepository {
    val remoteCategories: StateFlow<List<Pair<Category, QuestionCount>>>
    val localCategories: Flow<List<Pair<Category, QuestionCount>>>
    val savedGames: Flow<List<Game>>
    suspend fun fetchCategories()
    suspend fun fetchQuestionCount(categoryId: Int)
    suspend fun fetchNewGame(
        options: List<GameOption>,
        offline: Boolean = false,
        processLog: suspend (currentIdx: Int) -> Unit
    ): Pair<ResponseCode, Game>
    suspend fun saveGame(game: Game)
    suspend fun deleteLocalCategories(vararg id: Int)
    suspend fun deleteAllLocalCategories()
    suspend fun deleteGame(gameId: UUID)
    suspend fun getLocalQuestions(categoryId: Int): List<Question>
    suspend fun saveQuestions(game: Game)
}

class TriviaRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val questionDao: QuestionDao,
    private val gameDao: GameDao,
    private val triviaApiClient: TriviaApiClient
) : TriviaRepository {
    override val remoteCategories: StateFlow<List<Pair<Category, QuestionCount>>>
        get() = _remoteCategories
    private val _remoteCategories = MutableStateFlow<List<Pair<Category, QuestionCount>>>(emptyList())

    override val localCategories: Flow<List<Pair<Category, QuestionCount>>>
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
        val remotes: List<Pair<Category, Int>> = triviaApiClient.getCategories().toList()
        // refresh remote
        _remoteCategories.emit(remotes.map { (category, total) ->
            category to QuestionCount(total, 0, 0, 0)
        })
        // update local (3 cases)
        val locals = localCategories.first()
        // case: new category
        remotes.filter { (remote, _) ->
            !locals.any { (local, _) ->
                remote.id == local.id
            }
        }.forEach { (remote, _) ->
            categoryDao.insert(Category(
                name = remote.name,
                id = remote.id
            ))
        }
        // case: rename
        remotes.filter { (remote, _) ->
            locals.any { (local, _) ->
                remote.id == local.id && remote.name != local.name
            }
        }.forEach { (remote, _) ->
            categoryDao.update(remote)
        }
        // case: delete
        locals.filter { (local, _) ->
            !remotes.any { (remote, _) ->
                local.id == remote.id
            } && local.id >= 0
        }.forEach { (local, _) ->
            categoryDao.insert(local.copy(
                id = categoryDao.getMinId().coerceAtMost(0) - 1
            ))
            categoryDao.delete(local)
        }
    }

    // In case of multiple asynchronous fetches
    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    private val fetchQuestionCountContext = newSingleThreadContext("fetchQuestionCount")

    override suspend fun fetchQuestionCount(categoryId: Int) {
        require(remoteCategories.value.isNotEmpty()) {
            "Must call fetchCategories() before fetchQuestionCount()."
        }
        require(remoteCategories.value.any { it.first.id == categoryId}) {
            "Category ID must exist."
        }
        val idx = remoteCategories.value.indexOfFirst { it.first.id == categoryId }
        if (idx >= 0) {
            val questionCount = triviaApiClient.getQuestionCount(categoryId)
            withContext(fetchQuestionCountContext) {
                _remoteCategories.emit(
                    remoteCategories.value.toMutableList().apply {
                        set(idx, get(idx).first to questionCount)
                    }
                )
            }
        }
    }

    override suspend fun fetchNewGame(
        options: List<GameOption>,
        offline: Boolean,
        processLog: suspend (currentIdx: Int) -> Unit
    ): Pair<ResponseCode, Game> {
        val questions = mutableListOf<GameQuestion>()
        val game = Game(
            detail = GameDetail(timestamp = Date(), totalTimeSecond = 0),
            questions = questions
        )
        return if (offline) fetchOfflineGame(options, questions, game, processLog)
        else fetchOnlineGame(options, questions, game, processLog)
    }

    private suspend fun fetchOnlineGame(
        options: List<GameOption>,
        questions: MutableList<GameQuestion>,
        game: Game,
        processLog: suspend (currentIdx: Int) -> Unit
    ): Pair<ResponseCode, Game> {
        require(remoteCategories.value.isEmpty()) {
            "Must call fetchCategories() before fetchOnlineGame()."
        }
        // get token
        val (respCode1, token) = triviaApiClient.getToken()
        if (respCode1 != ResponseCode.SUCCESS) return respCode1 to game
        // get questions
        options.forEachIndexed { idx, option ->
            processLog(idx)
            measureTime {
                val (respCode2, fetchedQuestions) = triviaApiClient.getQuestions(
                    amount = option.amount,
                    categoryId = option.category?.id,
                    difficulty = option.difficulty,
                    type = option.type,
                    token = token
                )
                if (respCode2 != ResponseCode.SUCCESS) return respCode2 to game
                questions.addAll(fetchedQuestions.map { question ->
                    // sync category and question with local by id
                    // (always use properties from `local<name>` variables)
                    val localCategory = question.categoryId?.let { categoryDao.getOneBy(it) }
                    val localQuestion = questionDao.getOneBy(question.question)
                    val questionId = localQuestion?.id ?: question.id
                    GameQuestion(
                        question = question.copy(
                            categoryId = localCategory?.id,
                            id = questionId
                        ),
                        answer = GameAnswer(
                            gameId = game.detail.gameId,
                            questionId = questionId,
                            answer = "",
                            categoryId = localCategory?.id
                        ),
                        category = localCategory
                    )
                })
            }.let { duration ->
                // delay due to rate limit (1 query per 5 seconds)
                delay((6000 - duration.inWholeMilliseconds).coerceAtLeast(0))
            }
        }
        return ResponseCode.SUCCESS to game
    }

    private suspend fun fetchOfflineGame(
        options: List<GameOption>,
        questions: MutableList<GameQuestion>,
        game: Game,
        processLog: suspend (currentIdx: Int) -> Unit
    ): Pair<ResponseCode, Game> {
        options.forEachIndexed { idx, option ->
            processLog(idx)
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
                    category = question.categoryId?.let { categoryDao.getOneBy(it) }
                )
            })
        }
        return ResponseCode.SUCCESS to game
    }

    override suspend fun saveGame(game: Game) {
        val (detail, questions) = game
        gameDao.insert(detail)
        questions.forEach { question ->
            // sync category and question ids with local
            val localQuestion = questionDao.getOneBy(question.question.question)
            val localCategory = question.question.categoryId?.let { categoryDao.getOneBy(it) }
            if (localQuestion == null) {  // new question
                questionDao.insert(question.question.copy(categoryId = localCategory?.id))
                gameDao.insertAnswer(question.answer.copy(categoryId = localCategory?.id))
            } else {  // old question
                gameDao.insertAnswer(question.answer.copy(questionId = localQuestion.id))
            }
            // assume category already exists, e.g., fetchCategories() before fetchOnlineGame()
        }
    }

    override suspend fun deleteLocalCategories(vararg id: Int) {
        id.forEach {
            categoryDao.delete(Category("", it))
        }
    }

    override suspend fun deleteAllLocalCategories() {
        categoryDao.deleteAll()
    }

    override suspend fun deleteGame(gameId: UUID) {
        gameDao.delete(GameDetail(timestamp = Date(), totalTimeSecond = 0, gameId = gameId))
    }

    override suspend fun getLocalQuestions(categoryId: Int): List<Question> =
        questionDao.getByCategory(categoryId)

    override suspend fun saveQuestions(game: Game) {
        // Save questions
        game.questions.forEach { question ->
            // sync category and question ids with local
            val localQuestion = questionDao.getOneBy(question.question.question)
            val localCategory = question.question.categoryId?.let { categoryDao.getOneBy(it) }
            if (localQuestion == null)  // new question
                questionDao.insert(question.question.copy(categoryId = localCategory?.id))
            // assume category already exists, e.g., fetchCategories() before fetchOnlineGame()
        }
    }
}