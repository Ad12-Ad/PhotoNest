package com.example.photonest.data.repository

import com.example.photonest.core.preferences.PreferencesManager
import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.local.dao.UserDao
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.data.model.AuthResult
import com.example.photonest.data.model.User
import com.example.photonest.domain.repository.IAuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val preferencesManager: PreferencesManager
) : IAuthRepository {

    override suspend fun signInWithEmailAndPassword(email: String, password: String): Resource<AuthResult> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Get user data from Firestore
                val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .get()
                    .await()

                val user = userDoc.toObject(User::class.java)?.copy(id = firebaseUser.uid)

                if (user != null) {
                    // Save to local database
                    userDao.insertUser(user.toEntity())

                    // Update preferences
                    preferencesManager.setLoggedIn(true)
                    preferencesManager.setUserId(user.id)

                    Resource.Success(AuthResult(success = true, user = user))
                } else {
                    Resource.Error("User data not found")
                }
            } else {
                Resource.Error("Authentication failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign in failed")
        }
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        name: String,
        username: String
    ): Resource<AuthResult> {
        return try {
            // Check if username is already taken
            val usernameQuery = firestore.collection(Constants.USERS_COLLECTION)
                .whereEqualTo("username", username)
                .get()
                .await()

            if (!usernameQuery.isEmpty) {
                return Resource.Error("Username is already taken")
            }

            // Create Firebase auth account
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser != null) {
                // Update Firebase auth profile
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()

                // Create user document in Firestore
                val user = User(
                    id = firebaseUser.uid,
                    email = email,
                    name = name,
                    username = username,
                    joinedDate = System.currentTimeMillis()
                )

                firestore.collection(Constants.USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .set(user)
                    .await()

                // Save to local database
                userDao.insertUser(user.toEntity())

                // Update preferences
                preferencesManager.setLoggedIn(true)
                preferencesManager.setUserId(user.id)

                Resource.Success(AuthResult(success = true, user = user))
            } else {
                Resource.Error("Account creation failed")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign up failed")
        }
    }

    override suspend fun signInWithGoogle(): Resource<AuthResult> {
        return try {
            // This would require Google Sign-In implementation
            // For now, return a placeholder
            Resource.Error("Google Sign-In not implemented yet")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Google sign in failed")
        }
    }

    override suspend fun signOut(): Resource<Unit> {
        return try {
            firebaseAuth.signOut()

            // Clear local data
            userDao.clearAllUsers()
            preferencesManager.clearUserData()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign out failed")
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to send password reset email")
        }
    }

    override suspend fun getCurrentUser(): Resource<User?> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                    .document(currentUser.uid)
                    .get()
                    .await()

                val user = userDoc.toObject(User::class.java)?.copy(id = currentUser.uid)
                Resource.Success(user)
            } else {
                Resource.Success(null)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to get current user")
        }
    }

    override fun isUserLoggedIn(): Flow<Boolean> = preferencesManager.isLoggedIn

    override suspend fun deleteAccount(): Resource<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                // Delete user document from Firestore
                firestore.collection(Constants.USERS_COLLECTION)
                    .document(currentUser.uid)
                    .delete()
                    .await()

                // Delete Firebase auth account
                currentUser.delete().await()

                // Clear local data
                userDao.clearAllUsers()
                preferencesManager.clearUserData()

                Resource.Success(Unit)
            } else {
                Resource.Error("No user to delete")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete account")
        }
    }
}
