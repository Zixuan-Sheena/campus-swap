package click.sheena.campusswap.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import click.sheena.campusswap.R
import click.sheena.campusswap.data.ItemRepository
import click.sheena.campusswap.data.ConversationRepository
import click.sheena.campusswap.data.ProposalRepository
import click.sheena.campusswap.data.UserRepository
import click.sheena.campusswap.data.UserSession
import click.sheena.campusswap.model.Item
import click.sheena.campusswap.ui.ProposalImage
import click.sheena.campusswap.ui.displayImagePath
import click.sheena.campusswap.ui.showProposalImages
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

class ProposalEditFragment : Fragment(R.layout.fragment_proposal) {
    companion object {
        const val ARG_CONVERSATION_ID = "conversationId"
        const val ARG_OTHER_USER_ID = "otherUserId"
        const val ARG_ITEM_ID = "itemId"
    }

    private var previousProposalId: String? = null
    private var sending = false
    private var initialized = false
    private var userId = ""
    private var otherUserId = ""
    private var ownItems = emptyList<Item>()
    private var otherItems = emptyList<Item>()
    private var meetingAt = 0L
    private var eircode = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Toolbar>(R.id.proposalToolbar).apply {
            setTitle(R.string.proposal_edit_title)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }
        view.findViewById<View>(R.id.proposalStatus).isVisible = false
        view.findViewById<View>(R.id.proposalLocation).isVisible = false

        parentFragmentManager.setFragmentResultListener(
            ProposalItemPickerFragment.RESULT_KEY, viewLifecycleOwner
        ) { _, result ->
            val ownerId = result.getString(ProposalItemPickerFragment.ARG_OWNER_ID)
            val ids = result.getStringArray(ProposalItemPickerFragment.ARG_SELECTED_IDS).orEmpty().toList()
            viewLifecycleOwner.lifecycleScope.launch {
                val items = ItemRepository.getItemsByIds(ids)
                if (ownerId == userId) ownItems = items else otherItems = items
                showItems(view)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            if (!initialized && !loadInitialValues()) {
                findNavController().navigateUp()
                return@launch
            }
            view.findViewById<Toolbar>(R.id.proposalToolbar).setTitle(
                if (previousProposalId == null) R.string.chat_create_proposal else R.string.proposal_edit_title
            )
            val name = UserRepository.getUserById(otherUserId)?.name ?: getString(R.string.unknown_user)
            view.findViewById<TextView>(R.id.proposalOtherLabel).text = getString(R.string.proposal_other_gives, name)
            setupEditing(view)
            showItems(view)
            showMeeting(view)
        }
    }

    private fun setupEditing(view: View) {
        view.findViewById<MaterialButton>(R.id.proposalEditYourItems).apply {
            isVisible = true
            setOnClickListener { selectItems(userId, ownItems) }
        }
        view.findViewById<MaterialButton>(R.id.proposalEditOtherItems).apply {
            isVisible = true
            setOnClickListener { selectItems(otherUserId, otherItems) }
        }
        view.findViewById<MaterialButton>(R.id.proposalEditMeeting).apply {
            isVisible = true
            setOnClickListener { selectMeeting(view) }
        }
        view.findViewById<EditText>(R.id.proposalEircodeInput).apply {
            isVisible = true
            setText(eircode)
            doAfterTextChanged { eircode = it.toString() }
        }
        view.findViewById<View>(R.id.proposalActions).isVisible = true
        view.findViewById<View>(R.id.proposalClose).isVisible = false
        view.findViewById<View>(R.id.proposalEdit).isVisible = false
        view.findViewById<MaterialButton>(R.id.proposalAccept).apply {
            setText(if (previousProposalId == null) R.string.proposal_send else R.string.proposal_resend)
            isEnabled = !sending
            setOnClickListener { sendProposal(view) }
        }
    }

