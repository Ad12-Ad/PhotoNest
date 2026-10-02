package com.example.photonest.feature.addpost

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.Post
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.domain.usecase.CreatePostUseCase
import com.example.photonest.feature.addpost.model.AddPostEvent
import com.example.photonest.feature.addpost.model.AddPostState
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
    private val userRepository: IUserRepository,
    private val createPostUseCase: CreatePostUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddPostState())
    val uiState: StateFlow<AddPostState> = _uiState
    private val _customCategories = mutableSetOf<String>()

    companion object {
        const val MAX_CATEGORIES = 3
    }

    private val categories = listOf(
        "Nature", "Travel", "Food", "Fashion", "Technology",
        "Art", "Music", "Sports", "Lifestyle", "Education"
    )

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.getCurrentUser().collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is NetworkResult.Success -> {
                            _uiState.update { it.copy(currentUser = result.data) }
                        }
                        is NetworkResult.Error -> {
                            _uiState.update { it.copy(error = result.message, showErrorDialog = true) }
                        }
                        is NetworkResult.Loading -> {}
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
        _uiState.update { it.copy(showErrorDialog = false, error = null) }
    }

    private fun updateImage(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    private fun updateCaption(caption: String) {
        _uiState.update { it.copy(caption = caption) }
    }

    private fun updateLocation(location: String) {
        _uiState.update { it.copy(location = location) }
    }

    private fun toggleCategory(category: String) {
        val currentCategories = _uiState.value.selectedCategories
        val newCategories = if (currentCategories.contains(category)) {
            currentCategories - category
        } else if (currentCategories.size < MAX_CATEGORIES) {
            if (!categories.contains(category)) {
                _customCategories.add(category)
            }
            currentCategories + category
        } else {
            currentCategories
        }

        _uiState.update { it.copy(selectedCategories = newCategories) }
    }

    fun getCustomCategories(): Set<String> = _customCategories.toSet()

    private fun clearCategories() {
        _uiState.update { it.copy(selectedCategories = emptySet()) }
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun createPost() {
        val currentState = _uiState.value
        val currentUser = currentState.currentUser

        if (currentUser == null) {
            _uiState.update { it.copy(error = "User not found. Please sign in again.", showErrorDialog = true) }
            return
        }

        val imageUri = currentState.selectedImageUri
        if (imageUri == null) {
            _uiState.update { it.copy(error = "Please select an image", showErrorDialog = true) }
            return
        }

        if (currentState.caption.isBlank()) {
            _uiState.update { it.copy(error = "Please add a caption", showErrorDialog = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val post = Post(
                id = "",
                userId = currentUser.id,
                userName = currentUser.username,
                userImage = currentUser.profilePicture,
                caption = currentState.caption,
                location = currentState.location,
                category = currentState.selectedCategories.toList(),
                timestamp = System.currentTimeMillis()
            )

            // Delegated to UseCase: Compression, Upload, and File Cleanup happen here safely
            when (val result = createPostUseCase(post, imageUri)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isPostCreated = true, error = null) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to create post", showErrorDialog = true) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun getFilteredCategories(): List<String> {
        return categories.filter {
            it.lowercase().contains(_uiState.value.searchQuery.lowercase())
        }
    }

    fun resetPostCreated() {
        _uiState.update { it.copy(isPostCreated = false, selectedImageUri = null) }
    }
}