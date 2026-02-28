package com.example.photonest.feature.feed.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.Post
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.domain.repository.ICommentRepository
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.domain.usecase.AddCommentUseCase
import com.example.photonest.domain.usecase.ToggleBookmarkUseCase
import com.example.photonest.domain.usecase.ToggleFollowUseCase
import com.example.photonest.domain.usecase.ToggleLikeUseCase
import com.example.photonest.feature.feed.postdetail.model.PostDetailEffect
import com.example.photonest.feature.feed.postdetail.model.PostDetailEvent
import com.example.photonest.feature.feed.postdetail.model.PostDetailState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val commentRepository: ICommentRepository,
    private val userRepository: IUserRepository,
    private val authRepository: IAuthRepository,
    private val toggleLikeUseCase: ToggleLikeUseCase,
    private val toggleBookmarkUseCase: ToggleBookmarkUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val toggleFollowUseCase: ToggleFollowUseCase
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
            PostDetailEvent.CloseLikesSheet -> _state.update { it.copy(isLikesSheetVisible = false) }
            is PostDetailEvent.UpdateComment -> _state.update { it.copy(newComment = event.value) }
            PostDetailEvent.AddComment -> addComment()
            is PostDetailEvent.ToggleCommentLike -> toggleCommentLike(event.commentId)
            PostDetailEvent.DeletePost -> deletePost()
            is PostDetailEvent.DeleteComment -> deleteComment(event.commentId)
            PostDetailEvent.SharePost -> sharePost()
            PostDetailEvent.DismissError -> {}
        }
    }

    private fun loadPost(id: String) = viewModelScope.launch {
        postId = id
        val userId = authRepository.getCurrentUserId()

        _state.update { it.copy(isLoading = true, currentUserId = userId) }

        if (userId != null) {
            when (val userResult = userRepository.getUserById(userId)) {
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

        val postResult = postRepository.getPostById(id)
        val commentsResult = commentRepository.getCommentsForPost(id)

        val fetchedComments = if (commentsResult is NetworkResult.Success) {
            val data = commentsResult.data ?: emptyList()
            data
        } else if (commentsResult is NetworkResult.Error) {
            emptyList()
        } else {
            emptyList()
        }

        when (postResult) {
            is NetworkResult.Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        postDetail = postResult.data?.copy(comments = fetchedComments)
                    )
                }
            }
            is NetworkResult.Error -> {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(PostDetailEffect.ShowError(postResult.message ?: "Failed to load post"))
            }
            else -> _state.update { it.copy(isLoading = false) }
        }
    }

    private fun toggleCommentLike(commentId: String) = viewModelScope.launch {
        val currentPostDetail = _state.value.postDetail ?: return@launch
        val comment = currentPostDetail.comments.find { it.id == commentId } ?: return@launch

        val wasLiked = comment.isLiked

        // 1. Optimistic UI Update: Instantly toggle the like state and count
        _state.update { state ->
            val updatedComments = state.postDetail!!.comments.map { c ->
                if (c.id == commentId) {
                    c.copy(
                        isLiked = !wasLiked,
                        likeCount = if (wasLiked) c.likeCount - 1 else c.likeCount + 1
                    )
                } else c
            }
            state.copy(postDetail = state.postDetail.copy(comments = updatedComments))
        }

        // 2. Delegate to Repository
        val result = if (wasLiked) {
            commentRepository.unlikeComment(commentId)
        } else {
            commentRepository.likeComment(commentId)
        }

        // 3. Rollback if the network call fails
        if (result is NetworkResult.Error) {
            _state.update { state ->
                val rolledBackComments = state.postDetail!!.comments.map { c ->
                    if (c.id == commentId) comment else c // Revert to original comment state
                }
                state.copy(postDetail = state.postDetail.copy(comments = rolledBackComments))
            }
            _effect.emit(PostDetailEffect.ShowError(result.message ?: "Failed to update like"))
        }
    }

    private fun deletePost() = viewModelScope.launch {
        _state.update { it.copy(isLoading = true) }

        when (val result = postRepository.deletePost(postId)) {
            is NetworkResult.Success -> {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(PostDetailEffect.NavigateBack)
            }

            is NetworkResult.Error -> {
                _state.update { it.copy(isLoading = false) }
                _effect.emit(PostDetailEffect.ShowError(result.message ?: "Failed to delete post"))
            }

            else -> Unit
        }
    }
    private fun toggleLike() = viewModelScope.launch {
        val post = _state.value.postDetail?.post ?: return@launch

        _state.update {
            it.copy(
                postDetail = it.postDetail!!.copy(
                    post = post.copy(
                        isLiked = !post.isLiked,
                        likeCount = if (post.isLiked) post.likeCount - 1 else post.likeCount + 1
                    )
                )
            )
        }

        // Delegate to UseCase
        val result = toggleLikeUseCase(post)

        if (result is NetworkResult.Error) {
            // Revert UI on failure
            _state.update { it.copy(postDetail = it.postDetail!!.copy(post = post)) }
            _effect.emit(PostDetailEffect.ShowError(result.message ?: "Failed to update like"))
        }
    }

    private fun toggleBookmark() = viewModelScope.launch {
        val post = _state.value.postDetail?.post ?: return@launch

        // Optimistic UI Update
        _state.update {
            it.copy(
                postDetail = it.postDetail!!.copy(
                    post = post.copy(isBookmarked = !post.isBookmarked)
                )
            )
        }

        // Delegate to UseCase
        val result = toggleBookmarkUseCase(post.id, post.isBookmarked)

        if (result is NetworkResult.Error) {
            _state.update { it.copy(postDetail = it.postDetail!!.copy(post = post)) }
        }
    }

    private fun toggleFollow() = viewModelScope.launch {
        val post = _state.value.postDetail?.post ?: return@launch
        val wasFollowing = post.isUserFollowed

        // Optimistic UI Update
        _state.update {
            it.copy(
                postDetail = it.postDetail!!.copy(
                    post = post.copy(isUserFollowed = !wasFollowing)
                )
            )
        }

        // Delegate to UseCase
        val result = toggleFollowUseCase(targetUserId = post.userId, isCurrentlyFollowing = wasFollowing)

        if (result is NetworkResult.Error) {
            _state.update { it.copy(postDetail = it.postDetail!!.copy(post = post)) }
        }
    }

    private fun addComment() = viewModelScope.launch {
        val text = _state.value.newComment.trim()
        val postOwnerId = _state.value.postDetail?.post?.userId ?: return@launch

        if (text.isEmpty()) return@launch

        _state.update { it.copy(isAddingComment = true) }

        // Delegate to UseCase
        when (val result = addCommentUseCase(postId, postOwnerId, text)) {
            is NetworkResult.Success -> {
                val newComment = result.data ?: return@launch

                _state.update { state ->
                    state.copy(
                        isAddingComment = false,
                        newComment = "",
                        postDetail = state.postDetail?.let { currentPost ->
                            currentPost.copy(
                                comments = listOf(newComment) + currentPost.comments
                            )
                        }
                    )
                }
            }
            is NetworkResult.Error -> {
                _state.update { it.copy(isAddingComment = false) }
                _effect.emit(PostDetailEffect.ShowError(result.message ?: "Failed to add comment"))
            }
            else -> Unit
        }
    }

    private fun openLikesSheet() = viewModelScope.launch {
        val postId = _state.value.postDetail?.post?.id ?: return@launch

        _state.update { it.copy(isLikesSheetVisible = true, isLikesLoading = true) }

        when (val result = postRepository.getUsersWhoLikedPost(postId)) {
            is NetworkResult.Success -> {
                _state.update {
                    it.copy(likedUsers = result.data ?: emptyList(), isLikesLoading = false)
                }
            }
            is NetworkResult.Error -> {
                _state.update { it.copy(isLikesLoading = false) }
                _effect.emit(PostDetailEffect.ShowError(result.message ?: "Failed to load likes"))
            }
            else -> Unit
        }
    }

    private fun deleteComment(commentId: String) = viewModelScope.launch {
        commentRepository.deleteComment(commentId)
        _state.update {
            it.copy(
                postDetail = it.postDetail!!.copy(
                    comments = it.postDetail.comments.filterNot { c -> c.id == commentId }
                )
            )
        }
    }

    private fun sharePost() = viewModelScope.launch {
        val post = _state.value.postDetail?.post ?: return@launch
        _effect.emit(PostDetailEffect.Share("${post.caption}\n\nby @${post.userName}"))
    }
}