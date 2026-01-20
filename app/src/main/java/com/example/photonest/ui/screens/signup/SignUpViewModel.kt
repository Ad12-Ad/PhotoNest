package com.example.photonest.ui.screens.signup

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.photonest.core.utils.Resource
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.ui.screens.signup.state.SignUpEffect
import com.example.photonest.ui.screens.signup.state.SignUpEvent
import com.example.photonest.ui.screens.signup.state.SignUpUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
            is SignUpEvent.NameChanged -> updateName(event.name)
            is SignUpEvent.UsernameChanged -> updateUsername(event.username)
            is SignUpEvent.EmailChanged -> updateEmail(event.email)
            is SignUpEvent.PasswordChanged -> updatePassword(event.password)
            is SignUpEvent.ConfirmPasswordChanged -> updateConfirmPassword(event.confirmPassword)
            SignUpEvent.Submit -> signUp()
            SignUpEvent.BackClicked -> {
                viewModelScope.launch {
                    _effect.send(SignUpEffect.NavigateBack)
                }
            }
            SignUpEvent.SignInTxtClicked -> {
                viewModelScope.launch {
                    _effect.send(SignUpEffect.NavigateSignIn)
                }
            }
        }
    }

    private fun showError(message: String) {
        viewModelScope.launch {
            _effect.send(SignUpEffect.ShowSnackbar(message))
        }
    }

    private fun updateName(name: String) {
        _uiState.update {
            it.copy(name = name, nameTouched = true)
        }
        validateInput()
    }

    private fun updateUsername(username: String) {
        _uiState.update {
            it.copy(username = username, usernameTouched = true)
        }
        validateInput()
    }

    private fun updateEmail(email: String) {
        _uiState.update {
            it.copy(email = email, emailTouched = true)
        }
        validateInput()
    }

    private fun updatePassword(password: String) {
        _uiState.update {
            it.copy(password = password, passwordTouched = true)
        }
        validateInput()
    }

    private fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update {
            it.copy(confirmPassword = confirmPassword, confirmPasswordTouched = true)
        }
        validateInput()
    }

    private fun signUp() {
        val state = _uiState.value

        // Client-side validation only (no network call)
        if (!state.isInputValid) {
            viewModelScope.launch {
                _effect.send(SignUpEffect.ShowSnackbar("Please fix all errors"))
            }
            return
        }

        if (state.password != state.confirmPassword) {
            viewModelScope.launch {
                _effect.send(SignUpEffect.ShowSnackbar("Passwords do not match"))
            }
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

        val isNameValid = s.name.length >= 2
        val isUsernameValid = s.username.length >= 3
        val isEmailValid = s.email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(s.email).matches()
        val isPasswordValid = s.password.length >= 8

        _uiState.update {
            it.copy(
                nameError = if (s.nameTouched && !isNameValid) "Name must be at least 2 characters" else null,
                usernameError = if (s.usernameTouched && !isUsernameValid) "Username must be at least 3 characters" else null,
                emailError = if (s.emailTouched && !isEmailValid) "Invalid email address" else null,
                passwordError = if (s.passwordTouched && !isPasswordValid) "Password must be at least 8 characters" else null,
                confirmPasswordError = if (s.confirmPasswordTouched && s.password != s.confirmPassword)
                    "Passwords do not match" else null
            )
        }
    }
}