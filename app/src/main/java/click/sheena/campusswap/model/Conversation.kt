package click.sheena.campusswap.model

import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userLowId: String,
    val userHighId: String,
    val createdAt: Long = System.currentTimeMillis()
)
