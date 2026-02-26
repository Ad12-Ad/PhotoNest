package com.example.photonest.feature.auth.signin.model

sealed class SignInEvents {
    data class EmailChanged(val email: String): SignInEvents()
    data class PasswordChanged(val password: String): SignInEvents()

    data object BackClick: SignInEvents()
    data object SignUpTxtClick: SignInEvents()
    data object SignInClick: SignInEvents()
}