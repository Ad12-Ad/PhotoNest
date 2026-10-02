package com.example.photonest.feature.auth.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.NetworkResult
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.domain.repository.IUserRepository
import com.example.photonest.feature.auth.onboarding.model.OnboardingEvent
import com.example.photonest.feature.auth.onboarding.model.OnboardingUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val userRepository: IUserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: OnboardingEvent) {
        when (event) {
            is OnboardingEvent.NameChanged -> _uiState.update { it.copy(name = event.name) }
            is OnboardingEvent.UsernameChanged -> _uiState.update { it.copy(username = event.username) }
            is OnboardingEvent.BioChanged -> _uiState.update { it.copy(bio = event.bio) }
            is OnboardingEvent.BirthdayChanged -> _uiState.update { it.copy(birthday = event.date) }
            is OnboardingEvent.LocationChanged -> _uiState.update { it.copy(location = event.loc) }
            is OnboardingEvent.ProfilePicSelected -> _uiState.update { it.copy(profilePictureUri = event.uri) }
            OnboardingEvent.NextStep -> _uiState.update { it.copy(currentStep = it.currentStep + 1) }
            OnboardingEvent.PreviousStep -> _uiState.update { it.copy(currentStep = it.currentStep - 1) }
            OnboardingEvent.Submit -> saveProfile()
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val s = _uiState.value

            var uploadedPicUrl = ""
            if (s.profilePictureUri != null) {
                val uploadResult = userRepository.uploadProfilePicture(s.profilePictureUri.toString())
                if (uploadResult is NetworkResult.Success) {
                    uploadedPicUrl = uploadResult.data ?: ""
                } else {
                    _uiState.update { it.copy(error = uploadResult.message, isLoading = false) }
                    return@launch
                }
            }

            val result = authRepository.updateOnboardingData(
                name = s.name,
                username = s.username,
                bio = s.bio,
                birthday = s.birthday,
                location = s.location,
                profilePictureUrl = uploadedPicUrl
            )

            if (result is NetworkResult.Success) {
                _uiState.update { it.copy(isFinished = true, isLoading = false) }
            } else {
                _uiState.update { it.copy(error = result.message, isLoading = false) }
            }
        }
    }
}