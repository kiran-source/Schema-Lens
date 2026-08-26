package com.schemalens.app.parser

import com.schemalens.app.data.CallSite

/**
 * Pure Kotlin parser that statically traces imported symbols and their call sites
 * within a multi-file code buffer.
 *
 * Runs 100% on-device with ZERO network calls (Red Light Zone).
 */
object ImportTraceParser {

    private val FILE_HEADER_REGEX = Regex("""^(?://|/\*|#)\s*===+\s*(.*?)\s*===+(?:\*/)?\s*$""")

    /**
     * Parses the code buffer to find all call sites referencing the specified package or ORM.
     *
     * @param targetPackage The package or ORM module name to trace (e.g. "drizzle-orm", "prisma", "@prisma/client", "date-fns").
     * @param codeBuffer The complete code buffer, optionally containing multiple files separated by `// === filename.ts ===`.
     * @return List of traced call sites with file, line number, line snippet, and matched identifier.
     */
    fun tracePackageUsage(targetPackage: String, codeBuffer: String): List<CallSite> {
        val cleanPkg = targetPackage.trim()
        if (cleanPkg.isEmpty() || codeBuffer.isBlank()) {
            return emptyList()
        }

        val lines = codeBuffer.lines()
        var currentFile: String? = null
        val fileBlocks = mutableListOf<FileBlock>()
        var currentLines = mutableListOf<Pair<Int, String>>() // global line index to content

        for ((index, rawLine) in lines.withIndex()) {
            val trimmed = rawLine.trim()
            val headerMatch = FILE_HEADER_REGEX.find(trimmed)
            if (headerMatch != null) {
                if (currentLines.isNotEmpty()) {
                    fileBlocks.add(FileBlock(currentFile, currentLines.toList()))
                    currentLines = mutableListOf()
                }
                currentFile = headerMatch.groupValues[1].trim()
            } else {
                currentLines.add(Pair(index + 1, rawLine))
            }
        }
        if (currentLines.isNotEmpty()) {
            fileBlocks.add(FileBlock(currentFile, currentLines.toList()))
        }

        val callSites = mutableListOf<CallSite>()
        var siteIndex = 0

        for (block in fileBlocks) {
            val importedIdentifiers = extractImportedIdentifiers(cleanPkg, block.lines.map { it.second })

            // If no explicit imports were found for the package in this file block,
            // fallback to checking direct occurrences of the package name (or clean base name)
            val identifiersToTrace = if (importedIdentifiers.isNotEmpty()) {
                importedIdentifiers
            } else {
                setOf(cleanPkg.substringAfterLast('/'))
            }

            // Create word boundary regex patterns for each identifier
            val patterns = identifiersToTrace.filter { it.isNotBlank() }.map { id ->
                id to Regex("""\b${Regex.escape(id)}\b""")
            }

            var fileLineNumber = 1
            for ((globalLineNum, lineContent) in block.lines) {
                val effectiveLineNum = if (block.fileName != null) fileLineNumber else globalLineNum
                fileLineNumber++

                // Skip the import/require declaration lines themselves to avoid false positives
                if (isImportLineForPackage(cleanPkg, lineContent)) {
                    continue
                }

                // Check if any tracked identifier occurs on this line
                for ((id, pattern) in patterns) {
                    if (pattern.containsMatchIn(lineContent)) {
                        callSites.add(
                            CallSite(
                                index = siteIndex++,
                                file = block.fileName,
                                lineNumber = effectiveLineNum,
                                lineText = lineContent.trim(),
                                matchedIdentifier = id
                            )
                        )
                        break // Avoid duplicate entries for the same line if multiple identifiers match
                    }
                }
            }
        }

        return callSites
    }

