package com.korn.portfolio.randomtrivia.ui.common

import com.korn.portfolio.randomtrivia.database.model.Game

sealed interface FetchStatus {
    data object Loading : FetchStatus
    data class Error(val message: String) : FetchStatus
    data object Success : FetchStatus
}

sealed interface GameFetchStatus {
    data object Loading : GameFetchStatus
    data class Error(val message: String) : GameFetchStatus
    data class Success(val game: Game) : GameFetchStatus
}