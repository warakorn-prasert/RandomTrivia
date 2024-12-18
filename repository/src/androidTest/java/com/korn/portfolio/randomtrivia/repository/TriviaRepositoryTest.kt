package com.korn.portfolio.randomtrivia.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.korn.portfolio.randomtrivia.database.TriviaDatabase
import com.korn.portfolio.randomtrivia.database.dao.CategoryDao
import com.korn.portfolio.randomtrivia.database.dao.GameDao
import com.korn.portfolio.randomtrivia.database.dao.QuestionDao
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.network.TriviaApiClient
import com.korn.portfolio.randomtrivia.network.model.ResponseCode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TriviaRepositoryTest {
    private lateinit var db: TriviaDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var questionDao: QuestionDao
    private lateinit var gameDao: GameDao
    private lateinit var client: TriviaApiClient
    private lateinit var repo: TriviaRepository

    @Before
    fun create_repository() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, TriviaDatabase::class.java
        ).build()
        client = TriviaApiClient()
        categoryDao = db.categoryDao()
        questionDao = db.questionDao()
        gameDao = db.gameDao()
        repo = TriviaRepositoryImpl(categoryDao, questionDao, gameDao, client)

        repo.fetchCategories()
    }

    @Test
    fun fetch_categories() = runBlocking {
        assert(repo.remoteCategories.value.isNotEmpty())
        assert(repo.localCategories.first().isNotEmpty())
    }

    @Test
    fun fetch_question_count() = runBlocking {
        val catId = repo.remoteCategories.value.first().first.id
        repo.fetchQuestionCount(catId)
        val count = repo.remoteCategories.value.first { it.first.id == catId }.second
        assert(count.total > 0)
    }

    @Test
    fun fetch_save_replay_delete_game() = runBlocking {
        // fetch
        val options = listOf(
            GameOption(null, Difficulty.EASY, null, 2),
            GameOption(null, Difficulty.MEDIUM, null, 3),
            GameOption(null, Difficulty.HARD, null, 4)
        )
        val (respCode, game) = repo.fetchNewGame(options, false, {})
        assertEquals(ResponseCode.SUCCESS, respCode)
        assertEquals(2, game.questions.filter { it.question.difficulty == Difficulty.EASY }.size)
        assertEquals(3, game.questions.filter { it.question.difficulty == Difficulty.MEDIUM }.size)
        assertEquals(4, game.questions.filter { it.question.difficulty == Difficulty.HARD }.size)

        // save
        repo.saveGame(game)
        assert(repo.savedGames.first().any { it.detail.gameId == game.detail.gameId })

        // fetch offline
        val (respCode2, game2) = repo.fetchNewGame(options, true, {})
        assertEquals(ResponseCode.SUCCESS, respCode2)
        assertEquals(2, game2.questions.filter { it.question.difficulty == Difficulty.EASY }.size)
        assertEquals(3, game2.questions.filter { it.question.difficulty == Difficulty.MEDIUM }.size)
        assertEquals(4, game2.questions.filter { it.question.difficulty == Difficulty.HARD }.size)

        // delete
        repo.saveGame(game2)
        repo.deleteGame(game.detail.gameId)
        assertEquals(1, repo.savedGames.first().size)
        assertEquals(game2.detail.gameId, repo.savedGames.first().first().detail.gameId)
    }

    @Test
    fun delete_local_categories() = runBlocking {
        val catId = repo.localCategories.first().first().first.id
        repo.deleteLocalCategories(catId)
        assert(repo.localCategories.first().none { it.first.id == catId })
        repo.deleteAllLocalCategories()
        assert(repo.localCategories.first().isEmpty())
    }

    @Test
    fun get_played_questions_by_category() = runBlocking {
        val cats = repo.localCategories.first().slice(0..1).map { it.first }
        val options = listOf(
            GameOption(cats[0], null, null, 2),
            GameOption(cats[1], null, null, 3)
        )
        val (respCode, game) = repo.fetchNewGame(options, false, {})
        assertEquals(ResponseCode.SUCCESS, respCode)
        repo.saveGame(game)
        assertEquals(3, repo.getLocalQuestions(cats[1].id).size)
        assert(repo.getLocalQuestions(cats[1].id).all { it.categoryId == cats[1].id })
    }

    // TODO : Test error handling
    //  - @Test(expected = SomeException::class)
    //  - assertEquals(ResponseCode.RATE_LIMIT, respCode)
}