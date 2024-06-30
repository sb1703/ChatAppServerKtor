package com.example.domain.model

import io.ktor.websocket.*
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class User(
    val id: String,
    @BsonId
    val userId: String? = ObjectId.get().toString(),
    val name: String,
    val emailAddress: String,
    val profilePhoto: String,
    val list: List<String> = emptyList(),
    val online: Boolean = false,
    val lastLogin: String? = null,
    val socket: WebSocketSession? = null,
    val fcmToken: FCMToken? = null
)

@Serializable
data class FCMToken(
    val token: String,
    val timestamp: Long = System.currentTimeMillis()
)

class UserAlreadyExistsException: Exception("There is already a user with that name in the room")
