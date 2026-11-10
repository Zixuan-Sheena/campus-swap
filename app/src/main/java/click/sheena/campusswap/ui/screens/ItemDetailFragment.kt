package click.sheena.campusswap.ui.screens

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController
import click.sheena.campusswap.R
import click.sheena.campusswap.data.FavouriteRepository
import click.sheena.campusswap.data.ItemRepository
import click.sheena.campusswap.model.Item
import click.sheena.campusswap.data.UserSession
import click.sheena.campusswap.data.UserRepository
import java.text.DateFormat
import java.util.Date
import androidx.navigation.ui.setupWithNavController
import click.sheena.campusswap.ui.NavigationConfig
import click.sheena.campusswap.ui.displayImagePath
import click.sheena.campusswap.ui.loadItemImage

class ItemDetailFragment : Fragment(R.layout.fragment_item_detail) {

    companion object {
        const val ARG_ITEM_ID = "itemId"

    }

    private var loadJob: Job? = null
    private var actionJob: Job? = null
    private var item: Item? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        item = null
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
        val ownerText = view.findViewById<TextView>(R.id.detailOwner)
        val updatedAtText = view.findViewById<TextView>(R.id.detailUpdateAt)
        val owner = UserRepository.getUserById(item.ownerId)
        val ownerName = owner?.name ?: getString(R.string.unknown_user)
        val dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.MEDIUM,
            DateFormat.SHORT
        )
        val uploadedAt = dateFormat.format(Date(item.createdAt))

        image.loadItemImage(item.displayImagePath())

        title.text = item.title
        category.text = item.category.name
        wanted.text = item.wantedDescription
        description.text = item.description
        ownerText.text = getString(R.string.item_owner, ownerName)
        updatedAtText.text = getString(R.string.item_created_at, uploadedAt)
    }

    private fun setupToolbar(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.detailToolbar)

        toolbar.inflateMenu(R.menu.item_detail_menu)

        toolbar.setupWithNavController(
            findNavController(),
            NavigationConfig.appBarConfiguration
        )

        toolbar.menu.setGroupVisible(0, false)

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_favourite -> {
                    toggleFavourite(toolbar.menu)
                    true
                }

                R.id.action_edit_item -> {
                    item?.let { currentItem ->
                        findNavController().navigate(
                            R.id.listItemFragment,
                            Bundle().apply {
                                putString(
                                    ListItemFragment.ARG_ITEM_ID,
                                    currentItem.id
                                )
                            }
                        )
                    }
                    true
                }

                R.id.action_withdraw_item -> {
                    withdrawItem()
                    true
                }

                else -> false
            }
        }
    }

    private suspend fun updateToolbarActions(menu: Menu) {
        val currentItem = item ?: return
        val currentUser = UserSession.getCurrentUser()

        val favouriteAction = menu.findItem(R.id.action_favourite)
        val editAction = menu.findItem(R.id.action_edit_item)
        val withdrawAction = menu.findItem(R.id.action_withdraw_item)

        val isOwner = currentUser?.id == currentItem.ownerId

        if (currentUser != null && !isOwner) {
            val isFavourite = FavouriteRepository.isFavourite(currentUser.id, currentItem.id)
            updateFavouriteIcon(menu, isFavourite)
        }

        favouriteAction.isVisible = currentUser != null && !isOwner
        val canEdit = isOwner && currentItem.isAvailable()
        editAction.isVisible = canEdit
        withdrawAction.isVisible = canEdit
    }

    private fun toggleFavourite(menu: Menu) {
        val currentItem = item ?: return
        val currentUserId = UserSession.getCurrentUser()?.id ?: return
        if (actionJob?.isActive == true) return

        actionJob = viewLifecycleOwner.lifecycleScope.launch {
            val isFavourite = FavouriteRepository.toggleFavourite(currentUserId, currentItem.id)
            updateFavouriteIcon(menu, isFavourite)
            val messageRes = if (isFavourite) {
                R.string.item_added_to_favourites
            } else {
                R.string.item_removed_from_favourites
            }
            Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFavouriteIcon(menu: Menu, isFavourite: Boolean) {
        val favouriteItem = menu.findItem(R.id.action_favourite)

        if (isFavourite) {
            favouriteItem.setIcon(R.drawable.ic_favorite)
            favouriteItem.setTitle(R.string.remove_favourite)
        } else {
            favouriteItem.setIcon(R.drawable.ic_favorite_border)
            favouriteItem.setTitle(R.string.add_favourite)
        }
    }

    private fun withdrawItem() {
        val currentItem = item ?: return
        val currentUserId = UserSession.getCurrentUser()?.id ?: return

        if (actionJob?.isActive == true) return

        actionJob = viewLifecycleOwner.lifecycleScope.launch {
            val updatedItem = ItemRepository.withdrawItem(
                itemId = currentItem.id,
                userId = currentUserId
            )

            if (updatedItem == null) {
                Toast.makeText(
                    requireContext(),
                    R.string.item_withdraw_failed,
                    Toast.LENGTH_SHORT
                ).show()
                return@launch
            }

            item = updatedItem

            Toast.makeText(
                requireContext(),
                R.string.item_withdrawn,
                Toast.LENGTH_SHORT
            ).show()

            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()

        val root = view ?: return
        val itemId = arguments?.getString(ARG_ITEM_ID)
        if (itemId == null) {
            findNavController().navigateUp()
            return
        }
        loadJob?.cancel()
        loadJob = viewLifecycleOwner.lifecycleScope.launch {
            val latestItem = ItemRepository.getItemById(itemId)

            if (latestItem == null) {
                findNavController().popBackStack()
                return@launch
            }

            item = latestItem
            bindItem(root, latestItem)

            val toolbar = root.findViewById<Toolbar>(
                R.id.detailToolbar
            )
            updateToolbarActions(toolbar.menu)
        }
    }
}
