package com.example.photonest.feature.auth.signup.model

sealed interface SignUpEffect {
    data object NavigateToOtp : SignUpEffect
    data object NavigateBack : SignUpEffect
    data object NavigateSignIn : SignUpEffect
    data class ShowSnackbar(val message: String) : SignUpEffect
}
