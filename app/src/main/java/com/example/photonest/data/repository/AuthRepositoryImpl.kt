package com.example.photonest.data.repository

import com.example.photonest.core.preferences.PreferencesManager
import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.core.utils.getDataOrNull
import com.example.photonest.core.utils.getDataOrThrow
import com.example.photonest.core.utils.safeFirebaseCall
import com.example.photonest.data.local.dao.UserDao
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.domain.model.AuthResult
import com.example.photonest.domain.model.User
import com.example.photonest.domain.repository.IAuthRepository
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao,
    private val preferencesManager: PreferencesManager
) : IAuthRepository {

    private val pendingOtps = mutableMapOf<String, PendingOtpData>()

    data class PendingOtpData(
        val otp: String,
        val email: String,
        val password: String,
        val name: String? = null,
        val username: String? = null,
        val isSignUp: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )

    override suspend fun sendOtpToEmail(email: String): NetworkResult<String> = safeFirebaseCall {
        val otp = Random.nextInt(100000, 999999).toString()
        val verificationId = "${email}_${System.currentTimeMillis()}"

        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://photonest.page.link/verify")
            .setHandleCodeInApp(true)
            .setAndroidPackageName("com.example.photonest", true, null)
            .build()

        android.util.Log.d("OTP_DEBUG", "OTP for $email: $otp")

        pendingOtps[verificationId] = PendingOtpData(
            otp = otp,
            email = email,
            password = "",
            isSignUp = false
        )

        verificationId
    }

    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    override suspend fun getCurrentUserIdOrThrow(): String {
        return firebaseAuth.currentUser?.uid
            ?: throw IllegalStateException("User not authenticated")
    }

    override suspend fun verifyOtp(
        verificationId: String,
        otp: String,
        email: String,
        password: String,
        name: String?,
        username: String?,
        isSignUp: Boolean
    ): NetworkResult<AuthResult> = safeFirebaseCall {
        val pendingData = pendingOtps[verificationId] ?: throw IllegalStateException("OTP expired or invalid")

        val currentTime = System.currentTimeMillis()
        if (currentTime - pendingData.timestamp > 5 * 60 * 1000) {
            pendingOtps.remove(verificationId)
            throw IllegalStateException("OTP expired. Please request a new one")
        }

        if (pendingData.otp != otp) {
            throw IllegalStateException("Invalid OTP. Please try again")
        }

        pendingOtps.remove(verificationId)

        val result = if (isSignUp) {
            signUpWithEmailAndPassword(email, password, name ?: "", username ?: "")
        } else {
            signInWithEmailAndPassword(email, password)
        }

        result.getDataOrThrow()!!
    }

    override suspend fun resendOtp(email: String): NetworkResult<String> {
        pendingOtps.entries.removeIf { it.value.email == email }
        return sendOtpToEmail(email)
    }

    override suspend fun isOnboardingComplete(): Boolean {
        val uid = firebaseAuth.currentUser?.uid ?: return false
        return safeFirebaseCall {
            val doc = firestore.collection(Constants.USERS_COLLECTION)
                .document(uid).get().await()
            doc.toObject(User::class.java)?.onboardingCompleted ?: false
        }.getDataOrNull() ?: false
    }

    override suspend fun updateOnboardingData(
        name: String,
        username: String,
        bio: String,
        profilePictureUrl: String,
        birthday: String,
        location: String
    ): NetworkResult<Unit> = safeFirebaseCall {
        val uid = getCurrentUserIdOrThrow()

        val updates = mapOf(
            "name" to name,
            "username" to username,
            "bio" to bio,
            "profilePicture" to profilePictureUrl,
            "birthday" to birthday,
            "location" to location,
            "onboardingCompleted" to true
        )

        firestore.collection(Constants.USERS_COLLECTION)
            .document(uid)
            .update(updates)
            .await()

        val localUser = userDao.getUserById(uid)
        if (localUser != null) {
            val updatedUser = localUser.copy(
                name = name,
                username = username,
                bio = bio,
                profilePicture = profilePictureUrl,
                birthday = birthday,
                location = location,
                onboardingCompleted = true
            )
            userDao.updateUser(updatedUser)
        }

        preferencesManager.setOnboardingCompleted(true)
    }

    override suspend fun signInWithEmailAndPassword(email: String, password: String): NetworkResult<AuthResult> = safeFirebaseCall {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw IllegalStateException("Authentication failed")

        val userDoc = firestore.collection(Constants.USERS_COLLECTION)
            .document(firebaseUser.uid)
            .get()
            .await()

        val user = userDoc.toObject(User::class.java)?.copy(id = firebaseUser.uid)
            ?: throw IllegalStateException("User data not found")

        userDao.insertUser(user.toEntity())
        preferencesManager.setLoggedIn(true)
        preferencesManager.setUserId(user.id)

        AuthResult(success = true, user = user)
    }

    override suspend fun signUpWithEmailAndPassword(
        email: String,
        password: String,
        name: String,
        username: String
    ): NetworkResult<AuthResult> = safeFirebaseCall {
        val usernameQuery = firestore.collection(Constants.USERS_COLLECTION)
            .whereEqualTo("username", username)
            .get()
            .await()

        if (!usernameQuery.isEmpty) {
            throw IllegalStateException("Username is already taken")
        }

        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw IllegalStateException("Account creation failed")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        firebaseUser.updateProfile(profileUpdates).await()

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

        userDao.insertUser(user.toEntity())
        preferencesManager.setLoggedIn(true)
        preferencesManager.setUserId(user.id)
        preferencesManager.setOnboardingCompleted(false)

        AuthResult(success = true, user = user)
    }

    override suspend fun signOut(): NetworkResult<Unit> = safeFirebaseCall {
        firebaseAuth.signOut()
        userDao.clearAllUsers()
        preferencesManager.clearUserData()
    }

    override suspend fun getCurrentUser(): NetworkResult<User?> = safeFirebaseCall {
        val currentUser = firebaseAuth.currentUser ?: return@safeFirebaseCall null
        val userDoc = firestore.collection(Constants.USERS_COLLECTION)
            .document(currentUser.uid)
            .get()
            .await()
        userDoc.toObject(User::class.java)?.copy(id = currentUser.uid)
    }

    override fun isUserLoggedIn(): Flow<Boolean> = preferencesManager.isLoggedIn

    override suspend fun deleteAccount(): NetworkResult<Unit> = safeFirebaseCall {
        val currentUser = firebaseAuth.currentUser ?: throw IllegalStateException("No user to delete")
        firestore.collection(Constants.USERS_COLLECTION)
            .document(currentUser.uid)
            .delete()
            .await()
        currentUser.delete().await()
        userDao.clearAllUsers()
        preferencesManager.clearUserData()
    }
}