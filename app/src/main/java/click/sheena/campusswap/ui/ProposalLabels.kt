package click.sheena.campusswap.ui

import android.content.Context
import click.sheena.campusswap.R
import click.sheena.campusswap.model.ProposalStatus

fun Context.proposalLabel(status: ProposalStatus): String {
    val statusRes = when (status) {
        ProposalStatus.PENDING -> R.string.proposal_pending
        ProposalStatus.REJECTED -> R.string.proposal_rejected
        ProposalStatus.WITHDRAWN -> R.string.proposal_withdrawn
        ProposalStatus.ACCEPTED -> R.string.proposal_accepted
        ProposalStatus.SUPERSEDED -> R.string.proposal_superseded
        ProposalStatus.INVALIDATED -> R.string.proposal_invalidated
    }
    return getString(R.string.proposal_status_label, getString(statusRes))
}
