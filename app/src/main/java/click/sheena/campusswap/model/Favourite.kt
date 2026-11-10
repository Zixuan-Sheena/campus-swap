package click.sheena.campusswap.model

import androidx.room.Entity

@Entity(tableName = "favourites", primaryKeys = ["userId", "itemId"])
data class Favourite(
    val userId: String,
    val itemId: String,
    val createdAt: Long
)
