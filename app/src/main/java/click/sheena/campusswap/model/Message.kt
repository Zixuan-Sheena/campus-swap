package click.sheena.campusswap.model

import java.util.UUID

enum class MessageType {
    TEXT,
    IMAGE,
    PROPOSAL
}

data class Message(
    val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val senderId: String,
    val type: MessageType = MessageType.TEXT,
    val text: String? = null,
    val proposalId: String? = null,
    val createdAt: Long,
    val imagePath: String? = null
)