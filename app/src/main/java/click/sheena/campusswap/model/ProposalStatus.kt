package click.sheena.campusswap.model

enum class ProposalStatus {
    PENDING,        // Proposal has been sent but not yet responded to
    SUPERSEDED,     // Proposal has been superseded by a newer version
    REJECTED,       // Proposal has been rejected
    INVALIDATED,    // Proposal has been invalidated due to changes in the item or exchange
    ACCEPTED,       // Proposal has been accepted
    CANCELLED,      // Proposal has been cancelled
    COMPLETED       // Proposal has been completed
}