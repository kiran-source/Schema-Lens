package com.schemalens.app.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SchemaParserTest {

    @Test
    fun testSqlCreateTableAndForeignKeyParsing() {
        val ddl = """
            CREATE TABLE users (
                id VARCHAR(36) PRIMARY KEY,
                email VARCHAR(255) NOT NULL
            );

            CREATE TABLE orders (
                id VARCHAR(36) PRIMARY KEY,
                user_id VARCHAR(36) NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id)
            );
        """.trimIndent()

        val entities = SchemaParser.parseSchema(ddl)
        assertEquals(2, entities.size)

        val usersTable = entities.find { it.tableName == "users" }
        assertTrue(usersTable != null)
        assertTrue(usersTable!!.columns.contains("email"))

        val ordersTable = entities.find { it.tableName == "orders" }
        assertTrue(ordersTable != null)
        assertEquals(1, ordersTable!!.foreignKeys.size)
        assertEquals("user_id", ordersTable.foreignKeys[0].fromColumn)
        assertEquals("users", ordersTable.foreignKeys[0].targetTable)
        assertEquals("id", ordersTable.foreignKeys[0].targetColumn)
    }

    @Test
    fun testPrismaModelParsing() {
        val prisma = """
            model User {
                id String @id
                email String
                posts Post[]
            }

            model Post {
                id String @id
                authorId String
                author User @relation(fields: [authorId], references: [id])
            }
        """.trimIndent()

        val entities = SchemaParser.parseSchema(prisma)
        assertEquals(2, entities.size)
        val post = entities.find { it.tableName == "Post" }
        assertTrue(post != null)
        assertTrue(post!!.foreignKeys.isNotEmpty())
    }
}
