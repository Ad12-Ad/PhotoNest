package com.example.photonest.core.network

import com.google.firebase.auth.FirebaseAuth
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get Firebase Auth token
        val currentUser = FirebaseAuth.getInstance().currentUser
        val token = currentUser?.getIdToken(false)?.result?.token

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .build()
        }

        return chain.proceed(newRequest)
    }
}
