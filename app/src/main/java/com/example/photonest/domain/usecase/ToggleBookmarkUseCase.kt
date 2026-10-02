package com.example.photonest.domain.usecase

import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.repository.IPostRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val postRepository: IPostRepository
) {
    suspend operator fun invoke(postId: String, isCurrentlyBookmarked: Boolean): NetworkResult<Unit> {
        return if (isCurrentlyBookmarked) {
            postRepository.unbookmarkPost(postId)
        } else {
            postRepository.bookmarkPost(postId)
        }
    }
}