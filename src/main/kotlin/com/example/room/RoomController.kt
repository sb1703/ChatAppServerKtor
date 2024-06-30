package com.example.room

import com.example.domain.model.*
import com.example.domain.repository.ConversationDataSource
import com.example.domain.repository.UserDataSource
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class RoomController(
    private val conversationDataSource: ConversationDataSource,
    private val userDataSource: UserDataSource
) {

    private val json = Json {
        serializersModule = SerializersModule {
            polymorphic(ChatEvent::class) {
                subclass(ChatEvent.MessageEvent::class)
                subclass(ChatEvent.TypingEvent::class)
                subclass(ChatEvent.OnlineEvent::class)
                subclass(ChatEvent.ListEvent::class)
            }
        }
        classDiscriminator = "type"
    }

//    userId to User
    private val users = ConcurrentHashMap<String,User>()

    suspend fun onJoin(
        userId: String,
        sessionId: String,
        socket: WebSocketSession
    ) {

        if(users.containsKey(userId)){
            val user = users[userId]!!
            users[userId] = User(
                userId = user.userId,
                name = user.name,
                id = user.id,
                socket = socket,
                emailAddress = user.emailAddress,
                profilePhoto = user.profilePhoto,
                list = user.list,
                online = user.online,
                lastLogin = user.lastLogin
            )
//            throw UserAlreadyExistsException()
            return
        }

        val user = userDataSource.getUserInfoById(userId)

        if (user != null) {
            sendOnline(userId, true)
            users[userId] = User(
                userId = user.id,
                name = user.name,
                id = sessionId,
                socket = socket,
                emailAddress = user.emailAddress,
                profilePhoto = user.profilePhoto,
                list = user.list,
                online = user.online,
                lastLogin = user.lastLogin
            )
        }
    }

    suspend fun sendMessage(
        senderUserId: String,
        message: String,
        receiverUserIds: List<String>,
        messageId: String
    ) {

        val senderUser = userDataSource.getUserInfoById(senderUserId)
        val receiverUser = userDataSource.getUserInfoById(receiverUserIds[0])

        if (senderUser != null) {
            userDataSource.updateUserList(
                userId = senderUserId,
                list = listOf(receiverUserIds[0]) + senderUser.list.filter { it != receiverUserIds[0] }
            )
        }

        if (receiverUser != null) {
            userDataSource.updateUserList(
                userId = receiverUserIds[0],
                list = listOf(senderUserId) + receiverUser.list.filter { it != senderUserId }
            )
        }

        val chatMessage = ChatEvent.MessageEvent(
            messageText = message,
            receiverUserIds = listOf(senderUserId),
            messageId = messageId
        )

        users.forEach {
            if(it.key in receiverUserIds){
                val jsonText = json.encodeToString(ChatEvent.serializer(), chatMessage)
                it.value.socket?.send(Frame.Text(jsonText))
            }
        }
    }

    suspend fun sendTyping(
        senderUserId: String,
        receiverUserIds: List<String>
    ) {

        val chatTyping = ChatEvent.TypingEvent(
            typingText = "typing...",
            receiverUserIds = listOf(senderUserId)
        )

        users.forEach {
            if(it.key in receiverUserIds){
                val jsonText = json.encodeToString(ChatEvent.serializer(), chatTyping)
                it.value.socket?.send(Frame.Text(jsonText))
            }
        }
    }

    suspend fun sendOnline(
        userId: String,
        online: Boolean
    ) {

        val chatOnline = ChatEvent.OnlineEvent(
            online = online
        )

        users.forEach {
            if(it.key != userId) {
                val jsonText = json.encodeToString(ChatEvent.serializer(), chatOnline)
                it.value.socket?.send(Frame.Text(jsonText))
            }
        }
    }

    suspend fun sendList(
        senderUserId: String,
        receiverUserIds: List<String>
    ) {

        val chatList = ChatEvent.ListEvent(
            receiverUserIds = listOf(senderUserId)
        )

        users.forEach {
            if(it.key in receiverUserIds){
                val jsonText = json.encodeToString(ChatEvent.serializer(), chatList)
                it.value.socket?.send(Frame.Text(jsonText))
            }
        }
    }

    suspend fun sendSeen(
        senderUserId: String,
        receiverUserIds: List<String>,
        messageIds: List<String>,
        seenAt: String
    ) {

        conversationDataSource.updateSeenStatus(
            user1 = senderUserId,
            user2 = receiverUserIds[0],
            messageIds = messageIds,
            seenAt = seenAt
        )

        val chatSeen = ChatEvent.SeenEvent(
            receiverUserIds = listOf(senderUserId),
            messageIds = messageIds,
            seenAt = seenAt
        )

        users.forEach {
            if(it.key in receiverUserIds){
                val jsonText = json.encodeToString(ChatEvent.serializer(), chatSeen)
                it.value.socket?.send(Frame.Text(jsonText))
            }
        }
    }

    fun isOnline(
        userId: String
    ): Boolean {
        return users.containsKey(userId)
    }

    suspend fun updateLastLogin(
        userId: String
    ) {
        val currentTime = Calendar.getInstance().time
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())

        userDataSource.updateUserLastLogin(
            userId = userId,
            lastLogin = sdf.format(currentTime)
        )
    }

    suspend fun tryDisconnect(
        userId: String
    ) {
        updateLastLogin(userId)
        sendOnline(userId,false)
        users[userId]?.socket?.close()
        if(users.containsKey(userId)){
            users.remove(userId)
        }
    }

}