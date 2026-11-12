package click.sheena.campusswap.model

import java.util.UUID
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ProposalStatus {
    PENDING,        // Proposal has been sent but not yet responded to
    SUPERSEDED,     // Proposal has been superseded by a newer version
    REJECTED,       // Proposal has been rejected
    INVALIDATED,    // Proposal has been invalidated due to changes in the item or exchange
    ACCEPTED,       // Proposal has been accepted
    WITHDRAWN       // Proposal has been withdrawn by its proposer
}

@Entity(
    tableName = "proposals",
    indices = [Index(value = ["conversationId", "version"], unique = true)]
)
data class Proposal(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val version: Int,
    val proposerId: String,
    val receiverId: String,
    val status: ProposalStatus = ProposalStatus.PENDING,
    val meetingAt: Long,
    val eircode: String,
    val createdAt: Long
)
