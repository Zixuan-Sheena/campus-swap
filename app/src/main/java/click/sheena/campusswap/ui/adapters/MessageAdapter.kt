package click.sheena.campusswap.ui.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import click.sheena.campusswap.R
import click.sheena.campusswap.data.ItemImageStorage
import click.sheena.campusswap.model.Message
import click.sheena.campusswap.model.MessageType
import click.sheena.campusswap.model.ProposalStatus
import click.sheena.campusswap.ui.proposalLabel

class MessageAdapter(
    private var messages: List<Message>,
    private val currentUserId: String,
    private val onProposalClick: (String) -> Unit
) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var proposalStatuses: Map<String, ProposalStatus> = emptyMap()

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bubble: View = view.findViewById(R.id.messageBubble)
        val text: TextView = view.findViewById(R.id.messageText)
        val image: ImageView = view.findViewById(R.id.messageImage)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)

        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: MessageViewHolder,
        position: Int
    ) {
        val message = messages[position]
        val isOwnMessage = message.senderId == currentUserId

        val layoutParams =
            holder.bubble.layoutParams as FrameLayout.LayoutParams

        layoutParams.gravity =
            if (isOwnMessage) Gravity.END else Gravity.START

        holder.bubble.layoutParams = layoutParams
        holder.bubble.isSelected = isOwnMessage

        holder.bubble.setOnClickListener(null)
        holder.bubble.isClickable = false

        when (message.type) {
            MessageType.TEXT -> {
                holder.text.isVisible = true
                holder.image.isVisible = false
                holder.text.text = message.text.orEmpty()
            }

            MessageType.IMAGE -> {
                val bitmap = ItemImageStorage.readImage(
                    message.imagePath.orEmpty()
                )

                holder.text.isVisible = bitmap == null
                holder.image.isVisible = bitmap != null

                if (bitmap == null) {
                    holder.text.setText(R.string.chat_image_unavailable)
                } else {
                    holder.image.setImageBitmap(bitmap)
                }
            }

            MessageType.PROPOSAL -> {
                holder.text.isVisible = true
                holder.image.isVisible = false
                val status = proposalStatuses[message.proposalId]
                holder.text.text = status?.let { holder.itemView.context.proposalLabel(it) }
                    ?: holder.itemView.context.getString(R.string.chat_proposal_message)
                message.proposalId?.let { proposalId ->
                    holder.bubble.setOnClickListener { onProposalClick(proposalId) }
                }
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    fun updateItems(newMessages: List<Message>, statuses: Map<String, ProposalStatus>) {
        proposalStatuses = statuses
        messages = newMessages
        notifyDataSetChanged()
    }
}