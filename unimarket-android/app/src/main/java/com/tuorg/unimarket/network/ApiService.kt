package com.tuorg.unimarket.network

import com.tuorg.unimarket.models.*
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

    // Actualizar producto
    @PATCH("products/{id}")
    suspend fun updateProduct(
        @Path("id") productId: String,
        @Body request: UpdateProductRequest
    ): Response<CreateProductResponse>

    // Eliminar producto
    @DELETE("products/{id}")
    suspend fun deleteProduct(
        @Path("id") productId: String
    ): Response<DeleteResponse>

    // Eliminar imagen de producto
    @DELETE("products/{id}/images")
    suspend fun deleteProductImage(
        @Path("id") productId: String,
        @Query("publicId") publicId: String
    ): Response<DeleteImageResponse>

    // ========== CHAT ==========

    // Crear o obtener chat existente
    @POST("chats")
    suspend fun createChat(
        @Body request: CreateChatRequest
    ): Response<Chat>

    // Obtener todos mis chats
    @GET("chats")
    suspend fun getChats(): Response<List<Chat>>

    // Enviar mensaje en un chat
    @POST("chats/{id}/messages")
    suspend fun sendMessage(
        @Path("id") chatId: String,
        @Body request: SendMessageRequest
    ): Response<Message>

    // Obtener mensajes de un chat
    @GET("chats/{id}/messages")
    suspend fun getChatMessages(
        @Path("id") chatId: String
    ): Response<List<Message>>
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

// Request para actualizar producto
data class UpdateProductRequest(
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val price: Double? = null,
    val condition: String? = null,
    val status: String? = null
)

// Response de eliminación
data class DeleteResponse(
    val ok: Boolean
)

// Response de eliminar imagen
data class DeleteImageResponse(
    val ok: Boolean,
    val product: Product
)