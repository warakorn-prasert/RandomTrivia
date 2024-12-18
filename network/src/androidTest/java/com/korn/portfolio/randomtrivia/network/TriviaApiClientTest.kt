package com.korn.portfolio.randomtrivia.network

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TriviaApiClientTest {
    private lateinit var client: TriviaApiClient

    private inline fun assertNoException(block: () -> Unit) {
        var exception: Exception? = null
        try {
            block()
        } catch (e: Exception) {
            exception = e
        }
        assert(exception == null)
    }

    @Before
    fun create_client() {
        client = TriviaApiClient()
    }

    @Test
    fun get_categories() = runBlocking {
        assertNoException {
            client.getCategories()
        }
    }

    @Test
    fun get_token() = runBlocking {
        assertNoException {
            client.getToken()
        }
    }

    @Test
    fun get_question_count() = runBlocking {
        assertNoException {
            val catId = client.getCategories().entries.first().key.id
            client.getQuestionCount(catId)
        }
    }

    @Test
    fun get_questions() = runBlocking {
        assertNoException {
            val catId = client.getCategories().entries.first().key.id
            client.getQuestions(10, catId, null, null, null)
        }
    }
}