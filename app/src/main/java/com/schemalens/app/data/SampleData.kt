package com.schemalens.app.data

/**
 * Pre-configured production test cases matching the "AirTrace DB" problem statement,
 * allowing instant live demonstration of SchemaLens capabilities.
 */
object SampleData {

    const val DEFAULT_PACKAGE: String = "drizzle-orm"

    const val DEFAULT_CHANGE_NOTES: String =
        "Renaming `users.hashed_password` to `users.password_hash` and dropping the deprecated `role` column in favor of a new `roles` permission bitmask table."

    val DEFAULT_CODE_BUFFER: String = """
// === src/controllers/auth.controller.ts ===
import { eq, and } from 'drizzle-orm';
import { db } from '../db/client';
import { users } from '../db/schema';

export async function loginUser(email: string, passAttempt: string) {
  const user = await db.select().from(users).where(eq(users.email, email)).limit(1);
  if (!user || user.length === 0) return null;

  const valid = verifyPassword(passAttempt, user[0].hashed_password);
  if (!valid) throw new Error("Invalid credentials");

  if (user[0].role === 'admin') {
    return { token: signJwt(user[0].id), isAdmin: true };
  }
  return { token: signJwt(user[0].id), isAdmin: false };
}

// === src/services/user.service.ts ===
import { users, auditLogs } from '../db/schema';
import { db } from '../db/client';
import { eq } from 'drizzle-orm';

export async function updateUserRole(userId: string, newRole: string) {
  await db.update(users)
    .set({ role: newRole, updatedAt: new Date() })
    .where(eq(users.id, userId));

  await db.insert(auditLogs).values({
    userId,
    action: "Changed role"
  });
}

// === src/api/middleware/roleGuard.ts ===
import { eq as matchEq } from 'drizzle-orm';
import { db } from '../../db/client';
import { users } from '../../db/schema';

export async function requireAdmin(userId: string) {
  const record = await db.select().from(users).where(matchEq(users.id, userId));
  return record[0]?.role === 'admin';
}
""".trimIndent()

    val DEFAULT_SCHEMA_DDL: String = """
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    hashed_password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    action TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
""".trimIndent()
}
