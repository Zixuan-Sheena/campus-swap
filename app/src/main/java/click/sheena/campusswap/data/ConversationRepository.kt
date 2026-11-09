package click.sheena.campusswap.data

import click.sheena.campusswap.model.Conversation

object ConversationRepository {
    private val conversations = SampleData.conversations.map { it.copy() }.toMutableList()

    fun getConversationById(conversationId: String): Conversation? {
        return conversations.find { it.id == conversationId }?.copy()
    }

    fun getConversationsForUser(userId: String): List<Conversation> {
        return conversations
            .filter { it.userLowId == userId || it.userHighId == userId }
            .map { it.copy() }
    }

    fun getOtherUserId(conversation: Conversation, userId: String): String? {
        return when (userId) {
            conversation.userLowId -> conversation.userHighId
            conversation.userHighId -> conversation.userLowId
            else -> null
        }
    }
}
