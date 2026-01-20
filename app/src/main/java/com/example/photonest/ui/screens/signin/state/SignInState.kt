package com.example.photonest.ui.screens.signin.state

data class SignInState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,

    val emailTouched: Boolean = false,
    val passwordTouched: Boolean = false,

    val emailError:String? = null,
    val passwordError: String? = null
){
    val isInputValid: Boolean
        get() = emailError == null &&
                passwordError == null &&
                email.isNotBlank() &&
                password.isNotBlank()
}