package com.korn.portfolio.randomtrivia.network

import com.korn.portfolio.randomtrivia.network.model.response.FetchCategories
import com.korn.portfolio.randomtrivia.network.model.response.FetchNewSession
import com.korn.portfolio.randomtrivia.network.model.response.FetchOverall
import com.korn.portfolio.randomtrivia.network.model.response.FetchQuestionCount
import com.korn.portfolio.randomtrivia.network.model.response.FetchQuestions
import com.korn.portfolio.randomtrivia.network.model.response.FetchResetSession
import retrofit2.http.GET
import retrofit2.http.Query

interface TriviaApiService {
    @GET("api_category.php")
    suspend fun getCategories(): FetchCategories

    @GET("api_count.php")
    suspend fun getQuestionCount(@Query("category") categoryId: Int): FetchQuestionCount

    @GET("api_count_global.php")
    suspend fun getOverall(): FetchOverall

    @GET("api_token.php?command=request")
    suspend fun getNewSession(): FetchNewSession

    @GET("api_token?command=reset")
    suspend fun resetSession(@Query("token") token: String): FetchResetSession

    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") categoryId: Int,
        @Query("difficulty") difficulty: String,
        @Query("type") type: String,
        @Query("token") token: String
    ): FetchQuestions
}