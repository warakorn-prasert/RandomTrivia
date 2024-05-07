package com.korn.portfolio.randomtrivia

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun serialize_data_class() {
        @kotlinx.serialization.Serializable
        data class A(val a: Int, val aa: String)
        @kotlinx.serialization.Serializable
        data class B(val b: String, val bb: List<A>)
        val inO = B(
            b = "This is bObject",
            bb = listOf(
                A(a = 0, aa = "a0"),
                A(a = 1, aa = "a1"),
                A(a = 2, aa = "a2"),
            )
        )
        val str = Json.encodeToString(inO)
        val outO: B = Json.decodeFromString(str)
        assertEquals(inO, outO)
    }
}