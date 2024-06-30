package com.example.routes

import com.example.domain.model.*
import com.example.room.RoomController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getOnlineStatus(
    app: Application,
    roomController: RoomController
) {
    authenticate("auth-session") {
        post(Endpoint.GetOnlineStatus.path) {
            val userSession = call.principal<UserSession>()
            val userId = call.receive<ApiRequest>().userId
            if (userSession == null) {
                app.log.info("INVALID SESSION")
                call.respondRedirect(Endpoint.Unauthorized.path)
            } else {
                try {
                    if(userId != null) {
                        app.log.info("ONLINE - ${roomController.isOnline(userId)}")
                        call.respond(
                            message = ApiResponse(
                                success = true,
                                online = roomController.isOnline(userId)
                            ),
                            status = HttpStatusCode.OK
                        )
                    } else {
                        app.log.info("INVALID USER_ID")
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