package com.example.data.repository

import com.example.domain.model.ApiResponse
import com.example.domain.model.FCMToken
import com.example.domain.model.User
import com.example.domain.repository.UserDataSource
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.regex
import org.litote.kmongo.setValue
import java.util.concurrent.TimeUnit

class UserDataSourceImpl(
    database: CoroutineDatabase
): UserDataSource {

    private val users = database.getCollection<User>()

    override suspend fun getUserInfoById(userId: String): User? {
        return users.findOne(filter = User::userId eq userId)
    }

    override suspend fun getUserInfoByMail(mail: String): User? {
        return users.findOne(filter = User::emailAddress eq mail)
    }

    override suspend fun getUserInfoByUserName(name: String): User? {
        return users.findOne(filter = User::name eq name)
    }

    override suspend fun saveUserInfo(user: User): Boolean {
        val existingUser = users.findOne(filter = User::userId eq user.userId)
        return if (existingUser == null) {
            users.insertOne(document = user).wasAcknowledged()
        } else {
            true
        }
    }

    override suspend fun deleteUser(userId: String): Boolean {
        return users.deleteOne(filter = User::userId eq userId).wasAcknowledged()
    }

    override suspend fun updateUserName(
        userId: String,
        name: String
    ): Boolean {
        return users.updateOne(
            filter = User::userId eq userId,
            update = setValue(
                property = User::name,
                value = name
            )
        ).wasAcknowledged()
    }

    override suspend fun updateUserLastLogin(
        userId: String,
        lastLogin: String
    ): Boolean {
        return users.updateOne(
            filter = User::userId eq userId,
            update = setValue(
                property = User::lastLogin,
                value = lastLogin
            )
        ).wasAcknowledged()
    }

    override suspend fun updateUserList(
        userId: String,
        list: List<String>
    ): Boolean {
        return users.updateOne(
            filter = User::userId eq userId,
            update = setValue(
                property = User::list,
                value = list
            )
        ).wasAcknowledged()
    }

    override suspend fun updateUserFCMToken(
        userId: String,
        fcmToken: FCMToken
    ): Boolean {
        val fcmTokenInDB = users.findOne(filter = User::userId eq userId)?.fcmToken
        if(fcmTokenInDB != null) {
            val expirationDurationMillis = TimeUnit.DAYS.toMillis(60)

            val expirationTime = fcmTokenInDB.timestamp + expirationDurationMillis

            if(expirationTime > fcmToken.timestamp) {
                return false
            }
        }
        return users.updateOne(
            filter = User::userId eq userId,
            update = setValue(
                property = User::fcmToken,
                value = fcmToken
            )
        ).wasAcknowledged()
    }

    override suspend fun fetchUsers(userId: String): ApiResponse {
        val user = users.findOne(filter = User::userId eq userId)

        val userList = user?.list?.mapNotNull { userId ->
            getUserInfoById(userId)
        }

        return if(user != null) {
            ApiResponse(
                success = true,
                message = "ok",
                listUsers = userList ?: emptyList()
            )
        } else {
            ApiResponse(
                success = true,
                message = "ok",
                listUsers = emptyList()
            )
        }

    }

    override suspend fun addUsers(userId: String, userId2: String): Boolean {
        val existingUser = users.findOne(filter = User::userId eq userId)
        val existingUser2 = users.findOne(filter = User::userId eq userId2)
        return if (existingUser == null || existingUser2 == null) {
            false
        } else {
            val list1 = if(!existingUser.list.contains(userId2)) existingUser.list.plus(userId2) else existingUser.list
            val list2 = if(!existingUser2.list.contains(userId)) existingUser2.list.plus(userId) else existingUser2.list
            users.updateOne(
                filter = User::userId eq userId,
                update = setValue(
                    property = User::list,
                    value = list1
                )
            ).wasAcknowledged()
                    &&
            users.updateOne(
                filter = User::userId eq userId2,
                update = setValue(
                    property = User::list,
                    value = list2
                )
            ).wasAcknowledged()
        }
    }

    override suspend fun searchUsersByName(name: String, selfMail: String): ApiResponse {
        val user = users.find(filter = User::name.regex(name,"i"))

        return if ( user.first() != null) {
            ApiResponse(
                success = true,
                message = "ok",
                listUsers = user.toList().filter { user -> user.emailAddress != selfMail }
            )
        } else {
            ApiResponse(
                success = true,
                message = "ok",
                listUsers = emptyList()
            )
        }
    }

}