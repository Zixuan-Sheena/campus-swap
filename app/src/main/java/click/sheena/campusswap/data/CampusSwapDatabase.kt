package click.sheena.campusswap.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import click.sheena.campusswap.model.Favourite
import click.sheena.campusswap.model.Item
import click.sheena.campusswap.model.Conversation
import click.sheena.campusswap.model.Message
import click.sheena.campusswap.model.Proposal
import click.sheena.campusswap.model.ProposalItem
import click.sheena.campusswap.model.Exchange
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Database(
    entities = [Item::class, Favourite::class, Conversation::class, Message::class,
        Proposal::class, ProposalItem::class, Exchange::class],
    version = 1,
    exportSchema = false
)
abstract class CampusSwapDatabase : RoomDatabase() {
    abstract fun items(): ItemDao
    abstract fun favourites(): FavouriteDao
    abstract fun conversations(): ConversationDao
    abstract fun messages(): MessageDao
    abstract fun proposals(): ProposalDao
    abstract fun proposalItems(): ProposalItemDao
    abstract fun exchanges(): ExchangeDao

    companion object {
        private lateinit var database: CampusSwapDatabase
        private val seedLock = Mutex()
        private var seeded = false

        fun initialize(context: Context) {
            database = Room.databaseBuilder(
                context.applicationContext,
                CampusSwapDatabase::class.java,
                "campus-swap.db"
            ).build()
        }

        suspend fun ready(): CampusSwapDatabase = seedLock.withLock {
            if (!seeded) {
                database.withTransaction {
                    if (!database.conversations().hasConversations()) {
                        database.conversations().insertAll(SampleData.conversations)
                        database.messages().insertAll(SampleData.messages)
                    }
                    if (!database.items().hasItems()) {
                        database.items().insertAll(SampleData.items)
                        database.favourites().insertAll(SampleData.favourites)
                    }
                }
                seeded = true
            }
            database
        }
    }
}