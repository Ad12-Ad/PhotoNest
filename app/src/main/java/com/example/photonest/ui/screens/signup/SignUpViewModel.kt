package com.example.photonest.ui.screens.signup

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    // To Track user interaction with fields
    private var hasInteractedWithName = false
    private var hasInteractedWithUsername = false
    private var hasInteractedWithEmail = false
    private var hasInteractedWithPassword = false
    private var hasInteractedWithConfirmPassword = false

    fun updateName(name: String) {
        hasInteractedWithName = true
        _uiState.update { it.copy(name = name) }
        validateInput()
    }

    fun updateUsername(username: String) {
        hasInteractedWithUsername = true
        _uiState.update { it.copy(username = username) }
        validateInput()
    }

    fun updateEmail(email: String) {
        hasInteractedWithEmail = true
        _uiState.update { it.copy(email = email) }
        validateInput()
    }

    fun updatePassword(password: String) {
        hasInteractedWithPassword = true
        _uiState.update { it.copy(password = password) }
        validateInput()
    }

    fun updateConfirmPassword(confirmPassword: String) {
        hasInteractedWithConfirmPassword = true
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
        validateInput()
    }

    fun signUp() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update {
                it.copy(
                    error = "Name is required",
                    showErrorDialog = true
                )
            }
            return
        }

        if (currentState.username.isBlank()) {
            _uiState.update {
                it.copy(
                    error = "Username is required",
                    showErrorDialog = true
                )
            }
            return
        }

        if (currentState.email.isBlank()) {
            _uiState.update {
                it.copy(
                    error = "Email is required",
                    showErrorDialog = true
                )
            }
            return
        }

        if (currentState.password != currentState.confirmPassword) {
            _uiState.update {
                it.copy(
                    error = "Passwords do not match",
                    showErrorDialog = true
                )
            }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSignUpSuccessful = true,  // This triggers navigation
                        error = null
                    )
                }
            }
        }
    }

    fun signUpWithGoogle() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true, error = null) }
            }

            val result = authRepository.signInWithGoogle()

            withContext(Dispatchers.Main) {
                when (result) {
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSignUpSuccessful = true,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message ?: "Google sign up failed",
                                showErrorDialog = true
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Already handled above
                    }
                }
            }

        }
    }

    fun dismissErrorDialog() {
        _uiState.update { it.copy(showErrorDialog = false) }
    }

    fun resetSignUpSuccess() {
        _uiState.update { it.copy(isSignUpSuccessful = false) }
    }


    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validateInput() {
        val name = _uiState.value.name
        val username = _uiState.value.username
        val email = _uiState.value.email
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        val isNameValid = if (hasInteractedWithName) name.length >= 2 else true
        val isUsernameValid = if (hasInteractedWithUsername) username.length >= 3 else true
        val isEmailValid = if (hasInteractedWithEmail) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else true
        val isPasswordValid = if (hasInteractedWithPassword) password.length >= 8 else true
        val doPasswordsMatch = if (hasInteractedWithConfirmPassword) {
            password == confirmPassword
        } else true

        _uiState.update {
            it.copy(
                isInputValid = isNameValid && isUsernameValid && isEmailValid && isPasswordValid && doPasswordsMatch,
                nameError = if (hasInteractedWithName && !isNameValid) {
                    "Name must be at least 2 characters long"
                } else null,
                usernameError = if (hasInteractedWithUsername && !isUsernameValid) {
                    "Username must be at least 3 characters long"
                } else null,
                emailError = if (hasInteractedWithEmail && !isEmailValid) {
                    "Invalid email address"
                } else null,
                passwordError = if (hasInteractedWithPassword && !isPasswordValid) {
                    "Password must be at least 8 characters long"
                } else null,
                confirmPasswordError = if (hasInteractedWithConfirmPassword && !doPasswordsMatch) {
                    "Passwords do not match"
                } else null
            )
        }
    }
}

data class SignUpUiState(
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isInputValid: Boolean = false,
    val isSignUpSuccessful: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val showErrorDialog: Boolean = false
)
