package com.example.photonest.feature.feed.postdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.data.model.Comment
import com.example.photonest.data.model.Post
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.domain.repository.ICommentRepository
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
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
    private val authRepository: IAuthRepository
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
        _state.update {
            it.copy(
                isLoading = true,
                currentUserId = authRepository.getCurrentUserId()
            )
        }

        when (val result = postRepository.getPostById(id)) {
            is NetworkResult.Success -> {
                _state.update {
                    it.copy(
                        isLoading = false,
                        postDetail = result.data
                    )
                }
            }

            is NetworkResult.Error -> {
                _effect.emit(
                    PostDetailEffect.ShowError(
                        result.message ?: "Failed to load post"
                    )
                )
            }

            else -> Unit
        }
    }

    private fun toggleLike() = viewModelScope.launch {
        val post = _state.value.postDetail?.post ?: return@launch

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

        val result =
            if (post.isLiked) postRepository.unlikePost(post.id)
            else postRepository.likePost(post.id)

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

        val comment = Comment(
            id = "",
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
                _state.update {
                    it.copy(
                        isAddingComment = false,
                        newComment = "",
                        postDetail = it.postDetail!!.copy(
                            comments = listOf(comment) + it.postDetail.comments
                        )
                    )
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