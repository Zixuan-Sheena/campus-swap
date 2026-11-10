package click.sheena.campusswap.data

import androidx.room.withTransaction
import click.sheena.campusswap.model.Favourite

object FavouriteRepository {
    suspend fun addFavourite(userId: String, itemId: String) {
        CampusSwapDatabase.ready().favourites().insert(
            Favourite(userId, itemId, System.currentTimeMillis())
        )
    }

    suspend fun removeFavourite(userId: String, itemId: String) {
        CampusSwapDatabase.ready().favourites().remove(userId, itemId)
    }

    suspend fun isFavourite(userId: String, itemId: String): Boolean =
        CampusSwapDatabase.ready().favourites().contains(userId, itemId)

    suspend fun getFavourites(userId: String): List<Favourite> =
        CampusSwapDatabase.ready().favourites().getForUser(userId)

    suspend fun toggleFavourite(userId: String, itemId: String): Boolean {
        val database = CampusSwapDatabase.ready()
        val dao = database.favourites()
        return database.withTransaction {
            val shouldAdd = !dao.contains(userId, itemId)
            if (shouldAdd) {
                dao.insert(Favourite(userId, itemId, System.currentTimeMillis()))
            } else {
                dao.remove(userId, itemId)
            }
            shouldAdd
        }
    }
}