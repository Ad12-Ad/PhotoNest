package com.example.photonest.ui.screens.signin

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState

    private var hasInteractedWithEmail = false
    private var hasInteractedWithPassword = false

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

    fun signIn() {
        val currentState = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.signInWithEmailAndPassword(
                email = currentState.email,
                password = currentState.password
            )

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSignInSuccessful = true,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Sign in failed",
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

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = authRepository.signInWithGoogle()

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSignInSuccessful = true,
                            error = null
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message ?: "Google sign in failed",
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

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordResetEmail(email)

            when (result) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            error = null,
                            showPasswordResetDialog = true
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            error = result.message ?: "Failed to send password reset email",
                            showErrorDialog = true
                        )
                    }
                }
                is Resource.Loading -> {
                    // Handle loading if needed
                }
            }
        }
    }

    fun dismissErrorDialog() {
        _uiState.update { it.copy(showErrorDialog = false) }
    }

    fun dismissPasswordResetDialog() {
        _uiState.update { it.copy(showPasswordResetDialog = false) }
    }

    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validateInput() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        val isEmailValid = if (hasInteractedWithEmail) {
            Patterns.EMAIL_ADDRESS.matcher(email).matches()
        } else true

        val isPasswordValid = if (hasInteractedWithPassword) {
            password.isNotEmpty()
        } else true

        _uiState.update {
            it.copy(
                isInputValid = isEmailValid && isPasswordValid,
                emailError = if (hasInteractedWithEmail && !isEmailValid) {
                    "Invalid email address"
                } else null,
                passwordError = if (hasInteractedWithPassword && !isPasswordValid) {
                    "Password cannot be empty"
                } else null
            )
        }
    }
}

data class SignInUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isInputValid: Boolean = false,
    val isSignInSuccessful: Boolean = false,
    val error: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val showErrorDialog: Boolean = false,
    val showPasswordResetDialog: Boolean = false
)
