package com.example.photonest.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.photonest.core.utils.ImageCompressionUtils
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.Post
import com.example.photonest.domain.repository.IPostRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val postRepository: IPostRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(post: Post, imageUri: Uri): NetworkResult<Unit> = withContext(Dispatchers.IO) {
        var compressedFile: File? = null
        try {
            compressedFile = ImageCompressionUtils.compressImage(
                context = context,
                uri = imageUri,
                maxLongEdge = 1800,
                quality = 80
            )

            val compressedUri = Uri.fromFile(compressedFile)

            // Execute the repository call
            postRepository.createPost(post, compressedUri.toString())

        } catch (e: Exception) {
            NetworkResult.Error(e.message ?: "An error occurred while creating the post")
        } finally {
            // GUARANTEED CLEANUP: Even if the coroutine is cancelled because
            // the user navigated away, this file will be deleted.
            compressedFile?.delete()
        }
    }
}