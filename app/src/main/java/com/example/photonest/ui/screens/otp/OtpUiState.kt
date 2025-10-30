package com.example.photonest.presentation.auth.otp

data class OtpUiState(
    // User data
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val username: String = "",

    // OTP sending state
    val isSendingOtp: Boolean = false,
    val otpSent: Boolean = false,
    val otpSentTime: Long = 0L,
    val sendOtpError: String? = null,

    // OTP verification state
    val otpCode: String = "",
    val isVerifyingOtp: Boolean = false,
    val otpVerified: Boolean = false,
    val verifyOtpError: String? = null,
    val otpAttempts: Int = 0,

    // Account creation state
    val isCreatingAccount: Boolean = false,
    val accountCreated: Boolean = false,
    val accountCreationError: String? = null,

    // Resend countdown
    val resendCountdown: Int = 60,
    val isResendEnabled: Boolean = false
) {
    fun isOtpExpired(): Boolean {
        val timeSinceOtpSent = System.currentTimeMillis() - otpSentTime
        val fifteenMinutesInMillis = 15 * 60 * 1000
        return timeSinceOtpSent > fifteenMinutesInMillis
    }

    fun hasReachedMaxAttempts(): Boolean = otpAttempts >= 5
}
