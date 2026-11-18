package com.example.mediasorted

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(

    @PrimaryKey
    val chatId: String,

    val displayName: String,

    val previousDisplayName: String? = null,

    val updatedAt: Long = System.currentTimeMillis()
)