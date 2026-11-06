package ch.coachpoulpy.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppIconsTest {
    @Test
    fun theStandardComesFirstAndIsHappy() {
        assertEquals(AppIcon.HAPPY, AppIcons.standard)
        assertEquals(AppIcons.standard, AppIcons.all.first())
    }

    @Test
    fun idsAreUniqueAndLowerCase() {
        val ids = AppIcons.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(ids.all { id -> id.isNotEmpty() && id == id.lowercase() && id.all { it.isLetter() } })
    }

    @Test
    fun anIdFindsItsIconAndAnythingElseFallsBackToTheDefault() {
        AppIcons.all.forEach { assertEquals(it, AppIcons.fromId(it.id)) }
        assertEquals(AppIcons.standard, AppIcons.fromId(null))
        assertEquals(AppIcons.standard, AppIcons.fromId("retired"))
    }
}
