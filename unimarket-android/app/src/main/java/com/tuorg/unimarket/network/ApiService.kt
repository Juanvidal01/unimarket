package com.tuorg.unimarket.network

import com.tuorg.unimarket.ui.home.Product
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========== PRODUCTOS ==========

    // Obtener lista de productos (tu backend devuelve { total, page, limit, products: [...] })
    @GET("products")
    fun getProducts(
        @Query("q") q: String? = null,
        @Query("category") category: String? = null,
        @Query("status") status: String? = "publicado",
        @Query("ownerId") ownerId: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 40
    ): Call<ProductsResponse>

    // Obtener producto por ID
    @GET("products/{id}")
    fun getProductById(@Path("id") id: String): Call<ProductDetailResponse>

    // VERSIÓN SUSPEND para usar con coroutines (opcional)
    @GET("products")
    suspend fun getProductsSuspend(
        @Query("q") q: String? = null,
        @Query("category") category: String? = null,
        @Query("status") status: String? = "publicado",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 40
    ): Response<ProductsResponse>
}

// Response del backend: { total, page, limit, products: [...] }
data class ProductsResponse(
    val total: Int,
    val page: Int,
    val limit: Int,
    val products: List<Product>
)

// Response de detalle: { product: {...} }
data class ProductDetailResponse(
    val product: Product
)