package com.example.domain.repository

import com.example.domain.model.ApiResponse
import com.example.domain.model.FCMToken
import com.example.domain.model.User

interface UserDataSource {
    suspend fun getUserInfoById(userId: String): User?
    suspend fun getUserInfoByMail(mail: String): User?
    suspend fun getUserInfoByUserName(name: String): User?
    suspend fun saveUserInfo(user: User): Boolean
    suspend fun deleteUser(userId: String): Boolean
    suspend fun updateUserName(
        userId: String,
        name: String
    ): Boolean

    suspend fun updateUserLastLogin(
        userId: String,
        lastLogin: String
    ): Boolean

    suspend fun updateUserList(
        userId: String,
        list: List<String>
    ): Boolean

    suspend fun updateUserFCMToken(
        userId: String,
        fcmToken: FCMToken
    ): Boolean
    suspend fun fetchUsers(userId: String): ApiResponse
    suspend fun addUsers(userId: String, userId2: String): Boolean
    suspend fun searchUsersByName(name: String, selfMail: String): ApiResponse
}