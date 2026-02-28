package com.example.photonest.data.repository

import com.example.photonest.core.preferences.PreferencesManager
import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.core.utils.getDataOrNull
import com.example.photonest.core.utils.getDataOrThrow
import com.example.photonest.core.utils.safeFirebaseCall
import com.example.photonest.data.local.dao.CommentDao
import com.example.photonest.data.local.dao.FollowDao
import com.example.photonest.data.local.dao.NotificationDao
import com.example.photonest.data.local.dao.PostDao
import com.example.photonest.data.local.dao.UserDao
import com.example.photonest.data.mapper.toEntity
import com.example.photonest.domain.model.AuthResult
import com.example.photonest.domain.model.User
import com.example.photonest.domain.repository.IAuthRepository
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val userDao: UserDao,
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val notificationDao: NotificationDao,
    private val followDao: FollowDao,
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
            signUpWithEmailAndPassword(email, password)
        } else {
            signInWithEmailAndPassword(email, password)
        }

        result.getDataOrThrow()!!
    }

    override suspend fun checkUserExists(email: String): NetworkResult<Boolean> = safeFirebaseCall {
        val result = firebaseAuth.fetchSignInMethodsForEmail(email).await()
        !result.signInMethods.isNullOrEmpty()
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
    ): NetworkResult<AuthResult> = safeFirebaseCall {

        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user ?: throw IllegalStateException("Account creation failed")

        val user = User(
            id = firebaseUser.uid,
            email = email,
            joinedDate = System.currentTimeMillis()
        )

        firestore.collection(Constants.USERS_COLLECTION)
            .document(firebaseUser.uid)
            .set(user)
            .await()

        // 3. Update local state
        userDao.insertUser(user.toEntity())
        preferencesManager.setLoggedIn(true)
        preferencesManager.setUserId(user.id)

        // Crucial: This ensures the user is forced into the Onboarding flow next
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
        val uid = currentUser.uid
        val batch = firestore.batch()

        // 1. WIPE PROFILE PICTURE FROM STORAGE
        try {
            firebaseStorage.reference.child(Constants.PROFILE_IMAGES_PATH).child("$uid.jpg").delete().await()
        } catch (e: Exception) { /* Ignore if they don't have a profile picture */ }

        // 2. WIPE ALL POSTS AND POST IMAGES
        val posts = firestore.collection(Constants.POSTS_COLLECTION).whereEqualTo("userId", uid).get().await()
        for (postDoc in posts.documents) {
            val imageUrl = postDoc.getString("imageUrl")
            // Delete the image from storage if it exists and isn't a mock Picsum URL
            if (!imageUrl.isNullOrEmpty() && !imageUrl.contains("picsum.photos")) {
                try { firebaseStorage.getReferenceFromUrl(imageUrl).delete().await() } catch (e: Exception) {}
            }
            // Add post document to deletion batch
            batch.delete(postDoc.reference)
        }

        // 3. WIPE ALL COMMENTS
        val comments = firestore.collection(Constants.COMMENTS_COLLECTION).whereEqualTo("userId", uid).get().await()
        comments.documents.forEach { batch.delete(it.reference) }

        // 4. WIPE ALL LIKES, BOOKMARKS, AND FOLLOWS
        val likes = firestore.collection(Constants.LIKES_COLLECTION).whereEqualTo("userId", uid).get().await()
        likes.documents.forEach { batch.delete(it.reference) }

        val bookmarks = firestore.collection(Constants.BOOKMARKS_COLLECTION).whereEqualTo("userId", uid).get().await()
        bookmarks.documents.forEach { batch.delete(it.reference) }

        val followsAsFollower = firestore.collection(Constants.FOLLOWS_COLLECTION).whereEqualTo("followerId", uid).get().await()
        followsAsFollower.documents.forEach { batch.delete(it.reference) }

        val followsAsFollowing = firestore.collection(Constants.FOLLOWS_COLLECTION).whereEqualTo("followingId", uid).get().await()
        followsAsFollowing.documents.forEach { batch.delete(it.reference) }

        // 5. DELETE USER DOCUMENT
        batch.delete(firestore.collection(Constants.USERS_COLLECTION).document(uid))

        // 6. COMMIT ALL FIRESTORE DELETIONS AT ONCE
        batch.commit().await()

        // 7. DELETE FIREBASE AUTH ACCOUNT
        currentUser.delete().await()

        userDao.clearAllUsers()
        postDao.clearAllPosts()
        commentDao.clearAllComments()
        notificationDao.clearAllNotifications()
        followDao.clearAllFollows()

        // 9. CLEAR SHARED PREFERENCES
        preferencesManager.clearUserData()
    }
}