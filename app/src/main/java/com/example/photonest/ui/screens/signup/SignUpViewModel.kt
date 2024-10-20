package com.example.photonest.ui.screens.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignUpViewModel() : ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState

    // To Track user interaction with fields
    private var hasInteractedWithUsername = false
    private var hasInteractedWithEmail = false
    private var hasInteractedWithPassword = false
    private var hasInteractedWithConfirmPassword = false

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

        if(currentState.password != currentState.confirmPassword){
            _uiState.update { it.copy(error = "Password do not match") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val result = auth.createUserWithEmailAndPassword(currentState.email, currentState.password).await()
                result.user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(currentState.username).build())

                _uiState.update { it.copy(isLoading = false, error = null) }

            }catch (e: Exception){
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message?: "An error occurred during sign up"
                    )
                }
            }

        }
    }
    fun resetError() {
        _uiState.update { it.copy(error = null) }
    }


    private fun validateInput() {
        val username = _uiState.value.username
        val email = _uiState.value.email
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword

        val isUsernameValid = if (hasInteractedWithUsername) username.length >= 3 else true
        val isEmailValid = if (hasInteractedWithEmail) android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() else true
        val isPasswordValid = if (hasInteractedWithPassword) password.length >= 8 else true
        val doPasswordsMatch = if (hasInteractedWithConfirmPassword) password == confirmPassword else true

        _uiState.update {
            it.copy(
                isInputValid = isUsernameValid && isEmailValid && isPasswordValid && doPasswordsMatch,
                usernameError = if (hasInteractedWithUsername && !isUsernameValid) "Username must be at least 3 characters long" else null,
                emailError = if (hasInteractedWithEmail && !isEmailValid) "Invalid email address" else null,
                passwordError = if (hasInteractedWithPassword && !isPasswordValid) "Password must be at least 8 characters long" else null,
                confirmPasswordError = if (hasInteractedWithConfirmPassword && !doPasswordsMatch) "Passwords do not match" else null
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