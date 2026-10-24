package ch.coachpoulpy.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdsTest {
    @Test
    fun uuidV7HasVersionAndVariantBits() {
        val id = Ids.newUuidV7(nowMillis = 1_700_000_000_000)
        assertEquals(36, id.length)
        assertEquals('7', id[14])
        assertTrue(id[19] in "89ab")
    }

    @Test
    fun uuidV7SortsByCreationTime() {
        val earlier = Ids.newUuidV7(nowMillis = 1_700_000_000_000)
        val later = Ids.newUuidV7(nowMillis = 1_700_000_000_001)
        assertTrue(earlier < later)
    }
}
