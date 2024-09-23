package com.korn.portfolio.randomtrivia.ui.common

sealed interface FetchStatus {
    data object Loading : FetchStatus
    data class Error(val message: String) : FetchStatus
    data object Success : FetchStatus
}