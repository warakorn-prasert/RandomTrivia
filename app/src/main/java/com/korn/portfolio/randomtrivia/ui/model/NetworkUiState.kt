package com.korn.portfolio.randomtrivia.ui.model

sealed interface NetworkUiState {
    data object Loading : NetworkUiState
    data class Error(val error: Exception) : NetworkUiState
    data object Success : NetworkUiState
}