package click.sheena.campusswap.data

import click.sheena.campusswap.model.Favourite

object FavouriteRepository {
    private val favourites = SampleData.favourites.map { it.copy() }.toMutableList()

    fun addFavourite(userId: String, itemId: String) {
        if (isFavourite(userId, itemId)) return

        favourites.add(
            Favourite(
                userId = userId,
                itemId = itemId,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    fun removeFavourite(userId: String, itemId: String) {
        favourites.removeAll {
            it.userId == userId && it.itemId == itemId
        }
    }

    fun isFavourite(userId: String, itemId: String): Boolean {
        return favourites.any {
            it.userId == userId && it.itemId == itemId
        }
    }

    fun getFavourites(userId: String): List<Favourite> {
        return favourites
            .filter { it.userId == userId }
            .sortedByDescending { it.createdAt }
    }
}