package com.example.photonest.ui.screens.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.model.User
import com.example.photonest.domain.repository.IUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            userRepository.getCurrentUser().collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val user = result.data
                        if (user != null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    currentUser = user,
                                    name = user.name,
                                    username = user.username,
                                    bio = user.bio,
                                    website = user.website,
                                    location = user.location
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Failed to load user",
                                showErrorDialog = true
                            )
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = validateName(name),
                isInputValid = validateInput(name = name)
            )
        }
    }

    fun updateUsername(username: String) {
        _uiState.update {
            it.copy(
                username = username,
                usernameError = validateUsername(username),
                isInputValid = validateInput(username = username)
            )
        }
    }

    fun updateBio(bio: String) {
        _uiState.update {
            it.copy(
                bio = bio,
                bioError = validateBio(bio),
                isInputValid = validateInput(bio = bio)
            )
        }
    }

    fun updateWebsite(website: String) {
        _uiState.update {
            it.copy(
                website = website,
                websiteError = validateWebsite(website),
                isInputValid = validateInput(website = website)
            )
        }
    }

    fun updateLocation(location: String) {
        _uiState.update {
            it.copy(
                location = location,
                locationError = validateLocation(location),
                isInputValid = validateInput(location = location)
            )
        }
    }

    fun updateProfilePicture(uri: Uri) {
        _uiState.update { it.copy(profilePictureUri = uri) }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        val currentUser = currentState.currentUser ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // Upload profile picture if changed
                val profilePictureUrl = if (currentState.profilePictureUri != null) {
                    when (val result = userRepository.uploadProfilePicture(currentState.profilePictureUri.toString())) {
                        is Resource.Success -> result.data ?: currentUser.profilePicture
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.message ?: "Failed to upload profile picture",
                                    showErrorDialog = true
                                )
                            }
                            return@launch
                        }
                        else -> currentUser.profilePicture
                    }
                } else {
                    currentUser.profilePicture
                }

                // Update user profile
                val updatedUser = currentUser.copy(
                    name = currentState.name,
                    username = currentState.username,
                    bio = currentState.bio,
                    website = currentState.website,
                    location = currentState.location,
                    profilePicture = profilePictureUrl
                )

                when (val result = userRepository.updateUser(updatedUser)) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isUpdateSuccessful = true
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Failed to update profile",
                                showErrorDialog = true
                            )
                        }
                    }
                    else -> Unit
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred",
                        showErrorDialog = true
                    )
                }
            }
        }
    }

    private fun validateName(name: String = _uiState.value.name): String? {
        return when {
            name.isBlank() -> "Name cannot be empty"
            name.length < Constants.MIN_NAME_LENGTH -> "Name must be at least ${Constants.MIN_NAME_LENGTH} characters"
            name.length > Constants.MAX_NAME_LENGTH -> "Name cannot exceed ${Constants.MAX_NAME_LENGTH} characters"
            else -> null
        }
    }

    private fun validateUsername(username: String = _uiState.value.username): String? {
        return when {
            username.isBlank() -> "Username cannot be empty"
            username.length < Constants.MIN_USERNAME_LENGTH -> "Username must be at least ${Constants.MIN_USERNAME_LENGTH} characters"
            username.length > Constants.MAX_USERNAME_LENGTH -> "Username cannot exceed ${Constants.MAX_USERNAME_LENGTH} characters"
            !username.matches(Regex(Constants.RegexPatterns.USERNAME)) -> "Username can only contain letters, numbers, dots and underscores"
            else -> null
        }
    }

    private fun validateBio(bio: String = _uiState.value.bio): String? {
        return when {
            bio.length > Constants.MAX_BIO_LENGTH -> "Bio cannot exceed ${Constants.MAX_BIO_LENGTH} characters"
            else -> null
        }
    }

    private fun validateWebsite(website: String = _uiState.value.website): String? {
        return when {
            website.isNotEmpty() && website.length > Constants.MAX_WEBSITE_LENGTH -> "Website URL too long"
            website.isNotEmpty() && !website.matches(Regex(Constants.RegexPatterns.URL)) -> "Invalid website URL"
            else -> null
        }
    }

    private fun validateLocation(location: String = _uiState.value.location): String? {
        return when {
            location.length > Constants.MAX_LOCATION_LENGTH -> "Location cannot exceed ${Constants.MAX_LOCATION_LENGTH} characters"
            else -> null
        }
    }

    private fun validateInput(
        name: String = _uiState.value.name,
        username: String = _uiState.value.username,
        bio: String = _uiState.value.bio,
        website: String = _uiState.value.website,
        location: String = _uiState.value.location
    ): Boolean {
        return validateName(name) == null &&
                validateUsername(username) == null &&
                validateBio(bio) == null &&
                validateWebsite(website) == null &&
                validateLocation(location) == null
    }

    fun setEditing(isEditing: Boolean) {
        _uiState.update { it.copy(isEditing = isEditing) }
    }


    fun dismissError() {
        _uiState.update { it.copy(error = null, showErrorDialog = false) }
    }
}

data class EditProfileUiState(
    var isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val name: String = "",
    val username: String = "",
    val bio: String = "",
    val website: String = "",
    val location: String = "",
    val profilePictureUri: Uri? = null,
    val nameError: String? = null,
    val usernameError: String? = null,
    val bioError: String? = null,
    val websiteError: String? = null,
    val locationError: String? = null,
    val isInputValid: Boolean = false,
    val isUpdateSuccessful: Boolean = false,
    val error: String? = null,
    val showErrorDialog: Boolean = false
)
