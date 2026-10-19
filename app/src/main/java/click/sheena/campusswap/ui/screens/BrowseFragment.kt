package click.sheena.campusswap.ui.screens

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import click.sheena.campusswap.R
import click.sheena.campusswap.data.SampleItems
import click.sheena.campusswap.ui.adapters.ItemAdapter
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import android.widget.TextView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import click.sheena.campusswap.model.ItemCategory

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

        val itemAdapter = ItemAdapter(SampleItems.items)
        recyclerView.adapter = itemAdapter

        val chipGroup =
            view.findViewById<ChipGroup>(R.id.categoryChipGroup)

        val categories: List<ItemCategory?> =
            listOf(null) + ItemCategory.entries

        for (category in categories) {
            val chip = Chip(requireContext())

            chip.id = View.generateViewId()
            chip.text = category?.name ?: "All"
            chip.isCheckable = true

            chipGroup.addView(chip)

            // Default to "All" being selected
            if (category == null) {
                chip.isChecked = true
            }

            chip.setOnClickListener {
                val filteredItems = if (category == null) {
                    SampleItems.items
                } else {
                    SampleItems.items.filter {
                        it.category == category
                    }
                }

                itemAdapter.updateItems(filteredItems)
            }
        }
    }
}