    /**
     * Extracts locally bound identifiers imported from the target package.
     */
    fun extractImportedIdentifiers(targetPackage: String, lines: List<String>): Set<String> {
        val identifiers = mutableSetOf<String>()
        val pkgEscaped = Regex.escape(targetPackage)

        // 1. ES6 Named imports: import { a, b as c, d as e } from 'pkg'
        // Supports multiline import statements as well when combined
        val namedImportRegex = Regex("""import\s+(?:type\s+)?\{([^}]+)\}\s+from\s+['"]$pkgEscaped['"]""")

        // 2. ES6 Default import: import defaultName from 'pkg'
        val defaultImportRegex = Regex("""import\s+([A-Za-z0-9_$]+)\s+from\s+['"]$pkgEscaped['"]""")

        // 3. ES6 Namespace import: import * as namespaceName from 'pkg'
        val namespaceImportRegex = Regex("""import\s+\*\s+as\s+([A-Za-z0-9_$]+)\s+from\s+['"]$pkgEscaped['"]""")

        // 4. Combined default + named: import defaultName, { a, b as c } from 'pkg'
        val combinedImportRegex = Regex("""import\s+([A-Za-z0-9_$]+)\s*,\s*\{([^}]+)\}\s+from\s+['"]$pkgEscaped['"]""")

        // 5. CommonJS default: const x = require('pkg') or let/var
        val requireDefaultRegex = Regex("""(?:const|let|var)\s+([A-Za-z0-9_$]+)\s*=\s*require\(\s*['"]$pkgEscaped['"]\s*\)""")

        // 6. CommonJS destructured: const { a, b: c } = require('pkg')
        val requireDestructuredRegex = Regex("""(?:const|let|var)\s+\{([^}]+)\}\s*=\s*require\(\s*['"]$pkgEscaped['"]\s*\)""")

        // Iterate line by line
        for (line in lines) {
            val trimmed = line.trim()

            // Check Combined import
            combinedImportRegex.find(trimmed)?.let { match ->
                val def = match.groupValues[1].trim()
                if (def.isNotBlank()) identifiers.add(def)
                val namedGroup = match.groupValues[2]
                parseNamedImports(namedGroup, identifiers)
                return@let
            }

            // Check Named import
            namedImportRegex.find(trimmed)?.let { match ->
                val namedGroup = match.groupValues[1]
                parseNamedImports(namedGroup, identifiers)
            }

            // Check Namespace import
            namespaceImportRegex.find(trimmed)?.let { match ->
                val ns = match.groupValues[1].trim()
                if (ns.isNotBlank()) identifiers.add(ns)
            }

            // Check Default import
            defaultImportRegex.find(trimmed)?.let { match ->
                val def = match.groupValues[1].trim()
                if (def.isNotBlank() && def != "type") identifiers.add(def)
            }

            // Check CommonJS require default
            requireDefaultRegex.find(trimmed)?.let { match ->
                val req = match.groupValues[1].trim()
                if (req.isNotBlank()) identifiers.add(req)
            }

            // Check CommonJS require destructured
            requireDestructuredRegex.find(trimmed)?.let { match ->
                val destructuredGroup = match.groupValues[1]
                parseCommonJsDestructuring(destructuredGroup, identifiers)
            }
        }

        return identifiers
    }

    private fun parseNamedImports(namedGroup: String, outSet: MutableSet<String>) {
        // e.g. "format as fmt, parseISO, type User"
        val parts = namedGroup.split(',')
        for (part in parts) {
            val item = part.trim().removePrefix("type ").trim()
            if (item.isEmpty()) continue
            if (item.contains(" as ")) {
                val alias = item.substringAfter(" as ").trim()
                if (alias.isNotEmpty()) outSet.add(alias)
            } else {
                outSet.add(item)
            }
        }
    }

    private fun parseCommonJsDestructuring(group: String, outSet: MutableSet<String>) {
        // e.g. "findUser, select: sqlSelect"
        val parts = group.split(',')
        for (part in parts) {
            val item = part.trim()
            if (item.isEmpty()) continue
            if (item.contains(':')) {
                val alias = item.substringAfter(':').trim()
                if (alias.isNotEmpty()) outSet.add(alias)
            } else {
                outSet.add(item)
            }
        }
    }

    private fun isImportLineForPackage(targetPackage: String, line: String): Boolean {
        val trimmed = line.trim()
        val pkgEscaped = Regex.escape(targetPackage)
        return (trimmed.startsWith("import ") || trimmed.startsWith("import type ")) &&
                Regex("""from\s+['"]$pkgEscaped['"]""").containsMatchIn(trimmed) ||
                Regex("""require\(\s*['"]$pkgEscaped['"]\s*\)""").containsMatchIn(trimmed)
    }

    private data class FileBlock(
        val fileName: String?,
        val lines: List<Pair<Int, String>> // LineNumber to LineContent
    )
}
