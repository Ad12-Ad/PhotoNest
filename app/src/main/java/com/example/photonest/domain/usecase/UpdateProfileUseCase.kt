package com.example.photonest.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.photonest.core.utils.ImageCompressionUtils
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.User
import com.example.photonest.domain.repository.IUserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val userRepository: IUserRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(user: User, newProfilePictureUri: Uri?): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        var compressedFile: File? = null

        try {
            // 1. Handle Image Compression and Upload (if a new image was selected)
            val finalProfilePictureUrl = if (newProfilePictureUri != null) {
                compressedFile = ImageCompressionUtils.compressImage(
                    context = context,
                    uri = newProfilePictureUri,
                    maxLongEdge = 1800,
                    quality = 80
                )

                val compressedUri = Uri.fromFile(compressedFile)

                when (val uploadResult = userRepository.uploadProfilePicture(compressedUri.toString())) {
                    is NetworkResult.Success -> uploadResult.data ?: user.profilePicture
                    is NetworkResult.Error -> return@withContext NetworkResult.Error(uploadResult.message ?: "Failed to upload picture")
                    else -> user.profilePicture
                }
            } else {
                user.profilePicture // Keep the old one
            }

            // 2. Update the User Object
            val updatedUser = user.copy(profilePicture = finalProfilePictureUrl)

            // 3. Save to Repository
            userRepository.updateUser(updatedUser)

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "An error occurred while updating profile")
        } finally {
            // GUARANTEED CLEANUP: File is wiped from cache no matter what.
            compressedFile?.delete()
        }
    }
}