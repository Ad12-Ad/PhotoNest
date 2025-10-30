package com.example.photonest.ui.screens.otp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photonest.core.utils.Resource
import com.example.photonest.data.remote.otp.OtpRetrofitClient
import com.example.photonest.data.remote.otp.SendOtpRequest
import com.example.photonest.data.remote.otp.VerifyOtpRequest
import com.example.photonest.domain.repository.IAuthRepository
import com.example.photonest.presentation.auth.otp.OtpUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    fun initialize(email: String, password: String, name: String, username: String) {
        _uiState.update {
            it.copy(
                email = email,
                password = password,
                name = name,
                username = username
            )
        }
        sendOtp()
    }

    fun sendOtp() {
        val currentState = _uiState.value

        if (currentState.email.isBlank()) {
            _uiState.update { it.copy(sendOtpError = "Email is required") }
            return
        }

        viewModelScope.launch {
            try {
                Log.d("OTP_DEBUG", "Sending OTP to ${currentState.email}")

                _uiState.update {
                    it.copy(
                        isSendingOtp = true,
                        sendOtpError = null,
                        otpSent = false
                    )
                }

                val response = OtpRetrofitClient.apiService.sendOtp(
                    SendOtpRequest(currentState.email)
                )

                Log.d("OTP_DEBUG", "Response: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.update {
                        it.copy(
                            isSendingOtp = false,
                            otpSent = true,
                            otpSentTime = System.currentTimeMillis(),
                            sendOtpError = null
                        )
                    }
                    startCountdown()
                    Log.d("OTP_DEBUG", "OTP sent successfully")
                } else {
                    val errorMsg = response.body()?.message ?: "Failed to send OTP"
                    Log.e("OTP_DEBUG", "Error: $errorMsg")
                    _uiState.update {
                        it.copy(
                            isSendingOtp = false,
                            sendOtpError = errorMsg,
                            otpSent = false
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("OTP_DEBUG", "Exception: ${e.message}", e)
                _uiState.update {
                    it.copy(
                        isSendingOtp = false,
                        sendOtpError = "Network error. Please check your connection.",
                        otpSent = false
                    )
                }
            }
        }
    }

    fun verifyOtp(otp: String) {
        val currentState = _uiState.value

        if (otp.length != 4) {
            _uiState.update { it.copy(verifyOtpError = "Please enter complete OTP") }
            return
        }

        val timeSinceOtpSent = System.currentTimeMillis() - currentState.otpSentTime
        val fifteenMinutesInMillis = 15 * 60 * 1000

        if (timeSinceOtpSent > fifteenMinutesInMillis) {
            _uiState.update {
                it.copy(verifyOtpError = "OTP has expired. Please request a new one")
            }
            return
        }

        if (currentState.hasReachedMaxAttempts()) {
            _uiState.update {
                it.copy(verifyOtpError = "Too many attempts. Please request a new OTP")
            }
            return
        }

        viewModelScope.launch {
            try {
                Log.d("OTP_DEBUG", "Verifying OTP: $otp")

                _uiState.update {
                    it.copy(
                        isVerifyingOtp = true,
                        verifyOtpError = null,
                        otpCode = otp
                    )
                }

                val response = OtpRetrofitClient.apiService.verifyOtp(
                    VerifyOtpRequest(currentState.email, otp)
                )

                Log.d("OTP_DEBUG", "Verify response: ${response.code()}")

                if (response.isSuccessful && response.body()?.success == true) {
                    Log.d("OTP_DEBUG", "OTP verified successfully")

                    _uiState.update {
                        it.copy(
                            isVerifyingOtp = false,
                            otpVerified = true,
                            verifyOtpError = null
                        )
                    }

                    createUserAccount()
                } else {
                    val errorMsg = response.body()?.message ?: "Invalid OTP"
                    Log.e("OTP_DEBUG", "Verify error: $errorMsg")

                    _uiState.update {
                        it.copy(
                            isVerifyingOtp = false,
                            verifyOtpError = errorMsg,
                            otpAttempts = it.otpAttempts + 1,
                            otpCode = ""
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("OTP_DEBUG", "Verify exception: ${e.message}", e)

                _uiState.update {
                    it.copy(
                        isVerifyingOtp = false,
                        verifyOtpError = "Network error. Please try again.",
                        otpAttempts = it.otpAttempts + 1,
                        otpCode = ""
                    )
                }
            }
        }
    }

    private fun createUserAccount() {
        val currentState = _uiState.value

        viewModelScope.launch {
            try {
                Log.d("OTP_DEBUG", "Creating account for ${currentState.email}")

                _uiState.update {
                    it.copy(
                        isCreatingAccount = true,
                        accountCreationError = null
                    )
                }

                val result = authRepository.signUpWithEmailAndPassword(
                    email = currentState.email,
                    password = currentState.password,
                    name = currentState.name,
                    username = currentState.username
                )

                when (result) {
                    is Resource.Success -> {
                        if (result.data?.success == true) {
                            Log.d("OTP_DEBUG", "Account created successfully")

                            _uiState.update {
                                it.copy(
                                    isCreatingAccount = false,
                                    accountCreated = true,
                                    accountCreationError = null
                                )
                            }
                            clearSensitiveData()
                        } else {
                            _uiState.update {
                                it.copy(
                                    isCreatingAccount = false,
                                    accountCreationError = "Account creation failed"
                                )
                            }
                        }
                    }
                    is Resource.Error -> {
                        Log.e("OTP_DEBUG", "Account creation error: ${result.message}")

                        _uiState.update {
                            it.copy(
                                isCreatingAccount = false,
                                accountCreationError = result.message ?: "Failed to create account"
                            )
                        }
                    }
                    is Resource.Loading -> {
                        // Already handled
                    }
                }
            } catch (e: Exception) {
                Log.e("OTP_DEBUG", "Create account exception: ${e.message}", e)

                _uiState.update {
                    it.copy(
                        isCreatingAccount = false,
                        accountCreationError = "Error: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    private fun startCountdown() {
        countdownJob?.cancel()

        _uiState.update {
            it.copy(
                resendCountdown = 60,
                isResendEnabled = false
            )
        }

        countdownJob = viewModelScope.launch {
            repeat(60) { second ->
                delay(1000)
                _uiState.update {
                    it.copy(resendCountdown = 59 - second)
                }
            }
            _uiState.update {
                it.copy(
                    resendCountdown = 0,
                    isResendEnabled = true
                )
            }
        }
    }

    fun resendOtp() {
        _uiState.update {
            it.copy(
                otpCode = "",
                otpAttempts = 0,
                verifyOtpError = null,
                sendOtpError = null,
                otpSent = false
            )
        }
        sendOtp()
    }

    private fun clearSensitiveData() {
        _uiState.update {
            it.copy(
                password = "",
                otpCode = ""
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        clearSensitiveData()
    }
}
