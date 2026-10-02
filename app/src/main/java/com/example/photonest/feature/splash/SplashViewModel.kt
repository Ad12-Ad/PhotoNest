package com.example.photonest.feature.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.domain.repository.IAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        checkAuthenticationState()
    }

    private fun checkAuthenticationState() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.isUserLoggedIn()
                .collect { isLoggedIn ->
                    val isComplete = if (isLoggedIn) authRepository.isOnboardingComplete() else false

                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isUserLoggedIn = isLoggedIn,
                            isOnboardingComplete = isComplete // Ensure this field exists in SplashUiState
                        )
                    }
                }
        }
    }
}

data class SplashUiState(
    val isLoading: Boolean = true,
    val isUserLoggedIn: Boolean = false,
    val isOnboardingComplete: Boolean = false
)
