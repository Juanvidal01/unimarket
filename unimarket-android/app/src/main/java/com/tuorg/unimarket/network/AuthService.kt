package com.tuorg.unimarket.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

data class RegisterRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)

data class UserDto(val id: String?, val _id: String?, val name: String, val email: String, val role: String?) {
    val userId: String get() = id ?: _id ?: ""
}
data class AuthResponse(val token: String, val user: UserDto)

interface AuthService {
    @POST("auth/register") fun register(@Body body: RegisterRequest): Call<AuthResponse>
    @POST("auth/login")    fun login(@Body body: LoginRequest): Call<AuthResponse>
    @GET("auth/me")        fun me(): Call<Map<String, Any>>
}
