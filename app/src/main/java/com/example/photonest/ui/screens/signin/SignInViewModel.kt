package com.example.photonest.ui.screens.signin

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.photonest.core.utils.Resource
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.ui.screens.signin.state.SignInEffects
import com.example.photonest.ui.screens.signin.state.SignInEvents
import com.example.photonest.ui.screens.signin.state.SignInState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInState())
    val uiState: StateFlow<SignInState> = _uiState

    private val _effects = Channel<SignInEffects>()
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: SignInEvents){
        when(event){
            is SignInEvents.EmailChanged -> updateEmail(event.email)
            is SignInEvents.PasswordChanged -> updatePassword(event.password)
            SignInEvents.SignInClick -> signIn()
            SignInEvents.SignUpTxtClick -> {
                viewModelScope.launch {
                    _effects.send(SignInEffects.NavigateToSignUp)
                }
            }
            SignInEvents.BackClick -> {
                viewModelScope.launch {
                    _effects.send(SignInEffects.NavigateBack)
                }
            }
        }
    }

    private fun showError(message: String){
        viewModelScope.launch {
            _effects.send(SignInEffects.ShowSnackBar(message))
        }
    }

    private fun updateEmail(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                emailTouched = true
            )
        }
        validateInput()
    }

    private fun updatePassword(password: String) {
        _uiState.update {
            it.copy(
                password = password,
                passwordTouched = true
            )
        }
        validateInput()
    }

    private fun signIn() {
        val currentState = _uiState.value

        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(isLoading = true) }
            }

            val result = authRepository.signInWithEmailAndPassword(
                currentState.email,
                currentState.password
            )

            when (result) {
                is Resource.Success -> {
                    withContext(Dispatchers.Main) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                            )
                        }
                        _effects.send(SignInEffects.NavigateToHome)
                    }
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false) } // 👈 MISSING
                    _effects.send(
                        SignInEffects.ShowSnackBar(
                            result.message ?: "No internet connection"
                        )
                    )
                }
                is Resource.Loading -> {
                    // Already handled
                }
            }
        }
    }

//    fun resetSignInSuccess() {
//        _uiState.update { it.copy(isSignInSuccessful = false) }
//    }


//    fun signInWithGoogle() {
//        viewModelScope.launch(Dispatchers.IO) {
//            withContext(Dispatchers.Main) {
//                _uiState.update { it.copy(isLoading = true, error = null) }
//            }
//
//            val result = authRepository.signInWithGoogle()
//
//            withContext(Dispatchers.Main) {
//                when (result) {
//                    is Resource.Success -> {
//                        _uiState.update {
//                            it.copy(
//                                isLoading = false,
//                                isSignInSuccessful = true,
//                                error = null
//                            )
//                        }
//                    }
//                    is Resource.Error -> {
//                        _uiState.update {
//                            it.copy(
//                                isLoading = false,
//                                error = result.message ?: "Google sign in failed",
//                                showErrorDialog = true
//                            )
//                        }
//                    }
//                    is Resource.Loading -> {
//                        // Already handled above
//                    }
//                }
//            }
//        }
//    }
//
//    fun sendPasswordResetEmail(email: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val result = authRepository.sendPasswordResetEmail(email)
//
//            withContext(Dispatchers.Main) {
//                when (result) {
//                    is Resource.Success -> {
//                        _uiState.update {
//                            it.copy(
//                                error = null,
//                                showPasswordResetDialog = true
//                            )
//                        }
//                    }
//                    is Resource.Error -> {
//                        _uiState.update {
//                            it.copy(
//                                error = result.message ?: "Failed to send password reset email",
//                                showErrorDialog = true
//                            )
//                        }
//                    }
//                    is Resource.Loading -> {
//                        // Handle loading if needed
//                    }
//                }
//            }
//        }
//    }

//    fun dismissErrorDialog() {
//        _uiState.update { it.copy(showErrorDialog = false) }
//    }
//
//    fun dismissPasswordResetDialog() {
//        _uiState.update { it.copy(showPasswordResetDialog = false) }
//    }
//
//    fun resetError() {
//        _uiState.update { it.copy(error = null) }
//    }

    private fun validateInput() {
        val email = _uiState.value.email
        val password = _uiState.value.password

        val isEmailValid = email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

        val isPasswordValid = password.length >= 8


        _uiState.update {
            it.copy(
                emailError = if (_uiState.value.emailTouched && !isEmailValid) "Invalid email address" else null,
                passwordError = if (_uiState.value.passwordTouched && !isPasswordValid) "Password must be at least 8 characters" else null,
            )
        }
    }
}