package com.example.routes

import com.example.domain.model.*
import com.example.domain.repository.ConversationDataSource
import com.example.domain.repository.UserDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.addChats(
    app: Application,
    conversationDataSource: ConversationDataSource
) {
    authenticate("auth-session") {
        post(Endpoint.AddChats.path) {
            val userSession = call.principal<UserSession>()
            val message = call.receive<ApiRequest>().message
            if (userSession == null) {
                app.log.info("INVALID SESSION")
                call.respondRedirect(Endpoint.Unauthorized.path)
            } else {
                try {
                    if(message != null) {
                        if(message.author != null && message.receiver.isNotEmpty()) {
                            val messageEntity = Message(
                                author = message.author,
                                receiver = message.receiver,
                                messageText = message.messageText
                            )
                            call.respond(
                                message = ApiResponse(
                                    success = conversationDataSource.addChats(message.author,message.receiver.first(),messageEntity),
                                    messageId = messageEntity.messageId.toString()
                                ),
                                status = HttpStatusCode.OK
                            )
                        } else {
                            app.log.info("INVALID USER_ID")
                            call.respondRedirect(Endpoint.Unauthorized.path)
                        }
                    } else {
                        app.log.info("INVALID MESSAGE")
                        call.respondRedirect(Endpoint.Unauthorized.path)
                    }
                } catch (e: Exception) {
                    app.log.info("GETTING USER INFO ERROR: ${e.message}")
                    call.respondRedirect(Endpoint.Unauthorized.path)
                }
            }
        }
    }
}