package com.example.routes

import com.example.domain.model.*
import com.example.domain.repository.UserDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.updateFCMToken(
    app: Application,
    userDataSource: UserDataSource,
) {
    authenticate("auth-session") {
        post(Endpoint.UpdateFCMToken.path) {
            val userSession = call.principal<UserSession>()
            val fcmToken = call.receive<ApiRequest>().fcmToken
            if (userSession == null) {
                app.log.info("INVALID SESSION")
                call.respondRedirect(Endpoint.Unauthorized.path)
            } else {
                try {
                    if(fcmToken != null) {
                        call.respond(
                            message = ApiResponse(
                                success = userDataSource.updateUserFCMToken(
                                    userId = userDataSource.getUserInfoByMail(mail = userSession.mail)?.userId ?: throw Exception("User not found"),
                                    fcmToken = fcmToken
                                )
                            ),
                            status = HttpStatusCode.OK
                        )
                    } else {
                        app.log.info("INVALID FCM TOKEN")
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