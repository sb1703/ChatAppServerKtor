package com.example.domain.model

import com.google.firebase.messaging.Notification
import kotlinx.serialization.Serializable
import com.google.firebase.messaging.Message

@Serializable
data class SendMessageDto(
    val to: String,
    val notification: NotificationBody
)

@Serializable
data class NotificationBody(
    val userId: String,
    val title: String,
    val body: String,
    val profilePhotoUri: String
)

fun SendMessageDto.toMessage(): Message {
    return Message.builder()
        .putData("userId", notification.userId)
        .putData("title", notification.title)
        .putData("body", notification.body)
        .putData("profilePhotoUri", notification.profilePhotoUri)
        .setToken(to)
        .build()
}
