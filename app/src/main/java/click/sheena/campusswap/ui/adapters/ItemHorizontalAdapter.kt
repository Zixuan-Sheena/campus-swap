package click.sheena.campusswap.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import click.sheena.campusswap.R
import click.sheena.campusswap.model.Item

class ItemHorizontalAdapter(
    private var items: List<Item>,
    private val onItemClick: (Item) -> Unit
) : RecyclerView.Adapter<ItemHorizontalAdapter.ItemViewHolder>() {

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView =
            view.findViewById(R.id.itemHorizontalImage)

        val title: TextView =
            view.findViewById(R.id.itemHorizontalTitle)

        val category: TextView =
            view.findViewById(R.id.itemHorizontalCategory)

        val description: TextView =
            view.findViewById(R.id.itemHorizontalDescription)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_card_horizontal, parent, false)

        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int
    ) {
        val item = items[position]

        holder.image.setImageResource(R.drawable.sample_cat)
        holder.title.text = item.title
        holder.category.text = item.category.name
        holder.description.text = item.description

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}