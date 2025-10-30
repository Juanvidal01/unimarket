package com.tuorg.unimarket.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Clase para almacenar el token JWT
object TokenStore {
    var jwt: String? = null
}

// Interceptor que agrega el token JWT automáticamente
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        TokenStore.jwt?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(requestBuilder.build())
    }
}

// Cliente API con logging y autenticación
object ApiClient {

    // ⚠️ Importante: agregar "/" al final si tus endpoints son relativos
    private const val BASE_URL = "http://192.168.1.17:8080/"

    // Interceptor para ver los logs de las peticiones
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Cambia a Level.NONE en producción si no quieres ver logs sensibles
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Cliente HTTP configurado con interceptores
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .addInterceptor(loggingInterceptor)
        .build()

    // Instancia de Retrofit global
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
