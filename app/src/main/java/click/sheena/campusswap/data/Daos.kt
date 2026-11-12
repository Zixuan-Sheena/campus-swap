package click.sheena.campusswap.data

import androidx.room.*
import click.sheena.campusswap.model.*

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY rowid")
    suspend fun getAll(): List<Item>

    @Query("SELECT * FROM items WHERE ownerId = :ownerId ORDER BY rowid")
    suspend fun getByOwner(ownerId: String): List<Item>

    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getById(itemId: String): Item?

    @Query("SELECT * FROM items WHERE id IN (:itemIds)")
    suspend fun getByIds(itemIds: List<String>): List<Item>

    @Query("SELECT * FROM items WHERE ownerId = :ownerId AND status = 'AVAILABLE' ORDER BY createdAt DESC")
    suspend fun getAvailableByOwner(ownerId: String): List<Item>

    @Query("UPDATE items SET status = :status, updatedAt = :updatedAt WHERE id IN (:itemIds)")
    suspend fun updateStatus(itemIds: List<String>, status: ItemStatus, updatedAt: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM items)")
    suspend fun hasItems(): Boolean

    @Insert
    suspend fun insert(item: Item)

    @Insert
    suspend fun insertAll(items: List<Item>)

    @Update
    suspend fun update(item: Item)
}

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favourite: Favourite)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(favourites: List<Favourite>)

    @Query("DELETE FROM favourites WHERE userId = :userId AND itemId = :itemId")
    suspend fun remove(userId: String, itemId: String)

    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM favourites WHERE userId = :userId AND itemId = :itemId
        )
    """)
    suspend fun contains(userId: String, itemId: String): Boolean

    @Query("SELECT * FROM favourites WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getForUser(userId: String): List<Favourite>
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getById(conversationId: String): Conversation?

    @Query("SELECT * FROM conversations WHERE userLowId = :userId OR userHighId = :userId")
    suspend fun getForUser(userId: String): List<Conversation>

    @Query("SELECT * FROM conversations WHERE userLowId = :userLowId AND userHighId = :userHighId LIMIT 1")
    suspend fun getForPair(userLowId: String, userHighId: String): Conversation?

    @Query("SELECT EXISTS(SELECT 1 FROM conversations)")
    suspend fun hasConversations(): Boolean

    @Insert
    suspend fun insert(value: Conversation)

    @Insert
    suspend fun insertAll(values: List<Conversation>)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt, rowid")
    suspend fun getForConversation(conversationId: String): List<Message>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt DESC, rowid DESC LIMIT 1")
    suspend fun getLatest(conversationId: String): Message?

    @Insert
    suspend fun insert(value: Message)

    @Insert
    suspend fun insertAll(values: List<Message>)
}

@Dao
interface ProposalDao {
    @Query("SELECT * FROM proposals WHERE id = :proposalId")
    suspend fun getById(proposalId: String): Proposal?

    @Query("SELECT * FROM proposals WHERE conversationId = :conversationId ORDER BY version")
    suspend fun getForConversation(conversationId: String): List<Proposal>

    @Query("SELECT * FROM proposals WHERE conversationId = :conversationId ORDER BY version DESC LIMIT 1")
    suspend fun getLatest(conversationId: String): Proposal?

    @Query("SELECT * FROM proposals WHERE conversationId = :conversationId AND status = 'PENDING' ORDER BY version DESC LIMIT 1")
    suspend fun getPending(conversationId: String): Proposal?

    @Query("SELECT COALESCE(MAX(version), 0) + 1 FROM proposals WHERE conversationId = :conversationId")
    suspend fun getNextVersion(conversationId: String): Int

    @Query("UPDATE proposals SET status = :status WHERE id = :proposalId")
    suspend fun updateStatus(proposalId: String, status: ProposalStatus)

    @Query("""
        UPDATE proposals SET status = 'INVALIDATED'
        WHERE status = 'PENDING' AND id != :acceptedProposalId
        AND id IN (SELECT proposalId FROM proposal_items WHERE itemId IN (:itemIds))
    """)
    suspend fun invalidateForItems(itemIds: List<String>, acceptedProposalId: String)

    @Insert
    suspend fun insert(value: Proposal)

    @Insert
    suspend fun insertAll(values: List<Proposal>)
}

@Dao
interface ProposalItemDao {
    @Query("SELECT * FROM proposal_items WHERE proposalId = :proposalId")
    suspend fun getForProposal(proposalId: String): List<ProposalItem>

    @Insert
    suspend fun insert(value: ProposalItem)

    @Insert
    suspend fun insertAll(values: List<ProposalItem>)
}

@Dao
interface ExchangeDao {
    @Query("SELECT * FROM exchanges WHERE id = :exchangeId")
    suspend fun getById(exchangeId: String): Exchange?

    @Query("SELECT * FROM exchanges WHERE proposalId = :proposalId")
    suspend fun getForProposal(proposalId: String): Exchange?

    @Query("SELECT * FROM exchanges WHERE conversationId = :conversationId AND status = 'AWAITING_HANDOVER' LIMIT 1")
    suspend fun getAwaitingHandover(conversationId: String): Exchange?

    @Query("""
        SELECT exchanges.* FROM exchanges
        INNER JOIN conversations ON conversations.id = exchanges.conversationId
        WHERE conversations.userLowId = :userId OR conversations.userHighId = :userId
        ORDER BY exchanges.createdAt DESC
    """)
    suspend fun getForUser(userId: String): List<Exchange>

    @Query("SELECT * FROM exchanges WHERE conversationId = :conversationId ORDER BY createdAt DESC")
    suspend fun getForConversation(conversationId: String): List<Exchange>

    @Update
    suspend fun update(exchange: Exchange)

    @Insert
    suspend fun insert(value: Exchange)

    @Insert
    suspend fun insertAll(values: List<Exchange>)
}
