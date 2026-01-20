package com.example.photonest.ui.screens.signup.state

sealed interface SignUpEvent {
    data class NameChanged(val name: String) : SignUpEvent
    data class UsernameChanged(val username: String) : SignUpEvent  // Fixed typo
    data class EmailChanged(val email: String) : SignUpEvent
    data class PasswordChanged(val password: String) : SignUpEvent
    data class ConfirmPasswordChanged(val confirmPassword: String) : SignUpEvent
    data object Submit : SignUpEvent
    data object BackClicked : SignUpEvent
    data object SignInTxtClicked : SignUpEvent
}
