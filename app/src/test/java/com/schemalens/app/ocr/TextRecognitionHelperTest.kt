package com.schemalens.app.ocr

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextRecognitionHelperTest {

    @Test
    fun testExtractIdentifiersFindsAllCandidateIdentifiers() {
        val sampleText = """
            CREATE TABLE users (
                id VARCHAR(36) PRIMARY KEY,
                email_address VARCHAR(255) NOT NULL,
                created_at TIMESTAMP
            );
        """.trimIndent()

        val identifiers = TextRecognitionHelper.extractIdentifiers(sampleText)

        // Must contain user-defined identifiers
        assertTrue(identifiers.contains("users"))
        assertTrue(identifiers.contains("email_address"))
        assertTrue(identifiers.contains("created_at"))

        // Tokens shorter than 3 chars (like 'id') are excluded by {2,} (meaning >= 3 chars)
        assertFalse(identifiers.contains("id"))
    }

    @Test
    fun testExtractIdentifiersHandlesEmptyAndShortTokens() {
        val identifiers = TextRecognitionHelper.extractIdentifiers("a b c 1 2 3")
        assertTrue(identifiers.isEmpty())
    }

    @Test
    fun testExtractIdentifiersDeduplicates() {
        val sample = "users users users orders"
        val identifiers = TextRecognitionHelper.extractIdentifiers(sample)
        assertEquals(2, identifiers.size)
        assertTrue(identifiers.contains("users"))
        assertTrue(identifiers.contains("orders"))
    }
}
