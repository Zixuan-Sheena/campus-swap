package click.sheena.campusswap.model

data class Message(
    val id: Long = 0,
    val conversationId: Long,
    val senderId: Long,
    val type: MessageType = MessageType.TEXT,
    val text: String? = null,
    val proposalId: Long? = null,
    val createdAt: Long
)