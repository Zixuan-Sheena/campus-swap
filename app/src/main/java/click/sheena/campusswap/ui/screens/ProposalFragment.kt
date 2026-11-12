package click.sheena.campusswap.ui.screens

import android.os.Bundle
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import click.sheena.campusswap.R
import click.sheena.campusswap.data.ProposalRepository
import click.sheena.campusswap.data.UserRepository
import click.sheena.campusswap.data.UserSession
import click.sheena.campusswap.model.Proposal
import click.sheena.campusswap.model.ProposalItem
import click.sheena.campusswap.model.ProposalStatus
import click.sheena.campusswap.ui.ProposalImage
import click.sheena.campusswap.ui.showProposalImages
import click.sheena.campusswap.ui.proposalLabel
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

class ProposalFragment : Fragment(R.layout.fragment_proposal) {
    companion object {
        const val ARG_PROPOSAL_ID = "proposalId"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Toolbar>(R.id.proposalToolbar).setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val proposalId = arguments?.getString(ARG_PROPOSAL_ID) ?: return@launch
            val userId = UserSession.getCurrentUser()?.id ?: return@launch
            val proposal = ProposalRepository.getById(proposalId)
            if (proposal == null || userId !in listOf(proposal.proposerId, proposal.receiverId)) {
                findNavController().navigateUp()
                return@launch
            }

            val otherId = if (userId == proposal.proposerId) proposal.receiverId else proposal.proposerId
            val otherName = UserRepository.getUserById(otherId)?.name ?: getString(R.string.unknown_user)
            view.findViewById<TextView>(R.id.proposalOtherLabel).text =
                getString(R.string.proposal_other_gives, otherName)

            val items = ProposalRepository.getItems(proposalId)
            showItems(view.findViewById(R.id.proposalOtherItems), items.filter { it.ownerIdSnapshot == otherId })
            showItems(view.findViewById(R.id.proposalYourItems), items.filter { it.ownerIdSnapshot == userId })

            val date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date(proposal.meetingAt))
            view.findViewById<TextView>(R.id.proposalMeeting).text = getString(R.string.proposal_meeting, date)
            view.findViewById<TextView>(R.id.proposalLocation).text = getString(R.string.proposal_location, proposal.eircode)
            showActions(view, proposal, userId)
        }
    }

    private fun showItems(grid: GridLayout, items: List<ProposalItem>) {
        grid.showProposalImages(items.map { ProposalImage(it.itemId, it.titleSnapshot, it.imagePathSnapshot) }) { itemId ->
            findNavController().navigate(
                R.id.itemDetailFragment,
                Bundle().apply { putString(ItemDetailFragment.ARG_ITEM_ID, itemId) }
            )
        }
    }

    private fun showActions(view: View, proposal: Proposal, userId: String) {
        view.findViewById<TextView>(R.id.proposalStatus).text = requireContext().proposalLabel(proposal.status)
        val isPending = proposal.status == ProposalStatus.PENDING
        val canResend = proposal.status == ProposalStatus.REJECTED && userId == proposal.proposerId
        view.findViewById<View>(R.id.proposalActions).isVisible = isPending || canResend
        val isReceiver = userId == proposal.receiverId
        view.findViewById<MaterialButton>(R.id.proposalAccept).apply {
            isVisible = isPending && isReceiver
            setOnClickListener { showNotImplemented() }
        }
        view.findViewById<MaterialButton>(R.id.proposalEdit).apply {
            setText(when {
                canResend -> R.string.proposal_edit_resend
                isReceiver -> R.string.proposal_reject_edit
                else -> R.string.proposal_withdraw_edit
            })
            setOnClickListener {
                findNavController().navigate(
                    R.id.proposalEditFragment,
                    Bundle().apply { putString(ARG_PROPOSAL_ID, proposal.id) }
                )
            }
        }
        view.findViewById<MaterialButton>(R.id.proposalClose).apply {
            isVisible = isPending
            setText(if (isReceiver) R.string.proposal_reject else R.string.proposal_withdraw)
            setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    ProposalRepository.close(proposal.id, userId)
                    ProposalRepository.getById(proposal.id)?.let { showActions(view, it, userId) }
                }
            }
        }
    }

    private fun showNotImplemented() {
        Toast.makeText(requireContext(), R.string.proposal_not_implemented, Toast.LENGTH_SHORT).show()
    }
}
