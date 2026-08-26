package com.schemalens.app.network

import com.schemalens.app.data.RiskSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AnthropicApiClientTest {

    private val client = AnthropicApiClient()

    @Test
    fun testStripMarkdownFences() {
        val wrappedJson = """
            ```json
            {"overall_score": 75, "summary": "Breaking change on role column", "sites": [], "orm_patch": "export const users = ..."}
            ```
        """.trimIndent()

        val stripped = client.stripMarkdownFences(wrappedJson)
        assertEquals(
            """{"overall_score": 75, "summary": "Breaking change on role column", "sites": [], "orm_patch": "export const users = ..."}""",
            stripped
        )
    }

    @Test
    fun testParseAssessmentJson() {
        val json = """
            {
              "overall_score": 85,
              "summary": "Dropping role column will cause 2 runtime errors.",
              "sites": [
                {"index": 0, "sev": "green", "note": "Safe lookup by ID."},
                {"index": 1, "sev": "red", "note": "Accesses dropped column user.role."}
              ],
              "orm_patch": "export const users = pgTable('users', { id: text('id').primaryKey() });"
            }
        """.trimIndent()

        val result = client.parseAssessmentJson(json, 2)
        assertEquals(85, result.overallScore)
        assertEquals("Dropping role column will cause 2 runtime errors.", result.summary)
        assertEquals(2, result.sites.size)
        assertEquals(RiskSeverity.SAFE, result.sites[0].sev)
        assertEquals(RiskSeverity.BREAKING, result.sites[1].sev)
        assertNotNull(result.ormPatch)
    }
}
