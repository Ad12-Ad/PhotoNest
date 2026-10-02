package com.example.photonest.core.utils

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.delay
import retrofit2.Response
import java.io.IOException

/**
 * Exponential backoff retry mechanism for network calls.
 */
suspend fun <T> retryCall(
    times: Int = 3,
    initialDelay: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong()
        }
    }
    return block() // Final attempt
}

/**
 * Safe API Call wrapper specifically for Retrofit Responses.
 */
suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(body)
            } else {
                NetworkResult.Error("Empty response body")
            }
        } else {
            NetworkResult.Error("HTTP Error: ${response.code()} - ${response.message()}")
        }
    } catch (e: IOException) {
        NetworkResult.Error("No internet connection. Please check your network.")
    } catch (e: Exception) {
        NetworkResult.Error(e.message ?: "Unknown API error occurred")
    }
}

/**
 * Safe Firebase Call wrapper for catching and mapping specific Firebase Exceptions.
 */
suspend fun <T> safeFirebaseCall(action: suspend () -> T): NetworkResult<T> {
    return try {
        NetworkResult.Success(action())
    } catch (e: FirebaseFirestoreException) {
        val message = when (e.code) {
            FirebaseFirestoreException.Code.UNAVAILABLE -> "Network unavailable. Operating offline."
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> "Permission denied."
            FirebaseFirestoreException.Code.NOT_FOUND -> "Requested document not found."
            else -> e.message ?: "Database error occurred"
        }
        NetworkResult.Error(message)
    } catch (e: FirebaseAuthException) {
        val message = when (e.errorCode) {
            "ERROR_INVALID_CREDENTIAL" -> "Invalid email or password."
            "ERROR_USER_NOT_FOUND" -> "No account found with this email."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "This email is already registered."
            else -> e.message ?: "Authentication error"
        }
        NetworkResult.Error(message)
    } catch (e: IOException) {
        NetworkResult.Error("Network error. Please check your connection.")
    } catch (e: Exception) {
        NetworkResult.Error(e.message ?: "An unexpected error occurred")
    }
}