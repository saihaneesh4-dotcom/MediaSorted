package com.example.mediasorted

data class ChatUpdateResult(
    val chat: ChatEntity,
    val isNewChat: Boolean,
    val nameChanged: Boolean,
    val previousName: String? = null
)

class ChatRepository(
    private val chatDao: ChatDao
) {

    suspend fun registerOrUpdateChat(
        chatId: String,
        displayName: String
    ): ChatUpdateResult {

        val existingChat = chatDao.getChatById(chatId)

        // First time encountering this chat
        if (existingChat == null) {

            chatDao.insertChat(
                ChatEntity(
                    chatId = chatId,
                    displayName = displayName
                )
            )

            val savedChat = chatDao.getChatById(chatId)
                ?: throw IllegalStateException("Failed to save chat")

            return ChatUpdateResult(
                chat = savedChat,
                isNewChat = true,
                nameChanged = false
            )
        }

        // Display name has changed
        if (existingChat.displayName != displayName) {

            chatDao.updateChatName(
                chatId = chatId,
                oldName = existingChat.displayName,
                newName = displayName,
                updatedAt = System.currentTimeMillis()
            )

            val updatedChat = chatDao.getChatById(chatId)
                ?: throw IllegalStateException("Failed to update chat")

            return ChatUpdateResult(
                chat = updatedChat,
                isNewChat = false,
                nameChanged = true,
                previousName = existingChat.displayName
            )
        }

        // No change in display name
        return ChatUpdateResult(
            chat = existingChat,
            isNewChat = false,
            nameChanged = false
        )
    }
}