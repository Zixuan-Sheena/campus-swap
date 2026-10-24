package click.sheena.campusswap.data

import click.sheena.campusswap.model.Favourite
import click.sheena.campusswap.model.Item
import click.sheena.campusswap.model.ItemCategory
import click.sheena.campusswap.model.User

/**
 * Read-only Sample data for testing and demonstration purposes.
 * The data will be retrieved by Item/User/FavouriteRepository and be modified in memory, but will not be persisted.
 */
object SampleData {
    val items = listOf(
        Item(
            id = "114514-1",
            ownerId = "114514",
            title = "Two McMuffins",
            description = "Tasty, good for breakfast. Exchange with a coffee or a donut, or a croissant, or a bagel, or a muffin, or a scone, or a pancake, or a waffle, or a crepe, or a tart, or a pie, or a cake, or a cookie, or a brownie, or a bar.",
            category = ItemCategory.FOOD,
            wantedDescription = "Looking for snacks or drinks.",
            imagePath = "",
            createdAt = 1768473000000L
        ),
        Item(
            id = "114514-2",
            ownerId = "114514",
            title = "Tablet Stand",
            description = "Used for holding tablets, in good condition.",
            category = ItemCategory.SPORTS,
            wantedDescription = "Any novel or book, for example, a brief history of time, or a biography of a famous person, or a self-help book, or a cookbook, or a travel guide, or a dictionary, or an encyclopedia, or a thesaurus, or a manual, or a textbook, or a workbook, or a journal, or a diary.",
            imagePath = "",
            createdAt = 1779281100000L,
            updatedAt = 1779281100000L
        ),
        Item(
            id = "1919810-1",
            ownerId = "1919810",
            title = "Cat",
            description = "Cute, but damaging furniture. Needs a home.",
            category = ItemCategory.OTHER,
            wantedDescription = "Looking for a dog or a hamster.",
            imagePath = "",
            createdAt = 1786178400000L,
            updatedAt = 1786178400000L
        ),
        Item(
            id = "1919810-2",
            ownerId = "1919810",
            title = "Casa Mana",
            description = "Too sweet, need to exchange for something more bitter.",
            category = ItemCategory.FOOD,
            wantedDescription = "Looking for other alcoholic beverages, such as wine, beer, or spirits.",
            imagePath = "",
            createdAt = 1792254900000L,
            updatedAt = 1792254900000L
        ),
        Item(
            id = "12345678-1",
            ownerId = "12345678",
            title = "Canon New FD 50mm f/1.4 Lens",
            description = "A classic lens for Canon FD mount cameras. Great for portraits and low-light photography.",
            category = ItemCategory.ELECTRONICS,
            wantedDescription = "Looking for a new camera or other photography equipment.",
            imagePath = "",
            createdAt = 1768473000000L,
            updatedAt = 1768473000000L
        ),
        Item(
            id = "residentevil-1",
            ownerId = "12345678",
            title = "Resident Evil!!!",
            description = "I'll exchange my time and company for your time and company :)",
            category = ItemCategory.OTHER,
            wantedDescription = "Looking for a friend to watch Resident Evil together.",
            imagePath = "",
            createdAt = 1768473000000L,
            updatedAt = 1768473000000L
        )
    )

    val users = listOf(
        User(
            id = "114514",
            name = "Alice",
        ),
        User(
            id = "1919810",
            name = "Bob",
        ),
        User(
            id = "12345678",
            name = "David",
        ),
        User(
            id = "empty123",
            name = "Eva",
        )
    )

    val favourites = listOf(
        Favourite(
            userId = "114514",
            itemId = "1919810-1",
            createdAt = System.currentTimeMillis()
        )
    )


}