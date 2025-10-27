package com.tuorg.unimarket.network

import com.tuorg.unimarket.ui.home.Product
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path


interface ApiService {
    @GET("products")
    fun getProducts(
        @Query("q") q: String? = null,
        @Query("limit") limit: Int = 40

    ): Call<List<Product>>
    @GET("products/{id}")
    fun getProductById(@Path("id") id: String): Call<com.tuorg.unimarket.ui.home.Product>
}
