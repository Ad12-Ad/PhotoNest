package com.example.photonest.domain.repository

import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.AuthResult
import com.example.photonest.data.model.User
import kotlinx.coroutines.flow.Flow

interface IAuthRepository {
    suspend fun signInWithEmailAndPassword(email: String, password: String): Resource<AuthResult>
    suspend fun signUpWithEmailAndPassword(email: String, password: String, name: String, username: String): Resource<AuthResult>
    suspend fun signInWithGoogle(): Resource<AuthResult>
    suspend fun signOut(): Resource<Unit>
    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>
    suspend fun getCurrentUser(): Resource<User?>
    fun isUserLoggedIn(): Flow<Boolean>
    suspend fun deleteAccount(): Resource<Unit>
}
