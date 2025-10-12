package com.example.photonest.core.utils

/**
 * A generic wrapper class around data request
 * to handle loading, success and error states
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}

/**
 * Extension functions for Resource class to handle different states
 */
inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success && data != null) {
        action(data)
    }
    return this
}

inline fun <T> Resource<T>.onError(action: (String) -> Unit): Resource<T> {
    if (this is Resource.Error) {
        action(message ?: "Unknown error occurred")
    }
    return this
}

inline fun <T> Resource<T>.onLoading(action: () -> Unit): Resource<T> {
    if (this is Resource.Loading) {
        action()
    }
    return this
}

/**
 * Maps the data type of Resource from T to R
 */
inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<out R?> {
    return when (this) {
        is Resource.Success -> {
            try {
                Resource.Success(data?.let { transform(it) })
            } catch (e: Exception) {
                Resource.Error("Transformation failed: ${e.message}")
            }
        }
        is Resource.Error -> Resource.Error(message ?: "Unknown error", null)
        is Resource.Loading -> Resource.Loading()
    }
}

/**
 * Safely gets data from Resource
 */
fun <T> Resource<T>.getDataOrNull(): T? = when (this) {
    is Resource.Success -> data
    else -> null
}

/**
 * Gets data or throws exception
 */
fun <T> Resource<T>.getDataOrThrow(): T? = when (this) {
    is Resource.Success -> data
    is Resource.Error -> throw Exception(message ?: "Unknown error")
    is Resource.Loading -> throw Exception("Resource is still loading")
}
