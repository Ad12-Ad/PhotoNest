package com.example.photonest.ui.screens.addpost

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.Post
import com.example.photonest.domain.repository.IPostRepository
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.ui.screens.addpost.model.AddPostEvent
import com.example.photonest.ui.screens.addpost.model.AddPostState
import com.google.android.gms.common.util.CollectionUtils.listOf
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Collections.emptySet
import javax.inject.Inject

@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(AddPostState())
    val state: StateFlow<AddPostState> = _state

    companion object {
        const val MAX_CATEGORIES = 3
    }

    // TODO: Fetching the categories from firestore
    private val categories = listOf(
        "Nature", "Travel", "Food", "Fashion", "Technology",
        "Art", "Music", "Sports", "Lifestyle", "Education"
    )

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser()
                .collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(currentUser = result.data)
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = result.message,
                                showErrorDialog = true
                            )
                        }
                        is Resource.Loading -> {
                            // Handle loading if needed
                        }
                    }
                }
        }
    }

    fun handleEvent(event: AddPostEvent) {
        when (event) {
            is AddPostEvent.ImageSelected -> updateImage(event.uri)
            is AddPostEvent.CaptionChanged -> updateCaption(event.caption)
            is AddPostEvent.LocationChanged -> updateLocation(event.location)
            is AddPostEvent.CategoryToggled -> toggleCategory(event.category)
            is AddPostEvent.SearchQueryChanged -> updateSearchQuery(event.query)
            is AddPostEvent.ClearCategories -> clearCategories()
            AddPostEvent.PostClicked -> createPost()
            AddPostEvent.DismissErrorDialog -> dismissErrorDialog()
        }
    }

    private fun dismissErrorDialog() {
        _state.value = _state.value.copy(
            showErrorDialog = false,
            error = null
        )
    }

    private fun updateImage(uri: Uri?) {
        _state.value = _state.value.copy(selectedImageUri = uri)
    }

    private fun updateCaption(caption: String) {
        _state.value = _state.value.copy(caption = caption)
    }

    private fun updateLocation(location: String) {
        _state.value = _state.value.copy(location = location)
    }

    private fun toggleCategory(category: String) {
        val currentCategories = _state.value.selectedCategories
        val newCategories = if (currentCategories.contains(category)) {
            currentCategories - category
        } else if (currentCategories.size < MAX_CATEGORIES) {
            currentCategories + category
        } else {
            currentCategories
        }
        _state.value = _state.value.copy(selectedCategories = newCategories)
    }

    private fun clearCategories() {
        _state.value = _state.value.copy(selectedCategories = emptySet())
    }

    private fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }

    private fun createPost() {
        val currentState = _state.value
        val currentUser = currentState.currentUser

        if (currentUser == null) {
            _state.value = _state.value.copy(
                error = "User not found. Please sign in again.",
                showErrorDialog = true
            )
            return
        }

        if (currentState.selectedImageUri == null) {
            _state.value = _state.value.copy(
                error = "Please select an image",
                showErrorDialog = true
            )
            return
        }

        if (currentState.caption.isBlank()) {
            _state.value = _state.value.copy(
                error = "Please add a caption",
                showErrorDialog = true
            )
            return
        }

        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)

            try {
                val post = Post(
                    id = "", // Will be generated by repository
                    userId = currentUser.id,
                    userName = currentUser.name,
                    userImage = currentUser.profilePicture,
                    caption = currentState.caption,
                    location = currentState.location,
                    category = currentState.selectedCategories.toList(),
                    timestamp = System.currentTimeMillis()
                )

                val result = postRepository.createPost(
                    post = post,
                    imageUri = currentState.selectedImageUri.toString()
                )

                when (result) {
                    is Resource.Success -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            isPostCreated = true,
                            error = null
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message ?: "Failed to create post",
                            showErrorDialog = true
                        )
                    }
                    is Resource.Loading -> {
                        // Already handled above
                    }
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "An error occurred while creating post",
                    showErrorDialog = true
                )
            }
        }
    }

    fun getFilteredCategories(): List<String> {
        return categories.filter {
            it.lowercase().contains(_state.value.searchQuery.lowercase())
        }
    }

    fun resetPostCreated() {
        _state.value = _state.value.copy(isPostCreated = false)
    }
}