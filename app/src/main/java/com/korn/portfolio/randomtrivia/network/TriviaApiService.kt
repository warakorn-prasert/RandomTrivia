package com.korn.portfolio.randomtrivia.network

import com.korn.portfolio.randomtrivia.model.Categories
import com.korn.portfolio.randomtrivia.model.CategoryCount
import com.korn.portfolio.randomtrivia.model.Questions
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApiService {
    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") categoryId: Int,
        @Query("difficulty") difficulty: String
    ): Questions

    @GET("api_category.php")
    suspend fun getCategories(): Categories

    @GET("api_count.php")
    suspend fun getDetail(@Query("category") categoryId: Int): CategoryCount
}