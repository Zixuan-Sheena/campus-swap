package click.sheena.campusswap.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ItemTest {
    @Test
    fun createAndEditItem() {
        val testUserId = "114514"
        val item = Item(
            ownerId = testUserId,
            title = "Test Item",
            description = "This is a test item.",
            category = ItemCategory.OTHER,
            wantedDescription = "Any thing is fine.",
            imagePath = "",
        )

        // Edit the item
        val updatedItem = item.updateItem(
            newDescription = "This is an updated test item.",
            newStatus = ItemStatus.RESERVED
        )

        // Check updated values
        assertEquals("This is an updated test item.", updatedItem.description)
        assertEquals(ItemStatus.RESERVED, updatedItem.status)
        assertEquals(item.revision + 1, updatedItem.revision)
        assertFalse(updatedItem.isAvailable())
        assertTrue(updatedItem.isOwnedBy(testUserId))
        assertFalse(item.canBeEditedBy("123456"))

        print(updatedItem)
    }
}