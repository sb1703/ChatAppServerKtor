package com.example.di

import com.example.data.repository.ConversationDataSourceImpl
import com.example.data.repository.UserDataSourceImpl
import com.example.domain.repository.ConversationDataSource
import com.example.domain.repository.UserDataSource
import com.example.room.RoomController
import com.example.util.Constants.CHAT_DATABASE
import com.example.util.Constants.MONGODB_URI_TEST
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val koinModule = module {
    single {
//        KMongo.createClient(System.getenv("MONGODB_URI"))
        KMongo.createClient(MONGODB_URI_TEST)
            .coroutine
            .getDatabase(CHAT_DATABASE)
    }
    single<UserDataSource> {
        UserDataSourceImpl(get())
    }
    single<ConversationDataSource> {
        ConversationDataSourceImpl(get())
    }
    single {
        RoomController(get(),get())
    }
}