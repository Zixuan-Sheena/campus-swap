package click.sheena.campusswap.data

import click.sheena.campusswap.model.Item
import click.sheena.campusswap.model.ItemCategory

object ItemRepository {

    private val items = SampleData.items.map { it.copy() }.toMutableList()

    fun getItems(category: ItemCategory? = null): List<Item> {
        return items
            .filter { item ->
                item.isAvailable() &&
                        (category == null || item.category == category)
            }
            .map { it.copy() }
    }

    fun getItemByOwnerId(ownerId: String): List<Item> {
        return items
            .filter { item ->
                item.ownerId == ownerId
            }
            .map { it.copy() }
    }

    fun getItemById(itemId: String): Item? {
        return items
            .find { item ->
                item.id == itemId
            }
            ?.copy()
    }

    fun searchItems(
        category: ItemCategory? = null,
        keyword: String = ""
    ): List<Item> {
        val query = keyword.trim()

        return items
            .filter { item ->
                item.isAvailable() &&
                        (category == null || item.category == category) &&
                        item.title.contains(query, ignoreCase = true) ||
                        item.description.contains(query, ignoreCase = true) ||
                        item.wantedDescription.contains(query, ignoreCase = true)
            }
            .map { it.copy() }
    }
}