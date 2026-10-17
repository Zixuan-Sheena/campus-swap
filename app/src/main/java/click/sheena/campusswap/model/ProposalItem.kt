package click.sheena.campusswap.model

data class ProposalItem (
    val  proposalId: Long,
    val  itemId: Long,
    val ownerIdSnapshot: Long,
    val itemRevision: Int,
    val titleSnapshot: String,
    val descriptionSnapshot: String,
    val categorySnapshot: String,
    val imagePathSnapshot: String,

)