package com.example.photonest.data.remote.otp

import com.google.gson.annotations.SerializedName

// Request models
data class SendOtpRequest(
    @SerializedName("email")
    val email: String
)

data class VerifyOtpRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp")
    val otp: String
)

// Response model
data class OtpResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("verificationId")
    val verificationId: String? = null
)
