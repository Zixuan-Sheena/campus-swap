package click.sheena.campusswap.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import click.sheena.campusswap.R

data class ConversationListItem(
    val conversationId: String,
    val participantName: String,
    val preview: String
)

class ConversationAdapter(
    private var conversations: List<ConversationListItem>,
    private val onConversationClick: (ConversationListItem) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>() {

    class ConversationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val participantName: TextView = view.findViewById(R.id.conversationParticipantName)
        val preview: TextView = view.findViewById(R.id.conversationPreview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.participantName.text = conversation.participantName
        holder.preview.text = conversation.preview
        holder.itemView.setOnClickListener { onConversationClick(conversation) }
    }

    override fun getItemCount(): Int = conversations.size

    fun updateItems(newConversations: List<ConversationListItem>) {
        conversations = newConversations
        notifyDataSetChanged()
    }
}
