package click.sheena.campusswap.data

import click.sheena.campusswap.model.Item
import click.sheena.campusswap.model.ItemCategory
import kotlin.collections.mutableListOf

object SampleItems {
    val items = mutableListOf(
        Item(
            ownerId = "114514",
            title = "Two McMuffins",
            description = "Tasty, good for breakfast. Exchange with a coffee or a donut, or a croissant, or a bagel, or a muffin, or a scone, or a pancake, or a waffle, or a crepe, or a tart, or a pie, or a cake, or a cookie, or a brownie, or a bar.",
            category = ItemCategory.FOOD,
            wantedDescription = "Looking for snacks or drinks.",
            imagePath = ""
        ),
        Item(
            ownerId = "1919810",
            title = "Tablet Stand",
            description = "Used for holding tablets, in good condition.",
            category = ItemCategory.SPORTS,
            wantedDescription = "Any novel or book, for example, a brief history of time, or a biography of a famous person, or a self-help book, or a cookbook, or a travel guide, or a dictionary, or an encyclopedia, or a thesaurus, or a manual, or a textbook, or a workbook, or a journal, or a diary.",
            imagePath = ""
        ),
        Item(
            ownerId = "qwerty",
            title = "Cat",
            description = "Cute, but damaging furniture. Needs a home.",
            category = ItemCategory.OTHER,
            wantedDescription = "Looking for a dog or a hamster.",
            imagePath = ""
        ),
        Item(
            ownerId = "12345678",
            title = "Casa Mana",
            description = "Too sweet, need to exchange for something more bitter.",
            category = ItemCategory.FOOD,
            wantedDescription = "Looking for other alcoholic beverages, such as wine, beer, or spirits.",
            imagePath = ""
        ),
        Item(
            ownerId = "87654321",
            title = "Canon New FD 50mm f/1.4 Lens",
            description = "A classic lens for Canon FD mount cameras. Great for portraits and low-light photography.",
            category = ItemCategory.ELECTRONICS,
            wantedDescription = "Looking for a new camera or other photography equipment.",
            imagePath = ""
        )
    )
}