    private suspend fun loadInitialValues(): Boolean {
        userId = UserSession.getCurrentUser()?.id ?: return false
        val args = requireArguments()
        val sourceId = args.getString(ProposalFragment.ARG_PROPOSAL_ID)
        var proposal = sourceId?.let { ProposalRepository.getById(it) }
        if (sourceId != null && proposal == null) return false
        if (proposal != null) {
            if (userId !in listOf(proposal.proposerId, proposal.receiverId)) return false
            otherUserId = if (userId == proposal.proposerId) proposal.receiverId else proposal.proposerId
        } else {
            val conversationId = args.getString(ARG_CONVERSATION_ID)
            otherUserId = if (conversationId != null) {
                val conversation = ConversationRepository.getConversationById(conversationId) ?: return false
                ConversationRepository.getOtherUserId(conversation, userId) ?: return false
            } else {
                args.getString(ARG_OTHER_USER_ID) ?: return false
            }
            if (otherUserId == userId || UserRepository.getUserById(otherUserId) == null) return false
            val conversation = ConversationRepository.getForUsers(userId, otherUserId)
            proposal = conversation?.let { ProposalRepository.getPending(it.id) }
        }
        previousProposalId = proposal?.id
        if (proposal != null) {
            val items = ProposalRepository.getCurrentItems(proposal.id).filter { it.isAvailable() }
            ownItems = items.filter { it.ownerId == userId }
            otherItems = items.filter { it.ownerId == otherUserId }
        }
        args.getString(ARG_ITEM_ID)?.let { itemId ->
            val item = ItemRepository.getItemById(itemId)
            if (item != null && item.ownerId == otherUserId && item.isAvailable() &&
                otherItems.none { it.id == item.id } && otherItems.size < 8) {
                otherItems = otherItems + item
            }
        }
        meetingAt = proposal?.meetingAt ?: (System.currentTimeMillis() + 24 * 60 * 60 * 1000L)
        eircode = proposal?.eircode.orEmpty()
        initialized = true
        return true
    }

    private fun sendProposal(view: View) {
        if (sending) return
        val button = view.findViewById<MaterialButton>(R.id.proposalAccept)
        sending = true
        button.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val proposal = ProposalRepository.send(
                    senderId = userId,
                    receiverId = otherUserId,
                    ownItems = ownItems,
                    otherItems = otherItems,
                    meetingAt = meetingAt,
                    eircode = eircode,
                    previousProposalId = previousProposalId
                )
                Toast.makeText(requireContext(), R.string.proposal_sent, Toast.LENGTH_SHORT).show()
                val navController = findNavController()
                val existingChat = try {
                    navController.getBackStackEntry(R.id.chatDetailFragment)
                } catch (_: IllegalArgumentException) {
                    null
                }
                if (existingChat?.arguments?.getString(ChatDetailFragment.ARG_CONVERSATION_ID) == proposal.conversationId) {
                    navController.popBackStack(R.id.chatDetailFragment, false)
                } else {
                    navController.navigate(
                        R.id.chatDetailFragment,
                        Bundle().apply { putString(ChatDetailFragment.ARG_CONVERSATION_ID, proposal.conversationId) },
                        navOptions { popUpTo(R.id.proposalEditFragment) { inclusive = true } }
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IllegalArgumentException) {
                Toast.makeText(requireContext(), exception.message, Toast.LENGTH_LONG).show()
            } catch (_: Exception) {
                Toast.makeText(requireContext(), R.string.proposal_send_failed, Toast.LENGTH_LONG).show()
            } finally {
                sending = false
                button.isEnabled = true
            }
        }
    }

    private fun selectItems(ownerId: String, items: List<Item>) {
        findNavController().navigate(
            R.id.proposalItemPickerFragment,
            Bundle().apply {
                putString(ProposalItemPickerFragment.ARG_OWNER_ID, ownerId)
                putStringArray(ProposalItemPickerFragment.ARG_SELECTED_IDS, items.map { it.id }.toTypedArray())
            }
        )
    }

    private fun showItems(view: View) {
        fun show(gridId: Int, items: List<Item>) {
            view.findViewById<GridLayout>(gridId).showProposalImages(
                items.map { ProposalImage(it.id, it.title, it.displayImagePath(), it.status) }
            ) { itemId ->
                findNavController().navigate(
                    R.id.itemDetailFragment,
                    Bundle().apply { putString(ItemDetailFragment.ARG_ITEM_ID, itemId) }
                )
            }
        }
        show(R.id.proposalYourItems, ownItems)
        show(R.id.proposalOtherItems, otherItems)
    }

    private fun showMeeting(view: View) {
        val date = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(meetingAt))
        view.findViewById<TextView>(R.id.proposalMeeting).text = getString(R.string.proposal_meeting, date)
    }

    private fun selectMeeting(view: View) {
        val calendar = Calendar.getInstance().apply { timeInMillis = meetingAt }
        DatePickerDialog(requireContext(), { _, year, month, day ->
            calendar.set(year, month, day)
            TimePickerDialog(requireContext(), { _, hour, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                meetingAt = calendar.timeInMillis
                showMeeting(view)
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
