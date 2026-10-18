package click.sheena.campusswap.ui.screens

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import click.sheena.campusswap.R
import click.sheena.campusswap.data.SampleItems
import click.sheena.campusswap.ui.adapters.ItemAdapter

class BrowseFragment : Fragment(R.layout.fragment_browse) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView =
            view.findViewById<RecyclerView>(R.id.itemsRecyclerView)

        recyclerView.layoutManager =
            StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )

        recyclerView.adapter = ItemAdapter(SampleItems.items)
    }
}