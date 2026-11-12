package click.sheena.campusswap.ui.screens

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import click.sheena.campusswap.R
import click.sheena.campusswap.data.ItemRepository
import click.sheena.campusswap.ui.adapters.ProposalItemPickerAdapter
import kotlinx.coroutines.launch

class ProposalItemPickerFragment : Fragment(R.layout.fragment_proposal_item_picker) {
    companion object {
        const val ARG_OWNER_ID = "ownerId"
        const val ARG_SELECTED_IDS = "selectedIds"
        const val RESULT_KEY = "proposalItemSelection"
    }

    private val selectedIds = linkedSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedIds.addAll(arguments?.getStringArray(ARG_SELECTED_IDS).orEmpty())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ownerId = requireArguments().getString(ARG_OWNER_ID) ?: return
        val toolbar = view.findViewById<Toolbar>(R.id.proposalPickerToolbar)
        toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        toolbar.inflateMenu(R.menu.proposal_item_picker_menu)
        val confirm = toolbar.menu.findItem(R.id.confirmProposalItems)
        confirm.isEnabled = false
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.confirmProposalItems) {
                parentFragmentManager.setFragmentResult(RESULT_KEY, Bundle().apply {
                    putString(ARG_OWNER_ID, ownerId)
                    putStringArray(ARG_SELECTED_IDS, selectedIds.toTypedArray())
                })
                findNavController().popBackStack()
                true
            } else false
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val items = ItemRepository.getItemByOwnerId(ownerId).filter { it.isAvailable() }
            selectedIds.retainAll(items.map { it.id }.toSet())
            view.findViewById<RecyclerView>(R.id.proposalPickerList).apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = ProposalItemPickerAdapter(items, selectedIds,
                    onSelectionChanged = { showCount(view) },
                    onItemClick = { item ->
                        findNavController().navigate(
                            R.id.itemDetailFragment,
                            Bundle().apply { putString(ItemDetailFragment.ARG_ITEM_ID, item.id) }
                        )
                    }
                )
            }
            view.findViewById<View>(R.id.proposalPickerEmpty).isVisible = items.isEmpty()
            showCount(view)
            confirm.isEnabled = true
        }
    }

    private fun showCount(view: View) {
        view.findViewById<TextView>(R.id.proposalSelectionCount).text =
            getString(R.string.proposal_selection_count, selectedIds.size)
    }

    override fun onDestroyView() {
        view?.findViewById<RecyclerView>(R.id.proposalPickerList)?.adapter = null
        super.onDestroyView()
    }
}
