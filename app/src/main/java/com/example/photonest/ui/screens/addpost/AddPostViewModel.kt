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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddPostViewModel @Inject constructor(
    private val postRepository: IPostRepository,
    private val userRepository: IUserRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddPostState())
    val uiState: StateFlow<AddPostState> = _uiState
    private val _customCategories = mutableSetOf<String>()

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
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getCurrentUser()
                .collect { result ->
                    withContext(Dispatchers.Main) {
                        when (result) {
                            is Resource.Success -> {
                                _uiState.value = _uiState.value.copy(currentUser = result.data)
                            }
                            is Resource.Error -> {
                                _uiState.value = _uiState.value.copy(
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
        _uiState.value = _uiState.value.copy(
            showErrorDialog = false,
            error = null
        )
    }

    private fun updateImage(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    private fun updateCaption(caption: String) {
        _uiState.value = _uiState.value.copy(caption = caption)
    }

    private fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
    }


    private fun toggleCategory(category: String) {
        val currentCategories = _uiState.value.selectedCategories
        val newCategories = if (currentCategories.contains(category)) {
            currentCategories - category
        } else if (currentCategories.size < MAX_CATEGORIES) {
            // Track if it's a custom category (not in predefined list)
            if (!categories.contains(category)) {
                _customCategories.add(category)
            }
            currentCategories + category
        } else {
            currentCategories
        }

        _uiState.value = _uiState.value.copy(selectedCategories = newCategories)
    }

    fun getCustomCategories(): Set<String> = _customCategories.toSet()


    private fun clearCategories() {
        _uiState.value = _uiState.value.copy(selectedCategories = emptySet())
    }

    private fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    private fun createPost() {
        val currentState = _uiState.value
        val currentUser = currentState.currentUser

        if (currentUser == null) {
            _uiState.value = _uiState.value.copy(
                error = "User not found. Please sign in again.",
                showErrorDialog = true
            )
            return
        }

        if (currentState.selectedImageUri == null) {
            _uiState.value = _uiState.value.copy(
                error = "Please select an image",
                showErrorDialog = true
            )
            return
        }

        if (currentState.caption.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Please add a caption",
                showErrorDialog = true
            )
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            }

            try {
                val post = Post(
                    id = "", // Will be generated by repository
                    userId = currentUser.id,
                    userName = currentUser.username,
                    userImage = currentUser.profilePicture,
                    caption = currentState.caption,
                    location = currentState.location,
                    category = currentState.selectedCategories.toList(),
                    timestamp = System.currentTimeMillis()
                )

                val imageUriString = currentState.selectedImageUri?.toString() ?: ""
                val result = postRepository.createPost(
                    post = post,
                    imageUri = imageUriString
                )

                withContext(Dispatchers.Main) {
                    when (result) {
                        is Resource.Success -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isPostCreated = true,
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = result.message ?: "Failed to create post",
                                showErrorDialog = true
                            )
                        }
                        is Resource.Loading -> {
                            // Already handled above
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred while creating post",
                        showErrorDialog = true
                    )
                }
            }
        }
    }

    fun getFilteredCategories(): List<String> {
        return categories.filter {
            it.lowercase().contains(_uiState.value.searchQuery.lowercase())
        }
    }

    fun resetPostCreated() {
        _uiState.value = _uiState.value.copy(isPostCreated = false)
    }
}
