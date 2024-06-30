package com.example.routes

import com.example.domain.model.*
import com.example.domain.repository.UserDataSource
import com.example.room.RoomController
import io.ktor.client.utils.EmptyContent.status
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.slf4j.LoggerFactory
import java.net.URLDecoder

fun Route.chatSocket(
    roomController: RoomController,
    userDataSource: UserDataSource
) {
    webSocket("/chat-socket") {
        val session = call.sessions.get<ChatSession>()
        if(session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
            return@webSocket
        }
        val json = Json {
            serializersModule = SerializersModule {
                polymorphic(ChatEvent::class) {
                    subclass(ChatEvent.MessageEvent::class)
                    subclass(ChatEvent.TypingEvent::class)
                    subclass(ChatEvent.OnlineEvent::class)
                    subclass(ChatEvent.ListEvent::class)
                    subclass(ChatEvent.SeenEvent::class)
                }
            }
            classDiscriminator = "type"
        }

        try {
            roomController.onJoin(
                userId = session.userId,
                sessionId = session.sessionId,
                socket = this
            )
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val jsonText = frame.readText()


                    when (val event = json.decodeFromString(ChatEvent.serializer(), jsonText)) {
                        is ChatEvent.MessageEvent -> {
                            roomController.sendMessage(
                                senderUserId = session.userId,
                                message = event.messageText,
                                receiverUserIds = event.receiverUserIds,
                                messageId = event.messageId
                            )
                        }
                        is ChatEvent.TypingEvent -> {
                            roomController.sendTyping(
                                senderUserId = session.userId,
                                receiverUserIds = event.receiverUserIds
                            )
                        }

                        is ChatEvent.OnlineEvent -> {  }

                        is ChatEvent.ListEvent -> {
                            roomController.sendList(
                                senderUserId = session.userId,
                                receiverUserIds = event.receiverUserIds
                            )
                        }

                        is ChatEvent.SeenEvent -> {
                            roomController.sendSeen(
                                senderUserId = session.userId,
                                receiverUserIds = event.receiverUserIds,
                                messageIds = event.messageIds,
                                seenAt = event.seenAt
                            )
                        }
                    }
                }
            }
        } catch(e: UserAlreadyExistsException) {
            call.respond(HttpStatusCode.Conflict)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            val user = userDataSource.getUserInfoById(session.userId)
            if (user != null) {
                user.userId?.let { roomController.tryDisconnect(it) }
            }
        }
    }
}