package com.aistudio.qrgenerator.kmpzqr.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private lateinit var context: Context
    private val databaseName = "quickqr_migration_test.db"

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(databaseName)
    }

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migrate1To2_preservesHistoryAndMerchantProfile_andAddsIndexes() {
        createVersion1DatabaseWithData()

        val room = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .allowMainThreadQueries()
            .build()

        try {
            val db = room.openHelper.writableDatabase

            assertEquals(1, scalarCount(db, "SELECT COUNT(*) FROM qr_items"))
            assertEquals(1, scalarCount(db, "SELECT COUNT(*) FROM merchant_profiles"))
            assertEquals(
                "saved-payload",
                scalarText(db, "SELECT rawContent FROM qr_items WHERE id = 1")
            )
            assertEquals(
                "Saved Shop",
                scalarText(db, "SELECT businessName FROM merchant_profiles WHERE id = 1")
            )

            val indexes = indexNames(db)
            assertTrue(indexes.contains("index_qr_items_timestamp"))
            assertTrue(indexes.contains("index_qr_items_isScan_timestamp"))
        } finally {
            room.close()
        }
    }

    private fun createVersion1DatabaseWithData() {
        val db = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null)
        try {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS qr_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    type TEXT NOT NULL,
                    title TEXT NOT NULL,
                    subtitle TEXT NOT NULL,
                    rawContent TEXT NOT NULL,
                    targetId TEXT,
                    amount REAL,
                    timestamp INTEGER NOT NULL,
                    isScan INTEGER NOT NULL,
                    isFavorite INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS merchant_profiles (
                    id INTEGER NOT NULL PRIMARY KEY,
                    fullName TEXT NOT NULL,
                    businessName TEXT NOT NULL,
                    profession TEXT NOT NULL,
                    phoneNumber TEXT NOT NULL,
                    promptPayId TEXT NOT NULL,
                    lineId TEXT NOT NULL,
                    facebook TEXT NOT NULL,
                    email TEXT NOT NULL,
                    services TEXT NOT NULL,
                    cardTheme TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO qr_items (
                    id, type, title, subtitle, rawContent, targetId, amount, timestamp, isScan, isFavorite
                ) VALUES (
                    1, 'TEXT', 'Saved', 'Preserve me', 'saved-payload', NULL, NULL, 1000, 0, 0
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT INTO merchant_profiles (
                    id, fullName, businessName, profession, phoneNumber, promptPayId,
                    lineId, facebook, email, services, cardTheme
                ) VALUES (
                    1, 'Owner', 'Saved Shop', 'Merchant', '0812345678', '',
                    '', '', 'owner@example.test', '', 'NAVY_BLUE'
                )
                """.trimIndent()
            )
            db.execSQL("PRAGMA user_version = 1")
        } finally {
            db.close()
        }
    }

    private fun scalarCount(db: androidx.sqlite.db.SupportSQLiteDatabase, sql: String): Int =
        db.query(sql).use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getInt(0)
        }

    private fun scalarText(db: androidx.sqlite.db.SupportSQLiteDatabase, sql: String): String =
        db.query(sql).use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getString(0)
        }

    private fun indexNames(db: androidx.sqlite.db.SupportSQLiteDatabase): Set<String> =
        db.query("PRAGMA index_list('qr_items')").use { cursor ->
            val nameIndex = cursor.getColumnIndexOrThrow("name")
            buildSet {
                while (cursor.moveToNext()) {
                    add(cursor.getString(nameIndex))
                }
            }
        }
}
