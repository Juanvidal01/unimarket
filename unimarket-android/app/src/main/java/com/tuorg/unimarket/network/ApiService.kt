package com.tuorg.unimarket.network

import com.tuorg.unimarket.ui.home.Product
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========== PRODUCTOS ==========

    // Obtener lista de productos
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

    // Crear producto (PASO 1: sin imágenes)
    @POST("products")
    suspend fun createProductSuspend(
        @Body request: CreateProductRequest
    ): Response<CreateProductResponse>

    // Subir imágenes a producto existente (PASO 2)
    @Multipart
    @POST("products/{id}/images")
    suspend fun uploadProductImagesSuspend(
        @Path("id") productId: String,
        @Part images: List<MultipartBody.Part>
    ): Response<UploadImagesResponse>
}

// ========== RESPONSE MODELS ==========

data class ProductsResponse(
    val total: Int,
    val page: Int,
    val limit: Int,
    val products: List<Product>
)

data class ProductDetailResponse(
    val product: Product
)

data class CreateProductResponse(
    val product: Product
)

data class UploadImagesResponse(
    val added: Int,
    val images: List<ProductImage>,
    val product: Product
)

// ========== REQUEST MODELS ==========

data class CreateProductRequest(
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val condition: String,
    val location: ProductLocation?,
    val keywords: List<String>,
    val images: List<ProductImage>
)

data class ProductImage(
    val url: String,
    val publicId: String
)

data class ProductLocation(
    val campus: String? = null,
    val city: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)