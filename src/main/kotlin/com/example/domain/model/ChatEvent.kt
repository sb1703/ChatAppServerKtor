package com.example.domain.model

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Polymorphic
sealed class ChatEvent {
    @Serializable
    @SerialName("MessageEvent")
    data class MessageEvent(val messageText: String, val receiverUserIds: List<String>, val messageId: String = "") : ChatEvent()

    @Serializable
    @SerialName("TypingEvent")
    data class TypingEvent(val typingText: String = "typing...", val receiverUserIds: List<String>) : ChatEvent()

    @Serializable
    @SerialName("OnlineEvent")
    data class OnlineEvent(val online: Boolean) : ChatEvent()

    @Serializable
    @SerialName("ListEvent")
    data class ListEvent(val receiverUserIds: List<String>) : ChatEvent()

    @Serializable
    @SerialName("SeenEvent")
    data class SeenEvent(
        val receiverUserIds: List<String>,
        val messageIds: List<String>,
        val seenAt: String = getCurrentTimeIn12HourFormat()
    ) : ChatEvent()
}
