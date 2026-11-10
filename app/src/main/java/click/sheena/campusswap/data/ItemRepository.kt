package click.sheena.campusswap.data

import androidx.room.withTransaction
import click.sheena.campusswap.model.Item
import click.sheena.campusswap.model.ItemCategory
import click.sheena.campusswap.model.ItemStatus

object ItemRepository {
    suspend fun getItems(category: ItemCategory? = null): List<Item> =
        searchItems(category)

    suspend fun getItemByOwnerId(ownerId: String): List<Item> =
        CampusSwapDatabase.ready().items().getByOwner(ownerId)

    suspend fun getItemById(itemId: String): Item? =
        CampusSwapDatabase.ready().items().getById(itemId)

    suspend fun searchItems(
        category: ItemCategory? = null,
        keyword: String = ""
    ): List<Item> {
        val query = keyword.trim()
        return CampusSwapDatabase.ready().items().getAll()
            .filter { item ->
                item.isAvailable() &&
                        (category == null || item.category == category) &&
                        (item.title.contains(query, ignoreCase = true) ||
                                item.description.contains(query, ignoreCase = true) ||
                                item.wantedDescription.contains(query, ignoreCase = true))
            }
            .sortedByDescending { it.updatedAt }
    }

    suspend fun addItem(item: Item): Item {
        CampusSwapDatabase.ready().items().insert(item)
        return item
    }

    suspend fun withdrawItem(itemId: String, userId: String): Item? =
        changeOwnedItem(itemId, userId) {
            it.updateItem(newStatus = ItemStatus.WITHDRAWN)
        }

    suspend fun updateItem(
        itemId: String,
        userId: String,
        title: String,
        description: String,
        category: ItemCategory,
        wantedDescription: String,
        imagePath: String
    ): Item? = changeOwnedItem(itemId, userId) {
        it.updateItem(
            newTitle = title,
            newDescription = description,
            newCategory = category,
            newWantedDescription = wantedDescription,
            newImagePath = imagePath
        )
    }

    private suspend fun changeOwnedItem(
        itemId: String,
        userId: String,
        change: (Item) -> Item
    ): Item? {
        val database = CampusSwapDatabase.ready()
        val dao = database.items()

        return database.withTransaction {
            val current = dao.getById(itemId) ?: return@withTransaction null
            if (!current.canBeEditedBy(userId)) return@withTransaction null

            val updated = change(current)
            dao.update(updated)
            updated
        }
    }
}