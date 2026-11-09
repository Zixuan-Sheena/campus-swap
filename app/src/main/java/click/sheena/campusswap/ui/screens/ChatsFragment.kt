package click.sheena.campusswap.ui.screens

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import click.sheena.campusswap.R
import click.sheena.campusswap.data.ConversationRepository
import click.sheena.campusswap.data.MessageRepository
import click.sheena.campusswap.data.UserRepository
import click.sheena.campusswap.data.UserSession
import click.sheena.campusswap.ui.adapters.ConversationAdapter
import click.sheena.campusswap.ui.adapters.ConversationListItem

class ChatsFragment : Fragment(R.layout.fragment_chats) {

    private var conversationAdapter: ConversationAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = ConversationAdapter(emptyList()) { conversation ->
            findNavController().navigate(
                R.id.chatDetailFragment,
                Bundle().apply {
                    putString(ChatDetailFragment.ARG_CONVERSATION_ID, conversation.conversationId)
                }
            )
        }

        conversationAdapter = adapter
        view.findViewById<RecyclerView>(R.id.conversationsRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()

        val user = UserSession.getCurrentUser()
        val conversations = user?.let { currentUser ->
            ConversationRepository.getConversationsForUser(currentUser.id)
                .mapNotNull { conversation ->
                    val otherUserId = ConversationRepository.getOtherUserId(
                        conversation,
                        currentUser.id
                    ) ?: return@mapNotNull null

                    val participant = UserRepository.getUserById(otherUserId)
                    val latestMessage = MessageRepository.getLatestMessage(conversation.id)

                    ConversationListItem(
                        conversationId = conversation.id,
                        participantName = participant?.name ?: getString(R.string.unknown_user),
                        preview = latestMessage?.text ?: getString(R.string.chat_no_messages)
                    )
                }
                .sortedByDescending { item ->
                    MessageRepository.getLatestMessage(item.conversationId)?.createdAt ?: Long.MIN_VALUE
                }
        }.orEmpty()

        conversationAdapter?.updateItems(conversations)

        view?.findViewById<RecyclerView>(R.id.conversationsRecyclerView)?.isVisible =
            conversations.isNotEmpty()

        view?.findViewById<TextView>(R.id.emptyChatsText)?.apply {
            isVisible = conversations.isEmpty()
            text = if (user == null) {
                getString(R.string.chat_login_required)
            } else {
                getString(R.string.chat_empty)
            }
        }
    }

    override fun onDestroyView() {
        view?.findViewById<RecyclerView>(R.id.conversationsRecyclerView)?.adapter = null
        conversationAdapter = null
        super.onDestroyView()
    }
}
