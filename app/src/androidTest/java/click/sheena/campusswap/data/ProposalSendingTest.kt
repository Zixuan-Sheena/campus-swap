package click.sheena.campusswap.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import click.sheena.campusswap.model.Exchange
import click.sheena.campusswap.model.Item
import click.sheena.campusswap.model.ItemStatus
import click.sheena.campusswap.model.MessageType
import click.sheena.campusswap.model.Proposal
import click.sheena.campusswap.model.ProposalStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProposalSendingTest {
    private lateinit var database: CampusSwapDatabase
    private val aliceItem = SampleData.items.first { it.id == "114514-2" }
    private val bobItem = SampleData.items.first { it.id == "1919810-2" }
    private val futureTime get() = System.currentTimeMillis() + 86_400_000L

    @Before
    fun setup() = runBlocking {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            CampusSwapDatabase::class.java
        ).build()
        database.items().insertAll(listOf(aliceItem, bobItem))
    }

    @After
    fun cleanup() {
        database.close()
    }

    private suspend fun send(
        previousId: String? = null,
        fromBob: Boolean = false,
        time: Long = futureTime,
        location: String = " V94 T9PX ",
        offered: List<Item> = listOf(if (fromBob) bobItem else aliceItem)
    ): Proposal = ProposalRepository.send(
        database = database,
        senderId = if (fromBob) bobItem.ownerId else aliceItem.ownerId,
        receiverId = if (fromBob) aliceItem.ownerId else bobItem.ownerId,
        ownItems = offered,
        otherItems = listOf(if (fromBob) aliceItem else bobItem),
        meetingAt = time,
        eircode = location,
        previousProposalId = previousId
    )

    private suspend fun expectInvalid(action: suspend () -> Unit) {
        try {
            action()
            fail("Expected invalid proposal to be rejected")
        } catch (_: IllegalArgumentException) {
        }
    }

    @Test
    fun sendsMessageAndSnapshotsWithoutReservingItems() = runBlocking {
        val proposal = send()
        assertEquals(1, proposal.version)
        assertEquals("V94 T9PX", proposal.eircode)
        val conversation = database.conversations().getForPair(aliceItem.ownerId, bobItem.ownerId)
        assertEquals(proposal.conversationId, conversation?.id)
        val message = database.messages().getForConversation(proposal.conversationId).single()
        assertEquals(MessageType.PROPOSAL, message.type)
        assertEquals(proposal.id, message.proposalId)
        val snapshot = database.proposalItems().getForProposal(proposal.id).first { it.itemId == aliceItem.id }
        database.items().update(aliceItem.updateItem(newTitle = "Changed later"))
        assertEquals(aliceItem.title, snapshot.titleSnapshot)
        assertEquals(aliceItem.title, database.proposalItems().getForProposal(proposal.id).first { it.itemId == aliceItem.id }.titleSnapshot)
        assertEquals(ItemStatus.AVAILABLE, database.items().getById(bobItem.id)?.status)
    }

    @Test
    fun receiverCounterofferReusesChatAndSupersedesPendingVersion() = runBlocking {
        val first = send()
        val second = send(first.id, fromBob = true)
        assertEquals(first.conversationId, second.conversationId)
        assertEquals(2, second.version)
        assertEquals(bobItem.ownerId, second.proposerId)
        assertEquals(aliceItem.ownerId, second.receiverId)
        assertEquals(ProposalStatus.SUPERSEDED, database.proposals().getById(first.id)?.status)
        assertEquals(second.id, database.proposals().getPending(first.conversationId)?.id)
        assertEquals(2, database.messages().getForConversation(first.conversationId).size)
        assertEquals(1, database.conversations().getForUser(aliceItem.ownerId).size)
    }

    @Test
    fun senderCanResendRejectedProposalWithoutChangingHistory() = runBlocking {
        val first = send()
        database.proposals().updateStatus(first.id, ProposalStatus.REJECTED)
        val second = send(first.id)
        assertEquals(2, second.version)
        assertEquals(ProposalStatus.REJECTED, database.proposals().getById(first.id)?.status)
        assertEquals(ProposalStatus.PENDING, second.status)
    }

    @Test
    fun rejectsIncompleteFormsWithoutCreatingChat() = runBlocking {
        expectInvalid { send(offered = emptyList()) }
        expectInvalid { send(time = 1L) }
        expectInvalid { send(location = " ") }
        assertTrue(database.conversations().getForUser(aliceItem.ownerId).isEmpty())
    }

    @Test
    fun rejectsChangedOrUnavailableItemsWithoutCreatingChat() = runBlocking {
        database.items().update(aliceItem.updateItem(newTitle = "Edited"))
        expectInvalid { send() }
        database.items().update(aliceItem.copy(status = ItemStatus.RESERVED))
        expectInvalid { send() }
        assertTrue(database.conversations().getForUser(aliceItem.ownerId).isEmpty())
    }

    @Test
    fun rejectsDuplicateOrOutdatedSends() = runBlocking {
        val first = send()
        expectInvalid { send() }
        val second = send(first.id)
        expectInvalid { send(first.id) }
        assertEquals(second.id, database.proposals().getPending(first.conversationId)?.id)
        assertEquals(2, database.messages().getForConversation(first.conversationId).size)
    }

    @Test
    fun blocksSendingDuringHandover() = runBlocking {
        val first = send()
        database.proposals().updateStatus(first.id, ProposalStatus.ACCEPTED)
        database.exchanges().insert(Exchange(conversationId = first.conversationId, proposalId = first.id))
        expectInvalid { send() }
        assertEquals(1, database.proposals().getForConversation(first.conversationId).size)
    }

    @Test
    fun rollsBackVersionAndSnapshotsIfMessageCannotBeSaved() = runBlocking {
        val first = send()
        database.openHelper.writableDatabase.execSQL(
            "CREATE TRIGGER fail_message BEFORE INSERT ON messages BEGIN SELECT RAISE(ABORT, 'test failure'); END"
        )
        var failed = false
        try {
            send(first.id)
        } catch (_: android.database.sqlite.SQLiteException) {
            failed = true
        }
        assertTrue(failed)
        assertEquals(ProposalStatus.PENDING, database.proposals().getById(first.id)?.status)
        assertEquals(1, database.proposals().getForConversation(first.conversationId).size)
        assertEquals(2, database.proposalItems().getForProposal(first.id).size)
        assertEquals(1, database.messages().getForConversation(first.conversationId).size)
        val cursor = database.openHelper.readableDatabase.query("SELECT COUNT(*) FROM proposal_items")
        cursor.use {
            it.moveToFirst()
            assertEquals(2, it.getInt(0))
        }
    }
}
