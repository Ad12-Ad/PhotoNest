package com.example.photonest.core.utils

/**
 * Alternative to Resource class for UI-specific states
 * Can be used alongside Resource for more specific UI handling
 */
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: String) : UiState<Nothing>()
    data class Empty(val message: String = "No data available") : UiState<Nothing>()
}

/**
 * Extension functions for UiState
 */
inline fun <T> UiState<T>.onSuccess(action: (T) -> Unit): UiState<T> {
    if (this is UiState.Success) {
        action(data)
    }
    return this
}

inline fun <T> UiState<T>.onError(action: (String) -> Unit): UiState<T> {
    if (this is UiState.Error) {
        action(exception)
    }
    return this
}

inline fun <T> UiState<T>.onLoading(action: () -> Unit): UiState<T> {
    if (this is UiState.Loading) {
        action()
    }
    return this
}

inline fun <T> UiState<T>.onEmpty(action: (String) -> Unit): UiState<T> {
    if (this is UiState.Empty) {
        action(message)
    }
    return this
}

/**
 * Converts Resource to UiState
 */
fun <T> Resource<T>.toUiState(): UiState<T> {
    return when (this) {
        is Resource.Loading -> UiState.Loading
        is Resource.Success -> {
            if (data != null) {
                UiState.Success(data)
            } else {
                UiState.Empty("No data available")
            }
        }
        is Resource.Error -> UiState.Error(message ?: "Unknown error occurred")
    }
}

/**
 * Converts UiState to Resource
 */
fun <T> UiState<T>.toResource(): Resource<T> {
    return when (this) {
        is UiState.Loading -> Resource.Loading()
        is UiState.Success -> Resource.Success(data)
        is UiState.Error -> Resource.Error(exception)
        is UiState.Empty -> Resource.Error(message)
        is UiState.Idle -> Resource.Loading()
    }
}
