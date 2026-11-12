package click.sheena.campusswap.model

import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageType {
    TEXT,
    IMAGE,
    PROPOSAL
}

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val senderId: String,
    val type: MessageType = MessageType.TEXT,
    val text: String? = null,
    val proposalId: String? = null,
    val createdAt: Long,
    val imagePath: String? = null
)