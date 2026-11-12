package click.sheena.campusswap.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import click.sheena.campusswap.R
import click.sheena.campusswap.model.Item
import click.sheena.campusswap.ui.displayImagePath
import click.sheena.campusswap.ui.loadItemImage

class ProposalItemPickerAdapter(
    private val items: List<Item>,
    private val selectedIds: MutableSet<String>,
    private val onSelectionChanged: () -> Unit,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ProposalItemPickerAdapter.ItemViewHolder>() {
    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: View = view.findViewById(R.id.proposalPickerCard)
        val image: ImageView = view.findViewById(R.id.itemHorizontalImage)
        val title: TextView = view.findViewById(R.id.itemHorizontalTitle)
        val category: TextView = view.findViewById(R.id.itemHorizontalCategory)
        val description: TextView = view.findViewById(R.id.itemHorizontalDescription)
        val checkbox: CheckBox = view.findViewById(R.id.proposalItemSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_proposal_picker, parent, false))

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.image.loadItemImage(item.displayImagePath())
        holder.title.text = item.title
        holder.category.text = item.category.name
        holder.description.text = item.description
        holder.card.setOnClickListener { onItemClick(item) }
        holder.checkbox.apply {
            isChecked = item.id in selectedIds
            contentDescription = context.getString(R.string.proposal_select_item, item.title)
            setOnClickListener {
                if (isChecked && selectedIds.size >= 8) {
                    isChecked = false
                    Toast.makeText(context, R.string.proposal_selection_limit, Toast.LENGTH_SHORT).show()
                } else {
                    if (isChecked) selectedIds.add(item.id) else selectedIds.remove(item.id)
                    onSelectionChanged()
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
