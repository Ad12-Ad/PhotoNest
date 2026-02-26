package com.example.photonest.feature.auth.signin.model

sealed class SignInEffects {
    data class ShowSnackBar(val message: String): SignInEffects()
    data object NavigateToHome: SignInEffects()
    data object NavigateToSignUp: SignInEffects()
    data object NavigateBack: SignInEffects()
}