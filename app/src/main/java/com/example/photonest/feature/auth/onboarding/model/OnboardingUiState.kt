package com.example.photonest.feature.auth.onboarding.model

import android.net.Uri

data class OnboardingUiState(
    val currentStep: Int = 1,
    val name: String = "",
    val username: String = "",
    val profilePictureUri: Uri? = null,
    val bio: String = "",
    val birthday: String = "",
    val location: String = "",
    val isLoading: Boolean = false,
    val isFinished: Boolean = false,
    val error: String? = null
)