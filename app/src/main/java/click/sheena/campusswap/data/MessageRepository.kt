package click.sheena.campusswap.data

import click.sheena.campusswap.model.Message
import click.sheena.campusswap.model.MessageType

object MessageRepository {
    suspend fun getMessages(conversationId: String): List<Message> =
        CampusSwapDatabase.ready().messages().getForConversation(conversationId)

    suspend fun getLatestMessage(conversationId: String): Message? =
        CampusSwapDatabase.ready().messages().getLatest(conversationId)

    suspend fun sendTextMessage(
        conversationId: String,
        senderId: String,
        text: String
    ): Message? {
        val trimmedText = text.trim()
        if (trimmedText.isBlank()) return null

        return Message(
            conversationId = conversationId,
            senderId = senderId,
            text = trimmedText,
            createdAt = System.currentTimeMillis()
        ).also { CampusSwapDatabase.ready().messages().insert(it) }
    }

    suspend fun sendImageMessage(
        conversationId: String,
        senderId: String,
        imagePath: String
    ): Message {
        return Message(
            conversationId = conversationId,
            senderId = senderId,
            type = MessageType.IMAGE,
            imagePath = imagePath,
            createdAt = System.currentTimeMillis()
        ).also { message ->
            CampusSwapDatabase.ready().messages().insert(message)
        }
    }
}
