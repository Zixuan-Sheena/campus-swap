package click.sheena.campusswap.model

import java.util.UUID

enum class ItemStatus {
    AVAILABLE,  // Item is available for exchange
    RESERVED,   // Item is currently in a Proposal, not available for exchange, hidden from search and user profile
    EXCHANGED,  // Item has been exchanged, hidden from search and user profile
    WITHDRAWN   // Item has been withdrawn by the owner, hidden from search and user profile
}

enum class ItemCategory {
    BOOKS,
    ELECTRONICS,
    CLOTHING,
    FURNITURE,
    SPORTS,
    TOOLS,
    VEHICLES,
    OTHER
}

data class Item(
    val id: String = UUID.randomUUID().toString(),
    val ownerId: Long,
    var title: String,
    val description: String,
    val category: ItemCategory,
    val wantedDescription: String,
    val imagePath: String,
    val status: ItemStatus = ItemStatus.AVAILABLE,
    val revision: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt
){
    init {
        require(title.isNotBlank()) { "Title cannot be blank" }
        require(description.isNotBlank()) { "Description cannot be blank" }
        require(wantedDescription.isNotBlank()) { "Wanted description cannot be blank" }
    }

    fun updateItem(
        newTitle: String = this.title,
        newDescription: String = this.description,
        newCategory: ItemCategory = this.category,
        newWantedDescription: String = this.wantedDescription,
        newImagePath: String = this.imagePath,
        newStatus: ItemStatus = this.status
    ): Item {
        return this.copy(
            title = newTitle,
            description = newDescription,
            category = newCategory,
            wantedDescription = newWantedDescription,
            imagePath = newImagePath,
            status = newStatus,
            revision = this.revision + 1,
            updatedAt = System.currentTimeMillis()
        )
    }

    fun isOwnedBy(userId: Long): Boolean {
        return this.ownerId == userId
    }

    fun isAvailable(): Boolean {
        return this.status == ItemStatus.AVAILABLE
    }

    fun canBeEditedBy(userId: Long): Boolean {
        return this.ownerId == userId && this.status == ItemStatus.AVAILABLE
    }
}
