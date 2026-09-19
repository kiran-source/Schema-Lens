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
    product_id VARCHAR(36) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) DEFAULT 'pending',
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE products (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(50) UNIQUE,
    price DECIMAL(10, 2) NOT NULL,
    stock_count INT DEFAULT 0
);

CREATE TABLE audit_logs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    action TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
""".trimIndent()

    val SAAS_AUTH_SCHEMA_DDL: String = """
CREATE TABLE organizations (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(100) UNIQUE NOT NULL,
    plan VARCHAR(50) DEFAULT 'starter',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    org_id VARCHAR(36) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    mfa_enabled BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (org_id) REFERENCES organizations(id)
);

CREATE TABLE api_keys (
    id VARCHAR(36) PRIMARY KEY,
    org_id VARCHAR(36) NOT NULL,
    key_hash VARCHAR(255) NOT NULL,
    scopes TEXT DEFAULT 'read',
    expires_at TIMESTAMP,
    FOREIGN KEY (org_id) REFERENCES organizations(id)
);
""".trimIndent()

    val FINTECH_LEDGER_SCHEMA_DDL: String = """
CREATE TABLE accounts (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    account_number VARCHAR(34) UNIQUE NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',
    balance DECIMAL(18, 4) DEFAULT 0.0000,
    status VARCHAR(20) DEFAULT 'active'
);

CREATE TABLE ledger_entries (
    id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    amount DECIMAL(18, 4) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    reference_id VARCHAR(64) UNIQUE,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

CREATE TABLE compliance_checks (
    id VARCHAR(36) PRIMARY KEY,
    account_id VARCHAR(36) NOT NULL,
    risk_level VARCHAR(20) DEFAULT 'low',
    verified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);
""".trimIndent()
}

