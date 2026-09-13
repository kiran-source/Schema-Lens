package com.schemalens.app.data

/**
 * Target database / ORM dialects supported by SchemaLens Studio.
 */
enum class Dialect(
    val displayName: String,
    val fileExtension: String,
    val badgeLabel: String
) {
    DRIZZLE("Drizzle (TypeScript)", ".ts", "TS / Node"),
    ANDROID_ROOM("Android Room (Kotlin)", ".kt", "Room / Android"),
    PRISMA("Prisma (Schema)", ".prisma", "Prisma"),
    RAW_SQL("SQL (Up & Down)", ".sql", "PostgreSQL DDL")
}
