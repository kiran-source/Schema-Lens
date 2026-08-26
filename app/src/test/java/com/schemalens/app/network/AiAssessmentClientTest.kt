package com.schemalens.app.network

import com.schemalens.app.data.CallSite
import com.schemalens.app.data.RiskSeverity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AiAssessmentClientTest {

    private val client = AiAssessmentClient()

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
    fun testSmartSchemaEngineEvaluatesDroppedRoleAsBreaking() {
        val sites = listOf(
            CallSite(0, "auth.ts", 10, "if (user[0].role === 'admin')", "users"),
            CallSite(1, "service.ts", 25, "db.select().from(users)", "users")
        )
        val result = client.evaluateWithSmartSchemaEngine(
            packageName = "drizzle-orm",
            changeNotes = "Dropping deprecated role column from users table",
            callSites = sites
        )

        assertTrue(result.overallScore > 30)
        assertEquals(2, result.sites.size)
        assertEquals(RiskSeverity.BREAKING, result.sites[0].sev)
        assertNotNull(result.ormPatch)
    }
}
