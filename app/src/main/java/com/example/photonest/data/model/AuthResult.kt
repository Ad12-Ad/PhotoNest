package com.example.photonest.data.model

data class AuthResult(
    val success: Boolean = false,
    val user: User? = null,
    val errorMessage: String? = null,
    val needsEmailVerification: Boolean = false,
    val needsPhoneVerification: Boolean = false
)