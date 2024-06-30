package com.example.domain.repository

import com.example.domain.model.ApiResponse
import com.example.domain.model.Message
import com.example.domain.model.User

interface ConversationDataSource {
    suspend fun fetchChats(user1: String, user2: String): ApiResponse

    suspend fun fetchLastChat(user1: String,user2: String): ApiResponse

    suspend fun addChats(user1: String, user2: String, msg: Message): Boolean

    suspend fun updateSeenStatus(user1: String, user2: String, messageIds: List<String>, seenAt: String): Boolean
}