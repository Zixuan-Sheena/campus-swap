package click.sheena.campusswap.model

import java.util.UUID

enum class ExchangeStatus {
    AWAITING_HANDOVER,  // Waiting for the buyer to hand over the item to the seller, no more proposals can be made
    CANCELLED,          // One party has cancelled the exchange
    COMPLETED           // The exchange is completed
}

data class Exchange(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val userLowId: String,
    val userHighId: String,
    val status: ExchangeStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)