package click.sheena.campusswap.data

import androidx.room.withTransaction
import click.sheena.campusswap.model.Proposal
import click.sheena.campusswap.model.ProposalItem
import click.sheena.campusswap.model.ProposalStatus
import click.sheena.campusswap.model.Conversation
import click.sheena.campusswap.model.Item
import click.sheena.campusswap.model.Message
import click.sheena.campusswap.model.MessageType

object ProposalRepository {
    suspend fun getById(proposalId: String): Proposal? =
        CampusSwapDatabase.ready().proposals().getById(proposalId)

    suspend fun getForConversation(conversationId: String): List<Proposal> =
        CampusSwapDatabase.ready().proposals().getForConversation(conversationId)

    suspend fun getItems(proposalId: String): List<ProposalItem> =
        CampusSwapDatabase.ready().proposalItems().getForProposal(proposalId)

    suspend fun getPending(conversationId: String): Proposal? =
        CampusSwapDatabase.ready().proposals().getPending(conversationId)

    suspend fun send(
        senderId: String,
        receiverId: String,
        ownItems: List<Item>,
        otherItems: List<Item>,
        meetingAt: Long,
        eircode: String,
        previousProposalId: String? = null
    ): Proposal = send(
        CampusSwapDatabase.ready(), senderId, receiverId, ownItems, otherItems,
        meetingAt, eircode, previousProposalId
    )

    internal suspend fun send(
        database: CampusSwapDatabase,
        senderId: String,
        receiverId: String,
        ownItems: List<Item>,
        otherItems: List<Item>,
        meetingAt: Long,
        eircode: String,
        previousProposalId: String? = null
    ): Proposal {
        require(senderId != receiverId && UserRepository.getUserById(senderId) != null &&
            UserRepository.getUserById(receiverId) != null) { "Select another user to exchange with." }
        require(ownItems.size in 1..8 && otherItems.size in 1..8) { "Select 1 to 8 items from each person." }
        require(meetingAt > System.currentTimeMillis()) { "Choose a future date and time." }
        require(eircode.isNotBlank()) { "Enter a location (Eircode)." }

        return database.withTransaction {
            val selectedItems = ownItems + otherItems
            val ids = selectedItems.map { it.id }
            require(ids.distinct().size == ids.size && ownItems.all { it.ownerId == senderId } &&
                otherItems.all { it.ownerId == receiverId }) { "Check the selected items and their owners." }
            val currentItems = database.items().getByIds(ids)
            require(currentItems.size == ids.size && currentItems.all { current ->
                val selected = selectedItems.first { it.id == current.id }
                current.isAvailable() && current.ownerId == selected.ownerId && current.revision == selected.revision
            }) { "Some items have changed or are unavailable. Please select them again." }

            val users = listOf(senderId, receiverId).sorted()
            val existing = database.conversations().getForPair(users[0], users[1])
            val conversation = existing ?: Conversation(userLowId = users[0], userHighId = users[1])
            require(database.exchanges().getAwaitingHandover(conversation.id) == null) {
                "Complete or cancel the current handover before sending another proposal."
            }
            val pending = database.proposals().getPending(conversation.id)
            if (previousProposalId != null) {
                val previous = database.proposals().getById(previousProposalId)
                require(previous != null && previous.conversationId == conversation.id &&
                    database.proposals().getLatest(conversation.id)?.id == previous.id &&
                    (previous.status == ProposalStatus.PENDING ||
                        previous.status == ProposalStatus.REJECTED && previous.proposerId == senderId)) {
                    "This proposal has changed. Open the latest proposal in the chat."
                }
            } else {
                require(pending == null) { "Open the pending proposal to edit and resend it." }
            }

            val now = System.currentTimeMillis()
            val proposal = Proposal(
                conversationId = conversation.id,
                version = database.proposals().getNextVersion(conversation.id),
                proposerId = senderId,
                receiverId = receiverId,
                meetingAt = meetingAt,
                eircode = eircode.trim(),
                createdAt = now
            )
            if (existing == null) database.conversations().insert(conversation)
            if (pending != null) database.proposals().updateStatus(pending.id, ProposalStatus.SUPERSEDED)
            database.proposals().insert(proposal)
            database.proposalItems().insertAll(currentItems.map { item ->
                ProposalItem(
                    proposalId = proposal.id,
                    itemId = item.id,
                    ownerIdSnapshot = item.ownerId,
                    itemRevision = item.revision,
                    titleSnapshot = item.title,
                    descriptionSnapshot = item.description,
                    categorySnapshot = item.category.name,
                    imagePathSnapshot = item.imagePath
                )
            })
            database.messages().insert(Message(
                conversationId = conversation.id,
                senderId = senderId,
                type = MessageType.PROPOSAL,
                proposalId = proposal.id,
                createdAt = now
            ))
            proposal
        }
    }

    suspend fun close(proposalId: String, userId: String) {
        val database = CampusSwapDatabase.ready()
        database.withTransaction {
            val proposal = database.proposals().getById(proposalId) ?: return@withTransaction
            if (proposal.status != ProposalStatus.PENDING) return@withTransaction
            val status = when (userId) {
                proposal.proposerId -> ProposalStatus.WITHDRAWN
                proposal.receiverId -> ProposalStatus.REJECTED
                else -> return@withTransaction
            }
            database.proposals().updateStatus(proposalId, status)
        }
    }
}
