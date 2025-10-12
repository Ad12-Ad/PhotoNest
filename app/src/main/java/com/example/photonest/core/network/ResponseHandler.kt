package com.example.photonest.core.network

import retrofit2.Response
import java.io.IOException

class ResponseHandler {

    fun <T : Any> handleSuccess(data: T): NetworkResult<T> {
        return NetworkResult.Success(data)
    }

    fun <T : Any> handleException(e: Exception): NetworkResult<T> {
        return when (e) {
            is IOException -> NetworkResult.Error("Network error. Please check your connection.", null)
            else -> NetworkResult.Error("Something went wrong: ${e.message}", null)
        }
    }

    fun <T : Any> handleApi(response: Response<T>): NetworkResult<T> {
        return try {
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    NetworkResult.Success(body)
                } else {
                    NetworkResult.Error("Response body is null", response.code())
                }
            } else {
                NetworkResult.Error("API Error: ${response.message()}", response.code())
            }
        } catch (e: Exception) {
            NetworkResult.Error("Exception: ${e.message}", null)
        }
    }
}
