package click.sheena.campusswap.model

data class Proposal(
    val id: Long = 0,
    val exchangeId: Long,
    val version: Int,
    val proposerId: Long,
    val status: ProposalStatus = ProposalStatus.PENDING,
    val meetingAt: Long,
    val eircode: String,
    val createdAt: Long
)
