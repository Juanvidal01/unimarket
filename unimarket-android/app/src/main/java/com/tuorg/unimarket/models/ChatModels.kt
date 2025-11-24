package com.tuorg.unimarket.models

import com.google.gson.annotations.SerializedName

// ========== CHAT ==========

data class Chat(
    @SerializedName("_id")
    val id: String,
    val participants: List<ChatUser>,
    val productId: ChatProduct? = null,
    val createdAt: String,
    val updatedAt: String
)

data class ChatUser(
    @SerializedName("_id")
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null
)

data class ChatProduct(
    @SerializedName("_id")
    val id: String,
    val title: String,
    val price: Double,
    val images: List<ProductImage> = emptyList()
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

data class MessagesResponse(
    val messages: List<Message>
)

data class ChatsResponse(
    val chats: List<Chat>
)