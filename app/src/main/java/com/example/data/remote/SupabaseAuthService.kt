package com.example.data.remote

import com.example.data.remote.dto.SupabaseAuthResponse
import com.example.data.remote.dto.SupabaseSignInRequest
import com.example.data.remote.dto.SupabaseSignUpRequest
import com.example.data.remote.dto.SupabaseUserDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface SupabaseAuthService {

    @POST("auth/v1/signup")
    @Headers("Content-Type: application/json")
    suspend fun signUp(
        @Body request: SupabaseSignUpRequest
    ): Response<SupabaseAuthResponse>

    @POST("auth/v1/token")
    @Headers("Content-Type: application/json")
    suspend fun signInWithPassword(
        @Query("grant_type") grantType: String = "password",
        @Body request: SupabaseSignInRequest
    ): Response<SupabaseAuthResponse>

    @POST("auth/v1/logout")
    suspend fun logout(
        @Header("Authorization") userBearer: String
    ): Response<ResponseBody>

    @GET("auth/v1/user")
    suspend fun getCurrentUser(
        @Header("Authorization") userBearer: String
    ): Response<SupabaseUserDto>
}
