package com.tuorg.unimarket.ui.home

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("_id")
    val _id: String,
    val ownerId: String,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val condition: String,
    val images: List<ProductImage>,
    val status: String,
    val location: ProductLocation? = null,
    val keywords: List<String>? = null,
    val createdAt: String,
    val updatedAt: String
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