package com.schemalens.app.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportTraceParserTest {

    @Test
    fun testAliasedNamedImportResolvesCorrectly() {
        val code = """
            import { format as fmt, parseISO } from 'date-fns';

            function renderDate(d: string) {
                return fmt(parseISO(d), 'yyyy-MM-dd');
            }
        """.trimIndent()

        val identifiers = ImportTraceParser.extractImportedIdentifiers("date-fns", code.lines())
        assertTrue("Should contain alias fmt", identifiers.contains("fmt"))
        assertTrue("Should contain parseISO", identifiers.contains("parseISO"))
        assertFalse("Should NOT contain un-aliased format", identifiers.contains("format"))

        val sites = ImportTraceParser.tracePackageUsage("date-fns", code)
        assertEquals("Should find exactly 1 call site on the usage line", 1, sites.size)
        assertEquals(4, sites[0].lineNumber)
        assertTrue(sites[0].lineText.contains("fmt(parseISO(d)"))
    }

    @Test
    fun testDoesNotFalsePositiveOnImportDeclarationLine() {
        val code = """
            import { eq } from 'drizzle-orm';
            const x = 10;
        """.trimIndent()

        val sites = ImportTraceParser.tracePackageUsage("drizzle-orm", code)
        assertEquals("Should not match anything because eq is only on import line", 0, sites.size)
    }

    @Test
    fun testCommonJsDestructuredRequire() {
        val code = """
            const { findUser, select: sqlSelect } = require('my-db-orm');

            async function run() {
                const user = await sqlSelect('users');
                return findUser(user.id);
            }
        """.trimIndent()

        val identifiers = ImportTraceParser.extractImportedIdentifiers("my-db-orm", code.lines())
        assertTrue(identifiers.contains("findUser"))
        assertTrue(identifiers.contains("sqlSelect"))
        assertFalse(identifiers.contains("select"))

        val sites = ImportTraceParser.tracePackageUsage("my-db-orm", code)
        assertEquals(2, sites.size)
    }

    @Test
    fun testMultiFileSeparation() {
        val multiFileCode = """
            // === controllers/user.ts ===
            import { users } from 'my-orm';
            const a = users.find();

            // === services/auth.ts ===
            import { users as uTable } from 'my-orm';
            const b = uTable.query();
        """.trimIndent()

        val sites = ImportTraceParser.tracePackageUsage("my-orm", multiFileCode)
        assertEquals(2, sites.size)

        assertEquals("controllers/user.ts", sites[0].file)
        assertEquals(2, sites[0].lineNumber)
        assertEquals("users", sites[0].matchedIdentifier)

        assertEquals("services/auth.ts", sites[1].file)
        assertEquals(2, sites[1].lineNumber)
        assertEquals("uTable", sites[1].matchedIdentifier)
    }

    @Test
    fun testWordBoundaryPrecision() {
        val code = """
            import { eq } from 'drizzle-orm';
            const equalSign = "==";
            const requirement = "none";
            const actual = eq(a, b);
        """.trimIndent()

        val sites = ImportTraceParser.tracePackageUsage("drizzle-orm", code)
        assertEquals("Should only match exact word 'eq', not 'equalSign' or 'requirement'", 1, sites.size)
        assertEquals(4, sites[0].lineNumber)
    }
}
