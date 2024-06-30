package com.example.plugins

import com.example.domain.repository.ConversationDataSource
import com.example.domain.repository.UserDataSource
import com.example.room.RoomController
import com.example.routes.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.java.KoinJavaComponent.inject
import org.koin.ktor.ext.inject

fun Application.configureRouting() {
    routing {
        val userDataSource: UserDataSource by inject(UserDataSource::class.java)
        val conversationDataSource: ConversationDataSource by inject(ConversationDataSource::class.java)
        val roomController by inject<RoomController>()

        rootRoute()
        tokenVerificationRoute(application,userDataSource)
        getUserInfoRoute(application,userDataSource)
        getUserInfoByIdRoute(application,userDataSource)
        updateUserRoute(application,userDataSource)
        updateFCMToken(application,userDataSource)
        deleteUserRoute(application,userDataSource)
        getOnlineStatus(application,roomController)
        getLastLogin(application,userDataSource)
        addChats(application,conversationDataSource)
        addUsers(application,userDataSource)
        fetchChats(application,userDataSource,conversationDataSource)
        fetchLastChat(application,userDataSource,conversationDataSource)
        fetchUsers(application,userDataSource)
        searchUsers(application,userDataSource)
        sendMessageNotification(application)
        chatSocket(roomController,userDataSource)
        signOutRoute()
        authorizedRoute()
        unauthorizedRoute()
    }
}
