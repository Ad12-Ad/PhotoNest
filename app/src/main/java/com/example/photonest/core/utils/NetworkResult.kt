package com.example.photonest.core.utils

/**
 * A generic wrapper class around data request
 * to handle loading, success and error states
 */
sealed class NetworkResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : NetworkResult<T>(data)
    class Error<T>(message: String, data: T? = null) : NetworkResult<T>(data, message)
    class Loading<T>(data: T? = null) : NetworkResult<T>(data)
}

/**
 * Extension functions for Resource class to handle different states
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success && data != null) {
        action(data)
    }
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (String) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(message ?: "Unknown error occurred")
    }
    return this
}

inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) {
        action()
    }
    return this
}

/**
 * Maps the data type of Resource from T to R
 */
inline fun <T, R> NetworkResult<T>.map(transform: (T) -> R): NetworkResult<out R?> {
    return when (this) {
        is NetworkResult.Success -> {
            try {
                NetworkResult.Success(data?.let { transform(it) })
            } catch (e: Exception) {
                NetworkResult.Error("Transformation failed: ${e.message}")
            }
        }
        is NetworkResult.Error -> NetworkResult.Error(message ?: "Unknown error", null)
        is NetworkResult.Loading -> NetworkResult.Loading()
    }
}

/**
 * Safely gets data from Resource
 */
fun <T> NetworkResult<T>.getDataOrNull(): T? = when (this) {
    is NetworkResult.Success -> data
    else -> null
}

/**
 * Gets data or throws exception
 */
fun <T> NetworkResult<T>.getDataOrThrow(): T? = when (this) {
    is NetworkResult.Success -> data
    is NetworkResult.Error -> throw Exception(message ?: "Unknown error")
    is NetworkResult.Loading -> throw Exception("Resource is still loading")
}
