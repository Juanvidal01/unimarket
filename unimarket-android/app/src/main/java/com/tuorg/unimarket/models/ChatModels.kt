package com.tuorg.unimarket.models

import com.google.gson.annotations.SerializedName

// ========== CHAT ==========

data class Chat(
    @SerializedName("_id")
    val id: String,
    val participants: List<String>,  // IDs de los usuarios
    val productId: String? = null,   // ID del producto (opcional)
    val createdAt: String,
    val updatedAt: String
)

data class Message(
    @SerializedName("_id")
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val read: Boolean = false,
    val createdAt: String
)

// ========== REQUEST/RESPONSE ==========

data class CreateChatRequest(
    val otherUserId: String,
    val productId: String? = null
)

data class SendMessageRequest(
    val content: String
)