package com.schemalens.app.bridge

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.schemalens.app.data.CallSite
import com.schemalens.app.data.Dialect

/**
 * Dual-Screen "Live Hot-Patching Companion" for Office Kit integration.
 *
 * Creates structured clipboard payloads with metadata headers so that
 * paired desktop tools (VS Code, Android Studio, Office Kit clipboard bridge)
 * can parse and apply patches directly into the active editor.
 *
 * Runs 100% on-device with zero network dependency.
 */
object HotPatchCompanion {

    private const val MIME_PATCH = "text/x-patch"
    private const val CLIP_LABEL = "schemalens/hot-patch"

    /**
     * Builds a structured hot-patch payload with metadata headers.
     *
     * Format:
     * ```
     * --- SCHEMALENS HOT-PATCH v2.0 ---
     * Target-Dialect: Drizzle (TypeScript)
     * Target-File: schema.ts
     * Line-Range: 12-45
     * Risk-Score: 72
     * Timestamp: 2026-09-17T15:30:00Z
     * ---
     * <patch code>
     * ```
     */
    fun buildStructuredPayload(
        patchCode: String,
        dialect: Dialect,
        riskScore: Int? = null,
        targetFile: String? = null,
        lineRange: String? = null
    ): String {
        val timestamp = java.time.Instant.now().toString()
        val sb = StringBuilder()
        sb.appendLine("--- SCHEMALENS HOT-PATCH v2.0 ---")
        sb.appendLine("Target-Dialect: ${dialect.displayName}")
        targetFile?.let { sb.appendLine("Target-File: $it") }
        lineRange?.let { sb.appendLine("Line-Range: $it") }
        riskScore?.let { sb.appendLine("Risk-Score: $it") }
        sb.appendLine("Timestamp: $timestamp")
        sb.appendLine("---")
        sb.appendLine()
        sb.append(patchCode)
        return sb.toString()
    }

    /**
     * Builds a per-call-site hot-patch payload for line-level patching.
     */
    fun buildCallSitePatch(
        site: CallSite,
        remediation: String,
        dialect: Dialect
    ): String {
        val sb = StringBuilder()
        sb.appendLine("--- SCHEMALENS LINE-PATCH v2.0 ---")
        sb.appendLine("Target-Dialect: ${dialect.displayName}")
        site.file?.let { sb.appendLine("Target-File: $it") }
        sb.appendLine("Target-Line: ${site.lineNumber}")
        sb.appendLine("Matched-Symbol: ${site.matchedIdentifier}")
        sb.appendLine("Verdict: ${site.verdict?.sev?.label ?: "PENDING"}")
        sb.appendLine("---")
        sb.appendLine("// Original: ${site.lineText.trim()}")
        sb.appendLine("// Remediation:")
        sb.appendLine(remediation)
        return sb.toString()
    }

    /**
     * Copies a structured hot-patch payload to the system clipboard
     * with a custom label for Office Kit bridge identification.
     */
    fun copyToClipboard(context: Context, payload: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(CLIP_LABEL, payload)
        clipboard.setPrimaryClip(clip)
    }

    /**
     * Fires an ACTION_SEND intent with text/x-patch MIME type so that
     * paired desktop tools or Office Kit screen mirroring can intercept
     * the structured patch stream.
     */
    fun shareToIde(context: Context, payload: String, dialect: Dialect) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = MIME_PATCH
            putExtra(Intent.EXTRA_SUBJECT, "schemalens_hotpatch.${dialect.fileExtension}")
            putExtra(Intent.EXTRA_TEXT, payload)
            putExtra(Intent.EXTRA_TITLE, "SchemaLens Hot-Patch → IDE")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(sendIntent, "⚡ Stream Hot-Patch to IDE")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
