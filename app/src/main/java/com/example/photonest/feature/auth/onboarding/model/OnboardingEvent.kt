package com.example.photonest.feature.auth.onboarding.model

import android.net.Uri

sealed interface OnboardingEvent {
    data class NameChanged(val name: String) : OnboardingEvent
    data class UsernameChanged(val username: String) : OnboardingEvent
    data class BioChanged(val bio: String) : OnboardingEvent
    data class BirthdayChanged(val date: String) : OnboardingEvent
    data class LocationChanged(val loc: String) : OnboardingEvent
    data class ProfilePicSelected(val uri: Uri?) : OnboardingEvent
    object NextStep : OnboardingEvent
    object PreviousStep : OnboardingEvent
    object Submit : OnboardingEvent
}