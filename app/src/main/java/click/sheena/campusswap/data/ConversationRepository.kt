package click.sheena.campusswap.data

import click.sheena.campusswap.model.Conversation

object ConversationRepository {
    suspend fun getConversationById(conversationId: String): Conversation? =
        CampusSwapDatabase.ready().conversations().getById(conversationId)

    suspend fun getConversationsForUser(userId: String): List<Conversation> =
        CampusSwapDatabase.ready().conversations().getForUser(userId)

    suspend fun getForUsers(firstUserId: String, secondUserId: String): Conversation? {
        val users = listOf(firstUserId, secondUserId).sorted()
        return CampusSwapDatabase.ready().conversations().getForPair(users[0], users[1])
    }

    fun getOtherUserId(conversation: Conversation, userId: String): String? {
        return when (userId) {
            conversation.userLowId -> conversation.userHighId
            conversation.userHighId -> conversation.userLowId
            else -> null
        }
    }
}
