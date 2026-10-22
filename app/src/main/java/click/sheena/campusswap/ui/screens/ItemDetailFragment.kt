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
import click.sheena.campusswap.data.UserSession

class ItemDetailFragment : Fragment(R.layout.fragment_item_detail) {

    companion object {
        const val ARG_ITEM_ID = "itemId"

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

        updateToolbarActions(toolbar.menu)

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_favourite -> {
                    toggleFavourite()
                    updateToolbarActions(toolbar.menu)
                    true
                }

                R.id.action_edit_item -> {
                    true
                }

                R.id.action_withdraw_item -> {
                    true
                }

                else -> false
            }
        }
    }
    private fun updateToolbarActions(menu: Menu) {
        val currentItem = item ?: return
        val currentUser = UserSession.getCurrentUser()

        val favouriteAction = menu.findItem(R.id.action_favourite)
        val editAction = menu.findItem(R.id.action_edit_item)
        val withdrawAction = menu.findItem(R.id.action_withdraw_item)

        val isOwner = currentUser?.id == currentItem.ownerId

        favouriteAction.isVisible = currentUser != null && !isOwner
        editAction.isVisible = isOwner
        withdrawAction.isVisible = isOwner

        if (currentUser != null && !isOwner) {
            updateFavouriteIcon(menu, currentUser.id)
        }
    }

    private fun toggleFavourite() {
        val currentItem = item ?: return
        val currentUserId = UserSession.getCurrentUser()?.id ?: return

        if (FavouriteRepository.isFavourite(currentUserId, currentItem.id)) {
            FavouriteRepository.removeFavourite(currentUserId, currentItem.id)
        } else {
            FavouriteRepository.addFavourite(currentUserId, currentItem.id)
        }
    }

    private fun updateFavouriteIcon(
        menu: Menu,
        currentUserId: String
    ) {
        val currentItem = item ?: return
        val favouriteItem = menu.findItem(R.id.action_favourite)

        val isFavourite = FavouriteRepository.isFavourite(
            currentUserId,
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