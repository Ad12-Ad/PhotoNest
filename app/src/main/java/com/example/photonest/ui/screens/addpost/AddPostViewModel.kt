package com.example.photonest.ui.screens.addpost

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.ui.screens.addpost.model.AddPostEvent
import com.example.photonest.ui.screens.addpost.model.AddPostState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AddPostViewModel : ViewModel() {
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

    fun handleEvent(event: AddPostEvent) {
        when (event) {
            is AddPostEvent.ImageSelected -> updateImage(event.uri)
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
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
//                TODO: Implement the firebase logic
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun getFilteredCategories(): List<String> {
        return categories.filter {
            it.lowercase().contains(_state.value.searchQuery.lowercase())
        }
    }
}