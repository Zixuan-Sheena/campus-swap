package click.sheena.campusswap.data

import click.sheena.campusswap.model.Favourite
import click.sheena.campusswap.model.Conversation
import click.sheena.campusswap.model.Item
import click.sheena.campusswap.model.ItemCategory
import click.sheena.campusswap.model.Message
import click.sheena.campusswap.model.User
import click.sheena.campusswap.model.Proposal
import click.sheena.campusswap.model.ProposalItem
import click.sheena.campusswap.model.MessageType

object SampleData {
    val items = listOf(
        Item(
            id = "114514-1",
            ownerId = "114514",
            title = "Two McMuffins",
            description = "Tasty, good for breakfast. Exchange with a coffee or a donut, or a croissant, or a bagel, or a muffin, or a scone, or a pancake, or a waffle, or a crepe, or a tart, or a pie, or a cake, or a cookie, or a brownie, or a bar.",
            category = ItemCategory.FOOD,
            wantedDescription = "Looking for snacks or drinks.",
            imagePath = "sample:breakfast",
            createdAt = 1768473000000L
        ),
        Item(
            id = "114514-2",
            ownerId = "114514",
            title = "Tablet Stand",
            description = "Used for holding tablets, in good condition.",
            category = ItemCategory.SPORTS,
            wantedDescription = "Any novel or book, for example, a brief history of time, or a biography of a famous person, or a self-help book, or a cookbook, or a travel guide, or a dictionary, or an encyclopedia, or a thesaurus, or a manual, or a textbook, or a workbook, or a journal, or a diary.",
            imagePath = "sample:tablet_stand",
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
            imagePath = "sample:cat",
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
            imagePath = "sample:bottle",
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
            imagePath = "sample:camera_lens",
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
            imagePath = "sample:movie_night",
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

    val conversations = listOf(
        Conversation(
            id = "conversation-alice-bob",
            userLowId = "114514",
            userHighId = "1919810",
            createdAt = 1768473000000L
        ),
        Conversation(
            id = "conversation-alice-david",
            userLowId = "114514",
            userHighId = "12345678",
            createdAt = 1768476600000L
        )
    )

    val proposals = listOf(
        Proposal(
            id = "proposal-bob-alice",
            conversationId = "conversation-alice-bob",
            version = 1,
            proposerId = "1919810",
            receiverId = "114514",
            meetingAt = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L,
            eircode = "T12 AY95",
            createdAt = 1768473180000L
        )
    )

    val proposalItems = items.filter { it.id in listOf("114514-2", "1919810-2") }
        .map { item ->
            ProposalItem(
                proposalId = "proposal-bob-alice",
                itemId = item.id,
                ownerIdSnapshot = item.ownerId,
                itemRevision = item.revision,
                titleSnapshot = item.title,
                descriptionSnapshot = item.description,
                categorySnapshot = item.category.name,
                imagePathSnapshot = item.imagePath
            )
        }

    val messages = listOf(
        Message(
            id = "message-proposal-1",
            conversationId = "conversation-alice-bob",
            senderId = "1919810",
            type = MessageType.PROPOSAL,
            proposalId = "proposal-bob-alice",
            createdAt = 1768473180000L
        ),
        Message(
            id = "message-1",
            conversationId = "conversation-alice-bob",
            senderId = "1919810",
            text = "Hi Alice, is the Tablet Stand still available?",
            createdAt = 1768473060000L
        ),
        Message(
            id = "message-2",
            conversationId = "conversation-alice-bob",
            senderId = "114514",
            text = "Yes, it is. What book would you like to exchange?",
            createdAt = 1768473120000L
        ),
        Message(
            id = "message-3",
            conversationId = "conversation-alice-david",
            senderId = "12345678",
            text = "Would you be interested in swapping for the camera lens?",
            createdAt = 1768476660000L
        )
    )


}
