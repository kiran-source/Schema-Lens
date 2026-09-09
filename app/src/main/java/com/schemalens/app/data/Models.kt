package com.schemalens.app.data

/**
 * Represents a code call site where a tracked package or its imported identifier is invoked.
 */
data class CallSite(
    val index: Int,
    val file: String?,
    val lineNumber: Int,
    val lineText: String,
    val matchedIdentifier: String,
    val verdict: SiteVerdict? = null
)

/**
 * Risk severity verdict for a call site.
 */
enum class RiskSeverity(val code: String, val label: String) {
    SAFE("green", "SAFE"),
    RISKY("amber", "RISKY"),
    BREAKING("red", "BREAKING"),
    PENDING("grey", "PENDING");

    companion object {
        fun fromCode(code: String?): RiskSeverity {
            return when (code?.lowercase()?.trim()) {
                "green", "safe" -> SAFE
                "amber", "yellow", "risky", "warning" -> RISKY
                "red", "breaking", "error", "danger" -> BREAKING
                else -> PENDING
            }
        }
    }
}

/**
 * Individual verdict per call site returned from Cloud Assessment.
 */
data class SiteVerdict(
    val index: Int,
    val sev: RiskSeverity,
    val note: String
)

/**
 * Complete assessment response from Claude Opus 4.
 */
data class AssessmentResult(
    val overallScore: Int,
    val summary: String,
    val sites: List<SiteVerdict>,
    val ormPatch: String
)

/**
 * Represents a parsed schema entity (table or model) for ER Graph visualization.
 */
data class SchemaEntity(
    val tableName: String,
    val columns: List<String> = emptyList(),
    val foreignKeys: List<ForeignKeyRelation> = emptyList()
)

/**
 * Foreign key relationship between tables for ER link rendering.
 */
data class ForeignKeyRelation(
    val fromColumn: String,
    val targetTable: String,
    val targetColumn: String
)

/**
 * Output of on-device ML Kit OCR schema extraction.
 */
data class OcrExtractionResult(
    val rawText: String,
    val identifiers: List<String>
)

/**
 * Explicit Hackathon Phase Zones.
 */
enum class AppZone(val title: String, val badgeText: String, val isNetworkAllowed: Boolean) {
    RED_LIGHT("Red Light Zone", "🔒 on-device · no network", false),
    GREEN_LIGHT("Green Light Zone", "☁️ cloud reasoning", true)
}

// ─── New v2.0 Data Classes ─────────────────────────────────────────────

/**
 * Bottom navigation tab destinations.
 */
enum class AppTab(val route: String, val title: String, val icon: String) {
    SCHEMA("schema", "Schema", "schema"),
    TRACE("trace", "Trace", "trace"),
    ASSESS("assess", "Assess", "assess"),
    EXPORT("export", "Export", "export")
}

/**
 * A saved assessment in history for later review.
 */
data class AssessmentHistoryEntry(
    val id: String = System.currentTimeMillis().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val packageName: String,
    val overallScore: Int,
    val summary: String,
    val sitesCount: Int,
    val breakingCount: Int,
    val riskyCount: Int,
    val safeCount: Int,
    val ormPatch: String
)

/**
 * Migration step for the timeline visualization.
 */
data class MigrationStep(
    val title: String,
    val description: String,
    val status: MigrationStepStatus,
    val timestamp: Long? = null
)

enum class MigrationStepStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

/**
 * Schema diff entry showing before/after column changes.
 */
data class SchemaDiffEntry(
    val columnName: String,
    val changeType: DiffChangeType,
    val oldValue: String? = null,
    val newValue: String? = null
)

enum class DiffChangeType {
    ADDED,
    REMOVED,
    RENAMED,
    TYPE_CHANGED,
    UNCHANGED
}
