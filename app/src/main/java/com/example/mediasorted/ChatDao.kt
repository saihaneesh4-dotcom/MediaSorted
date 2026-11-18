package com.example.mediasorted

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ChatDao {

    // Insert a chat only if it doesn't already exist.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertChat(chat: ChatEntity): Long

    // Retrieve a chat using its WhatsApp shortcut ID.
    @Query("SELECT * FROM chats WHERE chatId = :chatId LIMIT 1")
    suspend fun getChatById(chatId: String): ChatEntity?

    // Update the display name while preserving the previous name.
    @Query("""
        UPDATE chats
        SET displayName = :newName,
            previousDisplayName = :oldName,
            updatedAt = :updatedAt
        WHERE chatId = :chatId
    """)
    suspend fun updateChatName(
        chatId: String,
        oldName: String,
        newName: String,
        updatedAt: Long
    ): Int

    // Retrieve all registered chats.
    @Query("SELECT * FROM chats ORDER BY displayName COLLATE NOCASE ASC")
    suspend fun getAllChats(): List<ChatEntity>
}