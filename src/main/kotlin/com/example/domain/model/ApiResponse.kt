package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse(
    val success: Boolean,
    val user: User? = null,
    val chat: Message? = null,
    var message: String? = null,
    val listMessages: List<Message> = emptyList(),
    val listUsers: List<User> = emptyList(),
    val online: Boolean = false,
    val lastLogin: String? = null,
    val messageId: String = ""
)
