package click.sheena.campusswap.model

import java.util.UUID

data class Conversation(
    val id: String = UUID.randomUUID().toString(),
    val userLowId: String,
    val userHighId: String,
    val createdAt: Long = System.currentTimeMillis()
)
