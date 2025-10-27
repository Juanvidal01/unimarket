package com.tuorg.unimarket.ui.home

data class Product(
    val _id: String,
    val title: String,
    val description: String?,
    val price: Double,
    val images: List<String> = emptyList()
)