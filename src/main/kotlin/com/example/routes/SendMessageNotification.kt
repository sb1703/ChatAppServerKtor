package com.example.routes

import com.example.domain.model.*
import com.google.firebase.messaging.FirebaseMessaging
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.sendMessageNotification(
    app: Application,
) {
    authenticate("auth-session") {
        post(Endpoint.SendMessageNotification.path) {
            val userSession = call.principal<UserSession>()
            val body = call.receiveNullable<SendMessageDto>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }
            if (userSession == null) {
                app.log.info("INVALID SESSION")
                call.respondRedirect(Endpoint.Unauthorized.path)
            } else {
                try {
                    FirebaseMessaging.getInstance().send(body.toMessage())

                    call.respond(HttpStatusCode.OK)
                } catch (e: Exception) {
                    app.log.info("GETTING USER INFO ERROR: ${e.message}")
                    call.respondRedirect(Endpoint.Unauthorized.path)
                }
            }
        }
    }
}