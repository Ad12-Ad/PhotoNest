package com.example.photonest.feature.profile.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.model.User
import com.example.photonest.domain.model.UserProfile
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.core.ui.components.UserListType
import com.example.photonest.domain.repository.IAuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: IUserRepository,
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile(userId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main){
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            try {
                // If userId is null, get current user profile
                val targetUserId = userId ?: run {
                    val currentUserResult = FirebaseAuth.getInstance().currentUser?.uid
                    if (currentUserResult == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = "User not authenticated. Please sign in again.",
                                showErrorDialog = true
                            )
                        }
                        return@launch
                    }
                    currentUserResult
                }

                val result = userRepository.getUserProfile(targetUserId)

                withContext(Dispatchers.Main){
                    when (result) {
                        is NetworkResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    userProfile = result.data,
                                    error = null
                                )
                            }
                        }
                        is NetworkResult.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = result.message ?: "Failed to load profile",
                                    showErrorDialog = true
                                )
                            }
                        }
                        is NetworkResult.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main){
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
    }

    fun loadFollowers(userId: String, onResult: (List<User>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = userRepository.getFollowers(userId)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is NetworkResult.Success -> {
                            onResult(result.data ?: emptyList())
                        }
                        is NetworkResult.Error -> {
                            onResult(emptyList())
                            _uiState.update {
                                it.copy(
                                    error = result.message ?: "Failed to load followers",
                                    showErrorDialog = true
                                )
                            }
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun loadFollowing(userId: String, onResult: (List<User>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = userRepository.getFollowing(userId)
                withContext(Dispatchers.Main) {
                    when (result) {
                        is NetworkResult.Success -> {
                            onResult(result.data ?: emptyList())
                        }
                        is NetworkResult.Error -> {
                            onResult(emptyList())
                            _uiState.update {
                                it.copy(
                                    error = result.message ?: "Failed to load following",
                                    showErrorDialog = true
                                )
                            }
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(emptyList())
                }
            }
        }
    }

    fun logOut(onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = authRepository.signOut()

            withContext(Dispatchers.Main) {
                if (result is NetworkResult.Success) {
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            error = result.message ?: "Failed to log out",
                            showErrorDialog = true
                        )
                    }
                }
            }
        }
    }

    fun showDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteAccountDialog = true) }
    }

    fun hideDeleteAccountDialog() {
        _uiState.update { it.copy(showDeleteAccountDialog = false) }
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true, showDeleteAccountDialog = false) }
            }

            // Delegates to your existing AuthRepositoryImpl logic
            val result = authRepository.deleteAccount()

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = false) }
                when (result) {
                    is NetworkResult.Success -> {
                        onSuccess() // Boot them out to the login screen
                    }
                    is NetworkResult.Error -> {
                        _uiState.update {
                            it.copy(
                                error = result.message ?: "Failed to delete account",
                                showErrorDialog = true
                            )
                        }
                    }
                    is NetworkResult.Loading -> {}
                }
            }
        }
    }
    fun dismissError() {
        _uiState.update {
            it.copy(
                error = null,
                showErrorDialog = false
            )
        }
    }

    fun refreshProfile() {
        _uiState.update { it.copy(isRefreshing = true) }
        loadUserProfile()
        _uiState.update { it.copy(isRefreshing = false) }
    }
}

data class ProfileUiState(
    val isLoading: Boolean = false,
    val userProfile: UserProfile? = null,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val showErrorDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false
)
