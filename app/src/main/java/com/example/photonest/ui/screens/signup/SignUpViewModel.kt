package com.example.photonest.ui.screens.singup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignUpViewModel(
//    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    fun updateUsername(username: String) {
        _uiState.update { it.copy(username = username) }
        validateInput()
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
        validateInput()
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
        validateInput()
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
        validateInput()
    }

    fun signUp() {
        if (!_uiState.value.isInputValid) return

        _uiState.update { it.copy(isLoading = true, error = null) }

//        viewModelScope.launch {
//            try {
//                userRepository.signUp(
//                    _uiState.value.username,
//                    _uiState.value.email,
//                    _uiState.value.password
//                )
//                _uiState.update { it.copy(isSignUpSuccessful = true, isLoading = false) }
//            } catch (e: Exception) {
//                _uiState.update {
//                    it.copy(error = e.message ?: "An unknown error occurred", isLoading = false)
//                }
//            }
//        }
    }

    private fun validateInput() {
        val username = _uiState.value.username
        val email = _uiState.value.email
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        val isUsernameValid = username.length >= 3
        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = password.length >= 8
        val doPasswordsMatch = password == confirmPassword

        _uiState.update {
            it.copy(
                isInputValid = isUsernameValid && isEmailValid && isPasswordValid && doPasswordsMatch,
                usernameError = if (isUsernameValid) null else "Username must be at least 3 characters long",
                emailError = if (isEmailValid) null else "Invalid email address",
                passwordError = if (isPasswordValid) null else "Password must be at least 8 characters long",
                confirmPasswordError = if (doPasswordsMatch) null else "Passwords do not match"
            )
        }
    }
}

data class SignUpUiState(
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isInputValid: Boolean = false,
    val isSignUpSuccessful: Boolean = false,
    val error: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
)