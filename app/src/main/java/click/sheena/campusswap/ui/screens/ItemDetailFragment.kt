package click.sheena.campusswap.ui.screens

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import click.sheena.campusswap.R
import click.sheena.campusswap.data.FavouriteRepository
import click.sheena.campusswap.data.ItemRepository
import click.sheena.campusswap.model.Item

class ItemDetailFragment : Fragment(R.layout.fragment_item_detail) {

    companion object {
        const val ARG_ITEM_ID = "itemId"

        // 临时演示身份；之后应改为从用户状态仓库读取。
        const val CURRENT_USER_ID = "114514"
    }

    private var item: Item? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val itemId = arguments?.getString(ARG_ITEM_ID)

        if (itemId == null) {
            findNavController().navigateUp()
            return
        }

        val loadedItem = ItemRepository.getItemById(itemId)

        if (loadedItem == null) {
            findNavController().navigateUp()
            return
        }

        item = loadedItem

        bindItem(view, loadedItem)
        setupToolbar(view)
    }

    private fun bindItem(
        view: View,
        item: Item
    ) {
        val image = view.findViewById<ImageView>(R.id.detailImage)
        val title = view.findViewById<TextView>(R.id.detailTitle)
        val category = view.findViewById<TextView>(R.id.detailCategory)
        val wanted = view.findViewById<TextView>(R.id.detailWanted)
        val description = view.findViewById<TextView>(R.id.detailDescription)

        // 当前 ItemCard 也统一显示示例图片。
        image.setImageResource(R.drawable.sample_cat)

        title.text = item.title
        category.text = item.category.name
        wanted.text = item.wantedDescription
        description.text = item.description
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.detailToolbar)

        toolbar.inflateMenu(R.menu.item_detail_menu)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        updateFavouriteIcon(toolbar.menu)

        toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_favourite) {
                toggleFavourite()
                updateFavouriteIcon(toolbar.menu)
                true
            } else {
                false
            }
        }
    }

    private fun toggleFavourite() {
        val currentItem = item ?: return

        if (
            FavouriteRepository.isFavourite(
                CURRENT_USER_ID,
                currentItem.id
            )
        ) {
            FavouriteRepository.removeFavourite(
                CURRENT_USER_ID,
                currentItem.id
            )
        } else {
            FavouriteRepository.addFavourite(
                CURRENT_USER_ID,
                currentItem.id
            )
        }
    }

    private fun updateFavouriteIcon(menu: Menu) {
        val currentItem = item ?: return
        val favouriteItem = menu.findItem(R.id.action_favourite)

        val isFavourite = FavouriteRepository.isFavourite(
            CURRENT_USER_ID,
            currentItem.id
        )

        if (isFavourite) {
            favouriteItem.setIcon(R.drawable.ic_favorite)
            favouriteItem.setTitle(R.string.remove_favourite)
        } else {
            favouriteItem.setIcon(R.drawable.ic_favorite_border)
            favouriteItem.setTitle(R.string.add_favourite)
        }
    }
}