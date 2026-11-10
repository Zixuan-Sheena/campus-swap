package click.sheena.campusswap.data

import android.content.Context
import androidx.room.*
import click.sheena.campusswap.model.Favourite
import click.sheena.campusswap.model.Item
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY rowid")
    suspend fun getAll(): List<Item>

    @Query("SELECT * FROM items WHERE ownerId = :ownerId ORDER BY rowid")
    suspend fun getByOwner(ownerId: String): List<Item>

    @Query("SELECT * FROM items WHERE id = :itemId")
    suspend fun getById(itemId: String): Item?

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

@Database(entities = [Item::class, Favourite::class], version = 1, exportSchema = false)
abstract class CampusSwapDatabase : RoomDatabase() {
    abstract fun items(): ItemDao
    abstract fun favourites(): FavouriteDao

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