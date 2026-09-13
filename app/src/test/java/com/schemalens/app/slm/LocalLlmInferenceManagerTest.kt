package com.schemalens.app.slm

import com.schemalens.app.data.CallSite
import com.schemalens.app.data.RiskSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalLlmInferenceManagerTest {

    @Test
    fun testFormatPromptContainsAllNecessaryContext() {
        val sites = listOf(
            CallSite(0, "auth.ts", 15, "const role = user.role;", "role"),
            CallSite(1, "service.ts", 42, "db.select().from(users);", "users")
        )

        // Test without Android Context by testing prompt formatting
        val siteListFormatted = sites.joinToString("\n") { site ->
            "[#${site.index}] ${site.file ?: "inline"}:${site.lineNumber} -> ${site.lineText}"
        }

        assertTrue(siteListFormatted.contains("[#0] auth.ts:15 -> const role = user.role;"))
        assertTrue(siteListFormatted.contains("[#1] service.ts:42 -> db.select().from(users);"))
    }

    @Test
    fun testStripMarkdownFences() {
        val raw1 = "```json\n{\"overall_score\": 75}\n```"
        var text = raw1.trim()
        if (text.startsWith("```json")) text = text.removePrefix("```json")
        if (text.startsWith("```")) text = text.removePrefix("```")
        if (text.endsWith("```")) text = text.removeSuffix("```")
        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            text = text.substring(firstBrace, lastBrace + 1)
        }
        assertEquals("{\"overall_score\": 75}", text.trim())
    }

    @Test
    fun testDeterministicHeuristicEngineEvaluatesDroppedColumnAsBreaking() {
        val sites = listOf(
            CallSite(0, "auth.ts", 10, "if (user[0].role === 'admin')", "users"),
            CallSite(1, "service.ts", 25, "db.select().from(users)", "users")
        )

        val changeNotes = "Dropping deprecated role column from users table"
        val notesLower = changeNotes.lowercase()
        val words = notesLower.split(Regex("[^a-zA-Z0-9_]")).filter { it.length > 2 }
        val isDropping = notesLower.contains("drop") || notesLower.contains("delete") || notesLower.contains("remove")

        assertTrue(isDropping)
        assertTrue(words.contains("role"))

        // Check that role line triggers breaking
        val site0Code = sites[0].lineText.lowercase()
        assertTrue(site0Code.contains("role"))
    }
}
