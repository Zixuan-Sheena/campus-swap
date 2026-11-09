package click.sheena.campusswap.data

import click.sheena.campusswap.model.Message
import click.sheena.campusswap.model.MessageType

object MessageRepository {
    private val messages = SampleData.messages.map { it.copy() }.toMutableList()

    fun getMessages(conversationId: String): List<Message> {
        return messages
            .filter { it.conversationId == conversationId }
            .sortedBy { it.createdAt }
            .map { it.copy() }
    }

    fun getLatestMessage(conversationId: String): Message? {
        return messages
            .filter { it.conversationId == conversationId }
            .maxByOrNull { it.createdAt }
            ?.copy()
    }

    fun sendTextMessage(
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
        ).also { messages.add(it) }
    }

    fun sendImageMessage(
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
            messages.add(message)
        }
    }
}
