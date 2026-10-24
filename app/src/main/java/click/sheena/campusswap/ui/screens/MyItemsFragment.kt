package click.sheena.campusswap.ui.screens

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import click.sheena.campusswap.R
import click.sheena.campusswap.data.ItemRepository
import click.sheena.campusswap.data.UserSession
import click.sheena.campusswap.ui.NavigationConfig
import click.sheena.campusswap.ui.adapters.ItemHorizontalAdapter

class MyItemsFragment : Fragment(R.layout.fragment_my_items) {

    private var itemAdapter: ItemHorizontalAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Toolbar>(R.id.myItemsToolbar)
            .setupWithNavController(
                findNavController(),
                NavigationConfig.appBarConfiguration
            )

        val adapter = ItemHorizontalAdapter(emptyList()) { item ->
            findNavController().navigate(
                R.id.itemDetailFragment,
                Bundle().apply {
                    putString(ItemDetailFragment.ARG_ITEM_ID, item.id)
                }
            )
        }

        itemAdapter = adapter

        view.findViewById<RecyclerView>(R.id.myItemsRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()

        val user = UserSession.getCurrentUser()

        val items = if (user == null) {
            emptyList()
        } else {
            ItemRepository.getItemByOwnerId(user.id)
                .filter { it.isAvailable() }
        }

        itemAdapter?.updateItems(items)

        view?.findViewById<TextView>(R.id.emptyText)?.isVisible =
            items.isEmpty()

        view?.findViewById<RecyclerView>(R.id.myItemsRecyclerView)?.isVisible =
            items.isNotEmpty()
    }

    override fun onDestroyView() {
        view?.findViewById<RecyclerView>(R.id.myItemsRecyclerView)
            ?.adapter = null
        itemAdapter = null
        super.onDestroyView()
    }
}