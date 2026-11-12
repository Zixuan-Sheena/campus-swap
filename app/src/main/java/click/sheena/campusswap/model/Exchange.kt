package click.sheena.campusswap.model

import java.util.UUID
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ExchangeStatus {
    AWAITING_HANDOVER,  // Waiting for both participants to confirm handover
    CANCELLED,          // One party has cancelled the exchange
    COMPLETED           // The exchange is completed
}

@Entity(
    tableName = "exchanges",
    indices = [Index(value = ["proposalId"], unique = true)]
)
data class Exchange(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val proposalId: String,
    val status: ExchangeStatus = ExchangeStatus.AWAITING_HANDOVER,
    val lowConfirmedAt: Long? = null,
    val highConfirmedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt
)
