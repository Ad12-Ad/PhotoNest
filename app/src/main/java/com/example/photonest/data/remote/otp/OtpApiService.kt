package com.example.photonest.data.remote.otp

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface OtpApiService {
    @POST("send-otp")
    suspend fun sendOtp(@Body request: SendOtpRequest): Response<OtpResponse>

    @POST("verify-otp")
    suspend fun verifyOtp(@Body request: VerifyOtpRequest): Response<OtpResponse>
}
