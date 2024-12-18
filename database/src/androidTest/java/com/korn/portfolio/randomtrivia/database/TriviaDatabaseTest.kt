package com.korn.portfolio.randomtrivia.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.korn.portfolio.randomtrivia.database.dao.CategoryDao
import com.korn.portfolio.randomtrivia.database.dao.GameDao
import com.korn.portfolio.randomtrivia.database.dao.QuestionDao
import com.korn.portfolio.randomtrivia.database.model.CategoryWithQuestions
import com.korn.portfolio.randomtrivia.database.model.Difficulty
import com.korn.portfolio.randomtrivia.database.model.entity.Category
import com.korn.portfolio.randomtrivia.database.model.entity.GameAnswer
import com.korn.portfolio.randomtrivia.database.model.entity.GameDetail
import com.korn.portfolio.randomtrivia.database.model.entity.Question
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

private val CategoryWithQuestions.ids: Set<String>
    get() = questions.map { it.id.toString() }.toSet() + category.id.toString()

private val Collection<CategoryWithQuestions>.ids: Set<String>
    get() = flatMap { it.ids }.toSet()

@RunWith(AndroidJUnit4::class)
class TriviaDatabaseTest {
    private lateinit var db: TriviaDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var questionDao: QuestionDao
    private lateinit var gameDao: GameDao

    private val cats = Array(5) { Category("cat $it", it) }

    private val catQns = Array(10) {
        Question(
            question = "Question $it",
            difficulty = Difficulty.entries.random(),
            categoryId = it % cats.size,
            correctAnswer = "correct answer of question $it",
            incorrectAnswers = List(3) { itt -> "incorrect answer $itt of question $it" }
        )
    }

    private val uncatQns = Array(2) {
        Question(
            question = "Uncat question $it",
            difficulty = Difficulty.entries.random(),
            categoryId = null,
            correctAnswer = "correct answer of uncat question $it",
            incorrectAnswers = listOf("incorrect answer of uncat question $it")
        )
    }

    private val qns = catQns + uncatQns

    private val catsWithQns = cats.map { cat ->
        CategoryWithQuestions(cat, catQns.filter { it.categoryId == cat.id })
    }

    private val gameDetails = Array(3) {
        GameDetail(Date(), 30 * (it + 1))
    }

    private val gameAnswers = Array(qns.size) {
        GameAnswer(
            gameId = gameDetails[it % gameDetails.size].gameId,
            questionId = qns[it].id,
            answer = "answer of ${qns[it].question}",
            categoryId = qns[it].categoryId
        )
    }

    @Before
    fun create_db() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, TriviaDatabase::class.java).build()
        categoryDao = db.categoryDao()
        questionDao = db.questionDao()
        gameDao = db.gameDao()

        categoryDao.insert(*cats)
        questionDao.insert(*qns)
    }

    @After
    fun close_db() = runBlocking {
        db.close()
    }

    @Test
    fun insert_update_delete_upsert() = runBlocking {
        val update = arrayOf(
            cats[1].copy(name = "update cat 1"),
            cats[2].copy(name = "update cat 2")
        )
        categoryDao.update(*update)
        val delete = cats.sliceArray(3..4)
        categoryDao.delete(*delete)
        val upsert = arrayOf(
            cats[0].copy(name = "upsert cat 0"),
            cats[4]
        )
        categoryDao.upsert(*upsert)
        assertEquals(
            listOf(upsert[0], *update, upsert[1]),
            categoryDao.getCategoriesWithQuestions().first().map { it.category }
        )
    }

    @Test
    fun get_categories_with_questions() = runBlocking {
        assertEquals(
            catsWithQns.ids,
            categoryDao.getCategoriesWithQuestions().first().ids
        )
    }

    @Test
    fun get_category_by_id() = runBlocking {
        val minId = categoryDao.getMinId()
        val minIdCat = categoryDao.getOneBy(minId)
        assertEquals(cats[0], minIdCat)
    }

    @Test
    fun get_uncat_questions() = runBlocking {
        assertEquals(
            uncatQns.toList(),
            questionDao.getUncategorized().first()
        )
    }

    @Test
    fun get_questions_by_category() = runBlocking {
        assertEquals(
            qns.filter { it.categoryId == 0 },
            questionDao.getByCategory(0)
        )
    }

    @Test
    fun delete_questions_by_category() = runBlocking {
        questionDao.deleteByCategory(0)
        assert(questionDao.getByCategory(0).isEmpty())
    }

    @Test
    fun insert_delete_get_games() = runBlocking {
        gameDao.insert(*gameDetails)
        gameDao.insertAnswer(*gameAnswers)
        gameDao.delete(gameDetails[0])
        assertEquals(
            gameDetails.drop(1).map { it.gameId }.toSet(),
            gameDao.getAll().first().map { it.detail.gameId }.toSet()
        )
    }

    @Test
    fun uncategorize_question() = runBlocking {
        categoryDao.delete(cats[0])
        assert(questionDao.getByCategory(0).isEmpty())
    }

    @Test
    fun update_answer() = runBlocking {
        gameDao.insert(*gameDetails)
        gameDao.insertAnswer(*gameAnswers)
        gameDao.updateAnswer(gameAnswers[0].copy(answer = "new answer"))
        assertEquals(
            "new answer",
            gameDao.getAll().first()
                .first { it.detail.gameId == gameAnswers[0].gameId }
                .questions.first { qn ->
                    qn.answer.questionId == gameAnswers[0].questionId
                }
                .answer.answer
        )
    }
}