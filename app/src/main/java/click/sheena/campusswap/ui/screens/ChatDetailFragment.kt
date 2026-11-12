package click.sheena.campusswap.ui.screens

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import click.sheena.campusswap.R
import click.sheena.campusswap.data.ConversationRepository
import click.sheena.campusswap.data.ItemImageStorage
import click.sheena.campusswap.data.MessageRepository
import click.sheena.campusswap.data.ProposalRepository
import click.sheena.campusswap.data.UserRepository
import click.sheena.campusswap.data.UserSession
import click.sheena.campusswap.ui.adapters.MessageAdapter
import com.google.android.material.button.MaterialButton

class ChatDetailFragment : Fragment(R.layout.fragment_chat_detail) {

    companion object {
        const val ARG_CONVERSATION_ID = "conversationId"
    }

    private var extensionsVisible = false
    private var messageAdapter: MessageAdapter? = null
    private var conversationId: String? = null

    private val imagePicker = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            sendSelectedImage(uri)
        }
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val user = UserSession.getCurrentUser()

            val loadedConversationId =
                arguments?.getString(ARG_CONVERSATION_ID)
                    ?: run {
                        findNavController().navigateUp()
                        return@launch
                    }

            val conversation =
                ConversationRepository.getConversationById(
                    loadedConversationId
                )

            if (
                user == null ||
                conversation == null ||
                ConversationRepository.getOtherUserId(
                    conversation,
                    user.id
                ) == null
            ) {
                findNavController().navigateUp()
                return@launch
            }

            conversationId = loadedConversationId

            val otherUserId = ConversationRepository.getOtherUserId(
                conversation,
                user.id
            )!!

            val otherUser = UserRepository.getUserById(otherUserId)

            val toolbar =
                view.findViewById<Toolbar>(R.id.chatDetailToolbar)

            toolbar.title =
                otherUser?.name ?: getString(R.string.unknown_user)

            toolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            setupMessages(view, user.id)
            setupExtensions(view)
            setupTextMessageSending(view, loadedConversationId, user.id)
            setupImageSending(view)

            view.findViewById<MaterialButton>(R.id.createProposalButton)
                .setOnClickListener {
                    findNavController().navigate(
                        R.id.proposalEditFragment,
                        Bundle().apply {
                            putString(ProposalEditFragment.ARG_CONVERSATION_ID, loadedConversationId)
                        }
                    )
                }

            refreshMessages()
        }
    }

    private fun setupMessages(
        view: View,
        currentUserId: String
    ) {
        val recyclerView =
            view.findViewById<RecyclerView>(R.id.messagesRecyclerView)

        recyclerView.layoutManager =
            LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }

        messageAdapter = MessageAdapter(
            messages = emptyList(),
            currentUserId = currentUserId,
            onProposalClick = { proposalId ->
                findNavController().navigate(
                    R.id.proposalFragment,
                    Bundle().apply { putString(ProposalFragment.ARG_PROPOSAL_ID, proposalId) }
                )
            }
        )

        recyclerView.adapter = messageAdapter
    }

    private fun setupExtensions(view: View) {
        val extensionsPanel =
            view.findViewById<View>(R.id.chatExtensionsPanel)

        val moreButton =
            view.findViewById<ImageButton>(R.id.chatMoreButton)

        moreButton.setOnClickListener {
            extensionsVisible = !extensionsVisible

            extensionsPanel.isVisible = extensionsVisible

            moreButton.setImageResource(
                if (extensionsVisible) {
                    R.drawable.ic_close
                } else {
                    R.drawable.ic_add
                }
            )

            moreButton.contentDescription = getString(
                if (extensionsVisible) {
                    R.string.chat_close_actions
                } else {
                    R.string.chat_more_actions
                }
            )
        }
    }

    private fun setupTextMessageSending(
        view: View,
        loadedConversationId: String,
        currentUserId: String
    ) {
        view.findViewById<MaterialButton>(R.id.sendMessageButton)
            .setOnClickListener {
                val input =
                    view.findViewById<EditText>(R.id.messageInput)

                val text = input.text.toString()
                viewLifecycleOwner.lifecycleScope.launch {
                    val sentMessage = MessageRepository.sendTextMessage(
                        conversationId = loadedConversationId,
                        senderId = currentUserId,
                        text = text
                    )

                    if (sentMessage != null) {
                        input.text?.clear()
                        refreshMessages()
                    }
                }
            }
    }

    private fun setupImageSending(view: View) {
        view.findViewById<MaterialButton>(R.id.sendImageButton)
            .setOnClickListener {
                imagePicker.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
    }

    private fun sendSelectedImage(uri: Uri) {
        val currentConversationId = conversationId ?: return
        val currentUserId = UserSession.getCurrentUser()?.id ?: return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val imagePath = ItemImageStorage.copyImage(
                    requireContext(),
                    uri
                )

                MessageRepository.sendImageMessage(
                    conversationId = currentConversationId,
                    senderId = currentUserId,
                    imagePath = imagePath
                )

                refreshMessages()
            } catch (_: Exception) {
                Toast.makeText(
                    requireContext(),
                    R.string.chat_image_send_failed,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun refreshMessages() {
        val currentConversationId = conversationId ?: return

        val messages = MessageRepository.getMessages(
            currentConversationId
        )

        val statuses = ProposalRepository.getForConversation(currentConversationId)
            .associate { it.id to it.status }
        messageAdapter?.updateItems(messages, statuses)

        view?.findViewById<TextView>(R.id.emptyMessagesText)
            ?.isVisible = messages.isEmpty()

        view?.findViewById<RecyclerView>(R.id.messagesRecyclerView)
            ?.apply {
                isVisible = messages.isNotEmpty()

                if (messages.isNotEmpty()) {
                    scrollToPosition(messages.lastIndex)
                }
            }
    }

    override fun onDestroyView() {
        view?.findViewById<RecyclerView>(R.id.messagesRecyclerView)
            ?.adapter = null

        messageAdapter = null

        super.onDestroyView()
    }
}