package click.sheena.campusswap.model

import androidx.room.Entity

@Entity(tableName = "proposal_items", primaryKeys = ["proposalId", "itemId"])
data class ProposalItem(
    val proposalId: String,
    val itemId: String,
    val ownerIdSnapshot: String,
    val itemRevision: Int,
    val titleSnapshot: String,
    val descriptionSnapshot: String,
    val categorySnapshot: String,
    val imagePathSnapshot: String
)
