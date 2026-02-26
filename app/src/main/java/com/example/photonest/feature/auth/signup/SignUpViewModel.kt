package com.example.photonest.feature.auth.signup

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.feature.auth.signup.model.SignUpEffect
import com.example.photonest.feature.auth.signup.model.SignUpEvent
import com.example.photonest.feature.auth.signup.model.SignUpUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SignUpEffect>()
    val effect = _effect.receiveAsFlow()

    fun onEvent(event: SignUpEvent) {
        when (event) {
            is SignUpEvent.EmailChanged -> updateEmail(event.email)
            is SignUpEvent.PasswordChanged -> updatePassword(event.password)
            is SignUpEvent.ConfirmPasswordChanged -> updateConfirmPassword(event.confirmPassword)
            SignUpEvent.Submit -> signUp()
            SignUpEvent.BackClicked -> {
                viewModelScope.launch { _effect.send(SignUpEffect.NavigateBack) }
            }
            SignUpEvent.SignInTxtClicked -> {
                viewModelScope.launch { _effect.send(SignUpEffect.NavigateSignIn) }
            }
        }
    }

    private fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, emailTouched = true) }
        validateInput()
    }

    private fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, passwordTouched = true) }
        validateInput()
    }

    private fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordTouched = true) }
        validateInput()
    }

    private fun signUp() {
        val state = _uiState.value
        if (!state.isInputValid) {
            viewModelScope.launch { _effect.send(SignUpEffect.ShowSnackbar("Please fix errors")) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            _effect.send(SignUpEffect.NavigateToOtp)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun validateInput() {
        val s = _uiState.value
        val isEmailValid = s.email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(s.email).matches()
        val isPasswordValid = s.password.length >= 8

        _uiState.update {
            it.copy(
                emailError = if (s.emailTouched && !isEmailValid) "Invalid email address" else null,
                passwordError = if (s.passwordTouched && !isPasswordValid) "Password must be at least 8 characters" else null,
                confirmPasswordError = if (s.confirmPasswordTouched && s.password != s.confirmPassword)
                    "Passwords do not match" else null
            )
        }
    }
}