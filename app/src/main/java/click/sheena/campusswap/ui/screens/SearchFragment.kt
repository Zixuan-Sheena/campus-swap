package click.sheena.campusswap.ui.screens

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import click.sheena.campusswap.R
import click.sheena.campusswap.data.ItemRepository
import click.sheena.campusswap.model.ItemCategory
import click.sheena.campusswap.ui.adapters.ItemHorizontalAdapter

class SearchFragment : Fragment(R.layout.fragment_search) {
    private var searchKeyword = ""
    private var selectedCategory: ItemCategory? = null
    private var hasSearched = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val searchInput = view.findViewById<EditText>(R.id.searchInput)
        val categoryButton = view.findViewById<Button>(R.id.categoryButton)
        val searchButton = view.findViewById<Button>(R.id.searchButton)
        val recyclerView =
            view.findViewById<RecyclerView>(R.id.searchRecyclerView)

        val itemAdapter = ItemHorizontalAdapter(emptyList()) { item ->
            findNavController().navigate(
                R.id.itemDetailFragment,
                Bundle().apply {
                    putString(ItemDetailFragment.ARG_ITEM_ID, item.id)
                }
            )
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = itemAdapter

        val emptyText = view.findViewById<TextView>(R.id.emptyText)
        fun showItems(items: List<click.sheena.campusswap.model.Item>) {
            itemAdapter.updateItems(items)
            emptyText.isVisible = items.isEmpty()
            recyclerView.isVisible = items.isNotEmpty()
        }
        showItems(emptyList())

        fun performSearch() {
            searchKeyword = searchInput.text.toString().trim()
            hasSearched = searchKeyword.isNotBlank()

            if (!hasSearched) {
                showItems(emptyList())
                return
            }

            val results = ItemRepository.searchItems(
                category = selectedCategory,
                keyword = searchKeyword
            )

            showItems(results)

            val message = if (results.size == 1) {
                "1 result"
            } else {
                "${results.size} results"
            }

            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        val categories: List<ItemCategory?> =
            listOf(null) + ItemCategory.entries
        val labels = categories.map { it?.name ?: "All" }.toTypedArray()

        categoryButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Select category")
                .setSingleChoiceItems(
                    labels,
                    categories.indexOf(selectedCategory)
                ) { dialog, position ->
                    selectedCategory = categories[position]
                    categoryButton.text = labels[position]
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        searchButton.setOnClickListener {
            performSearch()
        }

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        searchInput.setText(searchKeyword)
        categoryButton.text = selectedCategory?.name
            ?: getString(R.string.search_category)

        if (hasSearched) {
            showItems(
                ItemRepository.searchItems(
                    category = selectedCategory,
                    keyword = searchKeyword
                )
            )
        }
    }
}