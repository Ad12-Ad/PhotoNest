package com.example.photonest.domain.repository

import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.model.AuthResult
import com.example.photonest.data.model.User
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    suspend fun signInWithEmailAndPassword(email: String, password: String): NetworkResult<AuthResult>
    suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String, username: String): NetworkResult<AuthResult>
    suspend fun signOut(): NetworkResult<Unit>
    suspend fun getCurrentUser(): NetworkResult<User?>
    fun isUserLoggedIn(): Flow<Boolean>
    fun getCurrentUserId(): String?
    suspend fun getCurrentUserIdOrThrow(): String
    suspend fun deleteAccount(): NetworkResult<Unit>
    suspend fun sendOtpToEmail(email: String): NetworkResult<String> // Returns verification ID
    suspend fun verifyOtp(verificationId: String, otp: String, email: String, password: String, name: String? = null, username: String? = null, isSignUp: Boolean): NetworkResult<AuthResult>
    suspend fun resendOtp(email: String): NetworkResult<String>
    suspend fun isOnboardingComplete(): Boolean
    suspend fun updateOnboardingData(
        name: String,
        username: String,
        bio: String,
        profilePictureUrl: String,
        birthday: String,
        location: String,
    ): NetworkResult<Unit>
}
