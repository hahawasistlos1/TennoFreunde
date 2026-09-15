package com.example.tennofreunde

import com.example.tennofreunde.data.SavedBuild
import com.example.tennofreunde.data.buildCompleteness
import com.example.tennofreunde.data.buildShareText
import com.example.tennofreunde.data.duplicateBuild
import com.example.tennofreunde.data.upsertBuild
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildArchiveTest {
    private fun completeBuild(id: String = "one", name: String = "Wisp Stahlpfad") = SavedBuild(
        id = id,
        name = name,
        target = "Wisp Prime",
        purpose = "Stahlpfad",
        mods = "Vitality\nStretch\nContinuity",
        arcanes = "Molt Augmented",
        helminth = "Roar",
        focusSchool = "Zenurik",
        updatedAt = 1L
    )

    @Test
    fun upsertReplacesSameBuildAndRejectsIncompleteIdentity() {
        val original = completeBuild()
        val updated = original.copy(mods = "A\nB\nC", updatedAt = 2L)
        val result = upsertBuild(listOf(original), updated)

        assertEquals(1, result.size)
        assertEquals(2L, result.single().updatedAt)
        assertEquals(result, upsertBuild(result, original.copy(target = "")))
    }

    @Test
    fun duplicateGetsUniqueNameAndCompletenessIsVisible() {
        val original = completeBuild()
        val firstCopy = duplicateBuild(listOf(original), original, 2L, true)
        val secondCopy = duplicateBuild(firstCopy, original, 3L, true)

        assertTrue(secondCopy.any { it.name == "Wisp Stahlpfad Kopie" })
        assertTrue(secondCopy.any { it.name == "Wisp Stahlpfad Kopie 2" })
        assertEquals(100, buildCompleteness(original))
        assertTrue(buildCompleteness(original.copy(mods = "Vitality")) < 100)
    }

    @Test
    fun copiedTextContainsUsefulBuildFields() {
        val text = buildShareText(completeBuild(), true)
        assertTrue(text.contains("Wisp Prime"))
        assertTrue(text.contains("Vitality"))
        assertTrue(text.contains("Molt Augmented"))
        assertFalse(text.contains("null"))
    }
}
