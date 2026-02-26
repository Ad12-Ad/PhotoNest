package com.example.photonest.feature.feed.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.Comment
import com.example.photonest.domain.model.Notification
import com.example.photonest.domain.model.Post
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.domain.repository.ICommentRepository
import com.example.photonest.domain.repository.INotificationRepository
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.feature.feed.postdetail.model.PostDetailEffect
import com.example.photonest.feature.feed.postdetail.model.PostDetailEvent
import com.example.photonest.feature.feed.postdetail.model.PostDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val commentRepository: ICommentRepository,
    private val userRepository: IUserRepository,
    private val authRepository: IAuthRepository,
    private val notificationRepository: INotificationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PostDetailState())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<PostDetailEffect>()
    val effect = _effect.asSharedFlow()

    private var postId: String = ""

    fun onEvent(event: PostDetailEvent) {
        when (event) {
            is PostDetailEvent.Load -> loadPost(event.postId)

            PostDetailEvent.ToggleLike -> toggleLike()
            PostDetailEvent.ToggleBookmark -> toggleBookmark()
            PostDetailEvent.ToggleFollow -> toggleFollow()

            PostDetailEvent.OpenLikesSheet -> openLikesSheet()
            PostDetailEvent.CloseLikesSheet ->
                _state.update { it.copy(isLikesSheetVisible = false) }

            is PostDetailEvent.UpdateComment ->
                _state.update { it.copy(newComment = event.value) }

            PostDetailEvent.AddComment -> addComment()
            is PostDetailEvent.DeleteComment -> deleteComment(event.commentId)

            PostDetailEvent.SharePost -> sharePost()
            PostDetailEvent.DismissError -> {}
        }
    }

    private fun loadPost(id: String) = viewModelScope.launch {
        postId = id
        val userId = authRepository.getCurrentUserId() //

        _state.update {
            it.copy(
                isLoading = true,
                currentUserId = userId
            )
        }

        if (userId != null) {
            when (val userResult = userRepository.getUserById(userId)) { //
                is NetworkResult.Success -> {
                    _state.update {
                        it.copy(
                            currentUserName = userResult.data?.username,
                            currentUserImage = userResult.data?.profilePicture
                        )
                    }
                }
                else -> Unit
            }
        }

        val postResult = postRepository.getPostById(id) //

        val commentsResult = commentRepository.getCommentsForPost(id) //

        val fetchedComments = if (commentsResult is NetworkResult.Success) {
            commentsResult.data ?: emptyList() //
        } else {
            emptyList()
        }

        when (postResult) {
            is NetworkResult.Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        postDetail = postResult.data?.copy(
                            comments = fetchedComments
                        )
                    )
                }
            }
            is NetworkResult.Error -> {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(
                    PostDetailEffect.ShowError(
                        postResult.message ?: "Failed to load post" //
                    )
                )
            }
            else -> {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun toggleLike() = viewModelScope.launch {
        val post = _state.value.postDetail?.post ?: return@launch

        val currentUserId = authRepository.getCurrentUserId() ?: return@launch

        // Capture the state before we optimistically change it
        val wasLiked = post.isLiked

        // Optimistic UI
        _state.update {
            it.copy(
                postDetail = it.postDetail!!.copy(
                    post = post.copy(
                        isLiked = !post.isLiked,
                        likeCount =
                            if (post.isLiked) post.likeCount - 1
                            else post.likeCount + 1
                    )
                )
            )
        }

        val result = if (wasLiked) {
            postRepository.unlikePost(post.id)
        } else {
            postRepository.likePost(post.id)
        }

        if (result is NetworkResult.Error) {
            // Revert UI on failure
            rollbackLike(post)
            _effect.emit(
                PostDetailEffect.ShowError(
                    result.message ?: "Failed to update like"
                )
            )
        } else if (!wasLiked && post.userId != currentUserId) {
            // SUCCESS: It was a NEW like, and you aren't liking your own post.

            val notification = Notification(
                id = UUID.randomUUID().toString(),
                userId = post.userId,
                fromUserId = currentUserId,
                fromUsername = _state.value.currentUserName.orEmpty(),
                fromUserImage = _state.value.currentUserImage.orEmpty(),
                type = "LIKE", // Matches your NotificationType enum parsing
                postId = post.id,
                message = "liked your post",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            // Fire and forget on the IO dispatcher so we don't block the main thread
            viewModelScope.launch(Dispatchers.IO) {
                notificationRepository.createNotification(notification)
            }
        }

        if (result is NetworkResult.Error) {
            rollbackLike(post)

            _effect.emit(
                PostDetailEffect.ShowError(
                    result.message ?: "Failed to update like"
                )
            )
        }
    }

    private fun rollbackLike(originalPost: Post) {
        _state.update {
            it.copy(
                postDetail = it.postDetail!!.copy(post = originalPost)
            )
        }
    }

    private fun toggleBookmark() = viewModelScope.launch {
        val post = _state.value.postDetail?.post ?: return@launch

        _state.update {
            it.copy(
                postDetail = it.postDetail!!.copy(
                    post = post.copy(isBookmarked = !post.isBookmarked)
                )
            )
        }

        val result =
            if (post.isBookmarked)
                postRepository.unbookmarkPost(post.id)
            else
                postRepository.bookmarkPost(post.id)

        if (result is NetworkResult.Error) {
            _state.update {
                it.copy(
                    postDetail = it.postDetail!!.copy(post = post)
                )
            }
        }
    }

    private fun toggleFollow() = viewModelScope.launch {
        val post = _state.value.postDetail?.post ?: return@launch

        val currentUserId = authRepository.getCurrentUserId() ?: return@launch

        // Capture the state before optimistic update
        val wasFollowing = post.isUserFollowed

        _state.update {
            it.copy(
                postDetail = it.postDetail!!.copy(
                    post = post.copy(isUserFollowed = !post.isUserFollowed)
                )
            )
        }

        val result =
            if (post.isUserFollowed)
                userRepository.unfollowUser(post.userId)
            else
                userRepository.followUser(post.userId)

        if (result is NetworkResult.Error) {
            // Revert UI on failure
            _state.update {
                it.copy(
                    postDetail = it.postDetail!!.copy(post = post)
                )
            }
        } else if (!wasFollowing && post.userId != currentUserId) {
            // SUCCESS: It was a NEW follow, and you aren't following yourself.

            val notification = Notification(
                id = UUID.randomUUID().toString(),
                userId = post.userId,
                fromUserId = currentUserId,
                fromUsername = _state.value.currentUserName.orEmpty(),
                fromUserImage = _state.value.currentUserImage.orEmpty(),
                type = "FOLLOW",
                postId = post.id, // Optional depending on if clicking a follow notification takes you to the profile or the post
                message = "started following you",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )

            viewModelScope.launch(Dispatchers.IO) {
                notificationRepository.createNotification(notification)
            }
        }

        if (result is NetworkResult.Error) {
            _state.update {
                it.copy(
                    postDetail = it.postDetail!!.copy(post = post)
                )
            }
        }
    }

    private fun openLikesSheet() = viewModelScope.launch {
        val postId = _state.value.postDetail?.post?.id ?: return@launch

        _state.update {
            it.copy(
                isLikesSheetVisible = true,
                isLikesLoading = true
            )
        }

        when (val result = postRepository.getUsersWhoLikedPost(postId)) {
            is NetworkResult.Success -> {
                _state.update {
                    it.copy(
                        likedUsers = result.data ?: emptyList(),
                        isLikesLoading = false
                    )
                }
            }

            is NetworkResult.Error -> {
                _state.update {
                    it.copy(isLikesLoading = false)
                }
                _effect.emit(
                    PostDetailEffect.ShowError(
                        result.message ?: "Failed to load likes"
                    )
                )
            }

            else -> Unit
        }
    }

    private fun addComment() = viewModelScope.launch {
        val text = _state.value.newComment.trim()
        if (text.isEmpty()) return@launch

        val userId = authRepository.getCurrentUserId() ?: return@launch

        val temporaryUiId = UUID.randomUUID().toString()

        val comment = Comment(
            id = temporaryUiId,
            postId = postId,
            userId = userId,
            userName = _state.value.currentUserName.orEmpty(),
            userImage = _state.value.currentUserImage.orEmpty(),
            text = text,
            timestamp = System.currentTimeMillis()
        )

        _state.update { it.copy(isAddingComment = true) }

        when (val result = commentRepository.addComment(comment)) {
            is NetworkResult.Success -> {
                _state.update { state ->
                    state.copy(
                        isAddingComment = false,
                        newComment = "",
                        postDetail = state.postDetail?.let { currentPost ->
                            currentPost.copy(
                                // Prepend the new comment safely
                                comments = listOf(comment) + currentPost.comments
                            )
                        }
                    )
                }
                val postOwnerId = _state.value.postDetail?.post?.userId

                // Prevent notifying yourself
                if (postOwnerId != null && postOwnerId != userId) {
                    val notification = Notification(
                        id = UUID.randomUUID().toString(), // Generate an ID for Firestore
                        userId = postOwnerId,
                        fromUserId = userId,
                        fromUsername = _state.value.currentUserName.orEmpty(),
                        fromUserImage = _state.value.currentUserImage.orEmpty(),
                        type = "COMMENT", // Or your NotificationType.COMMENT.name
                        postId = postId,
                        message = "commented on your post",
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )

                    // Launch in a new coroutine so it doesn't block the UI update
                    viewModelScope.launch(Dispatchers.IO) {
                        notificationRepository.createNotification(notification)
                    }
                }
            }

            is NetworkResult.Error -> {
                _state.update { it.copy(isAddingComment = false) }
                _effect.emit(
                    PostDetailEffect.ShowError(
                        result.message ?: "Failed to add comment"
                    )
                )
            }

            else -> Unit
        }
    }

    private fun deleteComment(commentId: String) =
        viewModelScope.launch {
            commentRepository.deleteComment(commentId)
            _state.update {
                it.copy(
                    postDetail = it.postDetail!!.copy(
                        comments = it.postDetail.comments.filterNot { c ->
                            c.id == commentId
                        }
                    )
                )
            }
        }

    private fun sharePost() = viewModelScope.launch {
        val post = _state.value.postDetail?.post ?: return@launch

        _effect.emit(
            PostDetailEffect.Share(
                "${post.caption}\n\nby @${post.userName}"
            )
        )
    }
}