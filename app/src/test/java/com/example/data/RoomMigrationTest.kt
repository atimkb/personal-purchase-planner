package com.example.data

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoomMigrationTest {

    @Test
    fun testAllMigrationsFrom1To4() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "migration_test.db"

        // Delete any existing test database
        context.getDatabasePath(dbName).delete()

        // 1. Create a version 1 database using SQLiteOpenHelper with Version 1 schema and Double values
        val config = SupportSQLiteOpenHelper.Configuration.builder(context)
            .name(dbName)
            .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `user_settings` (
                            `id` INTEGER PRIMARY KEY NOT NULL, 
                            `userName` TEXT NOT NULL, 
                            `monthlyIncome` REAL NOT NULL, 
                            `currencySymbol` TEXT NOT NULL, 
                            `themeMode` TEXT NOT NULL, 
                            `allocationLimitWarningEnabled` INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `goals` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                            `name` TEXT NOT NULL, 
                            `targetPrice` REAL NOT NULL, 
                            `targetDateEpochMillis` INTEGER NOT NULL, 
                            `alreadySavedAmount` REAL NOT NULL, 
                            `category` TEXT NOT NULL, 
                            `priority` TEXT NOT NULL, 
                            `expectedReturnRate` REAL NOT NULL, 
                            `status` TEXT NOT NULL, 
                            `completedDateEpochMillis` INTEGER, 
                            `finalPurchasePrice` REAL, 
                            `iconName` TEXT NOT NULL, 
                            `createdAtEpochMillis` INTEGER NOT NULL
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `contributions` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                            `goalId` INTEGER NOT NULL, 
                            `amount` REAL NOT NULL, 
                            `dateEpochMillis` INTEGER NOT NULL, 
                            `investmentType` TEXT NOT NULL, 
                            `type` TEXT NOT NULL, 
                            `note` TEXT, 
                            FOREIGN KEY(`goalId`) REFERENCES `goals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                        )
                        """.trimIndent()
                    )
                    db.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS `commitments` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                            `name` TEXT NOT NULL, 
                            `monthlyAmount` REAL NOT NULL, 
                            `category` TEXT NOT NULL
                        )
                        """.trimIndent()
                    )

                    // Insert sample data with original Double-based values
                    db.execSQL("INSERT INTO `user_settings` VALUES (1, 'Amit', 50000.0, '₹', 'SYSTEM', 1)")
                    db.execSQL("INSERT INTO `goals` VALUES (1, 'New Car', 1200000.0, 1750000000000, 150000.0, 'Vehicle', 'HIGH', 10.0, 'ACTIVE', NULL, NULL, 'car', 1700000000000)")
                    db.execSQL("INSERT INTO `contributions` VALUES (101, 1, 25000.0, 1705000000000, 'SIP', 'SAVINGS', 'First SIP')")
                    db.execSQL("INSERT INTO `commitments` VALUES (201, 'Gym Subscription', 1500.0, 'Subscriptions')")
                }

                override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
            })
            .build()

        val openHelper = FrameworkSQLiteOpenHelperFactory().create(config)
        val v1Db = openHelper.writableDatabase
        v1Db.close()

        // 2. Open database via Room at version 4 with registered migrations (Room will run MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        val roomDb = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()

        val dao = roomDb.plannerDao()

        // 4. Assert all data survived intact with correctly converted paise values (Long)
        val userSettings = dao.getUserSettings().first()
        assertNotNull(userSettings)
        assertEquals("Amit", userSettings?.userName)
        assertEquals(5000000L, userSettings?.monthlyIncome) // ₹50,000 -> 5,000,000 paise

        val goal = dao.getGoalById(1).first()
        assertNotNull(goal)
        assertEquals("New Car", goal?.name)
        assertEquals(120000000L, goal?.targetPrice) // ₹12,00,000 -> 120,000,000 paise
        assertEquals(15000000L, goal?.alreadySavedAmount) // ₹1,50,000 -> 15,000,000 paise

        val contributions = dao.getContributionsForGoal(1).first()
        assertEquals(1, contributions.size)
        assertEquals(2500000L, contributions[0].amount) // ₹25,000 -> 2,500,000 paise

        val commitments = dao.getAllCommitments().first()
        assertEquals(1, commitments.size)
        assertEquals(150000L, commitments[0].monthlyAmount) // ₹1,500 -> 150,000 paise

        println("MIGRATION_TEST_SUCCESS: All data survived intact and amounts converted to paise correctly!")
        println("  UserSettings.monthlyIncome: 50000.0 -> ${userSettings?.monthlyIncome} paise")
        println("  Goal.targetPrice: 1200000.0 -> ${goal?.targetPrice} paise")
        println("  Goal.alreadySavedAmount: 150000.0 -> ${goal?.alreadySavedAmount} paise")
        println("  Contribution.amount: 25000.0 -> ${contributions[0].amount} paise")
        println("  Commitment.monthlyAmount: 1500.0 -> ${commitments[0].monthlyAmount} paise")

        roomDb.close()
    }
}
