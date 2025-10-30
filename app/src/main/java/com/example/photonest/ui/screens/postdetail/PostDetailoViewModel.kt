package com.example.photonest.ui.screens.postdetail

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Comment
import com.example.photonest.data.model.PostDetail
import com.example.photonest.domain.repository.ICommentRepository
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val commentRepository: ICommentRepository,
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private var currentPostId: String = ""

    init {
        loadCurrentUserImage()
    }

    private fun loadCurrentUserImage() {
        viewModelScope.launch {
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch

            userRepository.getCurrentUser().collect { result ->
                if (result is Resource.Success && result.data != null) {
                    _uiState.update {
                        it.copy(
                            currentUserImage = result.data.profilePicture,
                            currentUserName = result.data.name
                        )
                    }
                }
            }
        }
    }

    fun loadPostDetail(postId: String) {
        currentPostId = postId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val postResult = postRepository.getPostById(postId)
                when (postResult) {
                    is Resource.Success -> {
                        val postDetail = postResult.data
                        if (postDetail != null) {
                            // Load comments
                            val commentsResult = commentRepository.getCommentsForPost(postId)
                            when (commentsResult) {
                                is Resource.Success -> {
                                    val updatedPostDetail = postDetail.copy(comments = commentsResult.data ?: emptyList())
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            postDetail = updatedPostDetail
                                        )
                                    }
                                }
                                is Resource.Error -> {
                                    _uiState.update {
                                        it.copy(
                                            isLoading = false,
                                            postDetail = postDetail,
                                            error = "Failed to load comments: ${commentsResult.message}"
                                        )
                                    }
                                }
                                else -> Unit
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "Post not found"
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = postResult.message ?: "Failed to load post"
                            )
                        }
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            }
        }
    }

    fun toggleLike() {
        val currentPost = _uiState.value.postDetail?.post ?: return
        viewModelScope.launch {
            _uiState.update { state ->
                state.postDetail?.let { postDetail ->
                    state.copy(
                        postDetail = postDetail.copy(
                            post = postDetail.post.copy(
                                isLiked = !currentPost.isLiked,
                                likeCount = if (currentPost.isLiked)
                                    currentPost.likeCount - 1
                                else
                                    currentPost.likeCount + 1
                            )
                        )
                    )
                } ?: state
            }

            try {
                val result = if (currentPost.isLiked) {
                    postRepository.unlikePost(currentPost.id)
                } else {
                    postRepository.likePost(currentPost.id)
                }

                when (result) {
                    is Resource.Error -> {
                        _uiState.update { state ->
                            state.postDetail?.let { postDetail ->
                                state.copy(
                                    postDetail = postDetail.copy(
                                        post = postDetail.post.copy(
                                            isLiked = currentPost.isLiked,
                                            likeCount = currentPost.likeCount
                                        )
                                    )
                                )
                            } ?: state
                        }
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Failed to update like status",
                                showErrorDialog = true
                            )
                        }
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.postDetail?.let { postDetail ->
                        state.copy(
                            postDetail = postDetail.copy(
                                post = postDetail.post.copy(
                                    isLiked = currentPost.isLiked,
                                    likeCount = currentPost.likeCount
                                )
                            )
                        )
                    } ?: state
                }
            }
        }
    }

    fun toggleBookmark() {
        val currentPost = _uiState.value.postDetail?.post ?: return
        viewModelScope.launch {
            _uiState.update { state ->
                state.postDetail?.let { postDetail ->
                    state.copy(
                        postDetail = postDetail.copy(
                            post = postDetail.post.copy(
                                isBookmarked = !currentPost.isBookmarked
                            )
                        )
                    )
                } ?: state
            }

            try {
                val result = if (currentPost.isBookmarked) {
                    postRepository.unbookmarkPost(currentPost.id)
                } else {
                    postRepository.bookmarkPost(currentPost.id)
                }

                when (result) {
                    is Resource.Error -> {
                        _uiState.update { state ->
                            state.postDetail?.let { postDetail ->
                                state.copy(
                                    postDetail = postDetail.copy(
                                        post = postDetail.post.copy(
                                            isBookmarked = currentPost.isBookmarked
                                        )
                                    )
                                )
                            } ?: state
                        }
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.postDetail?.let { postDetail ->
                        state.copy(
                            postDetail = postDetail.copy(
                                post = postDetail.post.copy(
                                    isBookmarked = currentPost.isBookmarked
                                )
                            )
                        )
                    } ?: state
                }
            }
        }
    }

    fun toggleFollow() {
        val currentPost = _uiState.value.postDetail?.post ?: return
        viewModelScope.launch {
            _uiState.update { state ->
                state.postDetail?.let { postDetail ->
                    state.copy(
                        postDetail = postDetail.copy(
                            post = postDetail.post.copy(
                                isUserFollowed = !currentPost.isUserFollowed
                            )
                        )
                    )
                } ?: state
            }

            try {
                val result = if (currentPost.isUserFollowed) {
                    userRepository.unfollowUser(currentPost.userId)
                } else {
                    userRepository.followUser(currentPost.userId)
                }

                when (result) {
                    is Resource.Error -> {
                        _uiState.update { state ->
                            state.postDetail?.let { postDetail ->
                                state.copy(
                                    postDetail = postDetail.copy(
                                        post = postDetail.post.copy(
                                            isUserFollowed = currentPost.isUserFollowed
                                        )
                                    )
                                )
                            } ?: state
                        }
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.postDetail?.let { postDetail ->
                        state.copy(
                            postDetail = postDetail.copy(
                                post = postDetail.post.copy(
                                    isUserFollowed = currentPost.isUserFollowed
                                )
                            )
                        )
                    } ?: state
                }
            }
        }
    }

    fun sharePost(context: Context) {
        val post = _uiState.value.postDetail?.post
        val shareText = buildString {
            if (post != null){
                appendLine("Check out this post on PhotoNest!")
                appendLine()
                appendLine(post.caption)
                if (post.location.isNotEmpty()) {
                    appendLine("📍 ${post.location}")
                }
                appendLine()
                appendLine("by @${post.userName}")
            }
        }

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share Post"))
    }

    fun updateComment(comment: String) {
        _uiState.update { it.copy(newComment = comment) }
    }

    fun addComment() {
        val comment = _uiState.value.newComment.trim()
        if (comment.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingComment = true) }
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    _uiState.update {
                        it.copy(
                            isAddingComment = false,
                            error = "You must be logged in to comment",
                            showErrorDialog = true
                        )
                    }
                    return@launch
                }

                val userName = _uiState.value.currentUserName ?: "Anonymous"
                val userImage = _uiState.value.currentUserImage ?: ""

                val newComment = Comment(
                    id = "", // Will be set by repository
                    postId = currentPostId,
                    userId = currentUser.uid,
                    userName = userName,
                    userImage = userImage,
                    text = comment,
                    timestamp = System.currentTimeMillis()
                )

                val result = commentRepository.addComment(newComment)

                when (result) {
                    is Resource.Success -> {
                        _uiState.update { state ->
                            val currentPostDetail = state.postDetail
                            if (currentPostDetail != null) {
                                val updatedComments = listOf(newComment) + currentPostDetail.comments
                                val updatedPost = currentPostDetail.post.copy(
                                    commentCount = currentPostDetail.post.commentCount + 1
                                )
                                state.copy(
                                    isAddingComment = false,
                                    newComment = "",
                                    postDetail = currentPostDetail.copy(
                                        comments = updatedComments,
                                        post = updatedPost
                                    )
                                )
                            } else {
                                state.copy(
                                    isAddingComment = false,
                                    newComment = ""
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isAddingComment = false,
                                error = result.message ?: "Failed to add comment",
                                showErrorDialog = true
                            )
                        }
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAddingComment = false,
                        error = e.message ?: "An error occurred",
                        showErrorDialog = true
                    )
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update {
            it.copy(error = null, showErrorDialog = false)
        }
    }
}

data class PostDetailUiState(
    val isLoading: Boolean = false,
    val postDetail: PostDetail? = null,
    val error: String? = null,
    val showErrorDialog: Boolean = false,
    val newComment: String = "",
    val isAddingComment: Boolean = false,
    val currentUserImage: String? = null,
    val currentUserName: String? = null
)
