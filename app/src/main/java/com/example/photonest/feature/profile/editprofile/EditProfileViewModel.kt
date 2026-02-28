package com.example.photonest.feature.profile.editprofile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Constants
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.User
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val updateProfileUseCase: UpdateProfileUseCase // Injected UseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init { loadCurrentUser() }

    private fun loadCurrentUser() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { _uiState.update { it.copy(isLoading = true) } }

            userRepository.getCurrentUser().collect { result ->
                withContext(Dispatchers.Main) {
                    when (result) {
                        is NetworkResult.Success -> {
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
                        is NetworkResult.Error -> {
                            _uiState.update { it.copy(isLoading = false, error = result.message, showErrorDialog = true) }
                        }
                        is NetworkResult.Loading -> _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    // Input handlers remain the same...
    fun updateName(name: String) { _uiState.update { it.copy(name = name, nameError = validateName(name), isInputValid = validateInput(name = name)) } }
    fun updateUsername(username: String) { _uiState.update { it.copy(username = username, usernameError = validateUsername(username), isInputValid = validateInput(username = username)) } }
    fun updateBio(bio: String) { _uiState.update { it.copy(bio = bio, bioError = validateBio(bio), isInputValid = validateInput(bio = bio)) } }
    fun updateWebsite(website: String) { _uiState.update { it.copy(website = website, websiteError = validateWebsite(website), isInputValid = validateInput(website = website)) } }
    fun updateLocation(location: String) { _uiState.update { it.copy(location = location, locationError = validateLocation(location), isInputValid = validateInput(location = location)) } }

    fun updateProfilePicture(uri: Uri) {
        // We no longer compress immediately. We just hold the URI until save is clicked.
        _uiState.update { it.copy(profilePictureUri = uri) }
    }

    fun saveProfile() {
        val currentState = _uiState.value
        val currentUser = currentState.currentUser ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val updatedUser = currentUser.copy(
                name = currentState.name,
                username = currentState.username,
                bio = currentState.bio,
                website = currentState.website,
                location = currentState.location
            )

            // Delegate to the pure domain UseCase
            when (val result = updateProfileUseCase(updatedUser, currentState.profilePictureUri)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, isUpdateSuccessful = true) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message ?: "Failed to update profile", showErrorDialog = true) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    // Validation logic stays the same...
    private fun validateName(name: String = _uiState.value.name): String? { /* ... */ return null }
    private fun validateUsername(username: String = _uiState.value.username): String? { /* ... */ return null }
    private fun validateBio(bio: String = _uiState.value.bio): String? { /* ... */ return null }
    private fun validateWebsite(website: String = _uiState.value.website): String? { /* ... */ return null }
    private fun validateLocation(location: String = _uiState.value.location): String? { /* ... */ return null }
    private fun validateInput(name: String = _uiState.value.name, username: String = _uiState.value.username, bio: String = _uiState.value.bio, website: String = _uiState.value.website, location: String = _uiState.value.location): Boolean { return true }

    fun setEditing(isEditing: Boolean) { _uiState.update { it.copy(isEditing = isEditing) } }
    fun dismissError() { _uiState.update { it.copy(error = null, showErrorDialog = false) } }
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
    // REMOVED compressedFile. UI State should not hold references to java.io.File!
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