package com.example.photonest.ui.screens.signup.state

data class SignUpUiState(
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,

    val nameTouched: Boolean = false,
    val usernameTouched: Boolean = false,
    val emailTouched: Boolean = false,
    val passwordTouched: Boolean = false,
    val confirmPasswordTouched: Boolean = false,

    val nameError: String? = null,
    val usernameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null
) {
    val isInputValid: Boolean
        get() = nameError == null &&
                usernameError == null &&
                emailError == null &&
                passwordError == null &&
                confirmPasswordError == null &&
                name.isNotBlank() &&
                username.isNotBlank() &&
                email.isNotBlank()
}