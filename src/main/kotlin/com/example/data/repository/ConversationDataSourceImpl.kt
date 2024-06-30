package com.example.data.repository

import com.example.domain.model.ApiResponse
import com.example.domain.model.Conversation
import com.example.domain.model.Message
import com.example.domain.model.SeenBy
import com.example.domain.repository.ConversationDataSource
import org.litote.kmongo.and
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.setValue

class ConversationDataSourceImpl(
    database: CoroutineDatabase
): ConversationDataSource {

    private val conversations = database.getCollection<Conversation>()

    override suspend fun fetchChats(user1: String, user2: String): ApiResponse {
        val conversation = conversations.findOne(and(Conversation::member.contains(user1),Conversation::member.contains(user2)))
        return if(conversation != null) {
            ApiResponse(
                success = true,
                message = "ok",
                listMessages = conversation.messages
            )
        } else {
            ApiResponse(
                success = true,
                message = "ok",
                listMessages = emptyList()
            )
        }
    }

    override suspend fun fetchLastChat(user1: String, user2: String): ApiResponse {
        val conversation = conversations.findOne(and(Conversation::member.contains(user1),Conversation::member.contains(user2)))
        return if(conversation != null) {
            ApiResponse(
                success = true,
                chat = conversation.messages.last()
            )
        } else {
            ApiResponse(
                success = false,
                message = "No Last Message Found"
            )
        }
    }

    override suspend fun addChats(user1: String, user2: String, msg: Message): Boolean {
        val conversation = conversations.findOne(and(Conversation::member.contains(user1), Conversation::member.contains(user2)))
        val list = conversation?.messages?.plus(msg)
        return if (conversation != null) {
            conversations.updateOne(
                filter = Conversation::conversationId eq conversation.conversationId,
                update = setValue(
                    property = Conversation::messages,
                    value = list
                )
            ).wasAcknowledged()
        } else {
            conversations.insertOne(
                Conversation(
                    member = listOf(user1,user2),
                    messages = listOf(msg)
                )
            ).wasAcknowledged()
        }
    }

    override suspend fun updateSeenStatus(user1: String, user2: String, messageIds: List<String>, seenAt: String): Boolean {
        val conversation = conversations.findOne(and(Conversation::member.contains(user1),Conversation::member.contains(user2)))
        return if(conversation != null) {
            val messages = conversation.messages.map {
                if(it.messageId in messageIds) {
                    it.copy(seenBy = it.seenBy.plus(SeenBy(userId = user1, seenAt = seenAt)))
                } else {
                    it
                }
            }
            conversations.updateOne(
                filter = Conversation::conversationId eq conversation.conversationId,
                update = setValue(
                    property = Conversation::messages,
                    value = messages
                )
            ).wasAcknowledged()
        } else {
            false
        }
    }

}