package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `goals_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetPrice` INTEGER NOT NULL, `targetDateEpochMillis` INTEGER NOT NULL, `alreadySavedAmount` INTEGER NOT NULL, `category` TEXT NOT NULL, `priority` TEXT NOT NULL, `expectedReturnRate` INTEGER NOT NULL, `status` TEXT NOT NULL, `currentManualValue` INTEGER, `completedDateEpochMillis` INTEGER, `finalPurchasePrice` INTEGER, `iconName` TEXT NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL)")
        db.execSQL("INSERT INTO `goals_new` SELECT `id`, `name`, CAST(`targetPrice` * 100 AS INTEGER), `targetDateEpochMillis`, CAST(`alreadySavedAmount` * 100 AS INTEGER), `category`, `priority`, CAST(`expectedReturnRate` AS INTEGER), `status`, CASE WHEN `currentManualValue` IS NOT NULL THEN CAST(`currentManualValue` * 100 AS INTEGER) ELSE NULL END, `completedDateEpochMillis`, CASE WHEN `finalPurchasePrice` IS NOT NULL THEN CAST(`finalPurchasePrice` * 100 AS INTEGER) ELSE NULL END, `iconName`, `createdAtEpochMillis` FROM `goals`")
        db.execSQL("DROP TABLE `goals`")
        db.execSQL("ALTER TABLE `goals_new` RENAME TO `goals`")

        db.execSQL("CREATE TABLE IF NOT EXISTS `contributions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `goalId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `dateEpochMillis` INTEGER NOT NULL, `investmentType` TEXT NOT NULL, `type` TEXT NOT NULL, `note` TEXT, FOREIGN KEY(`goalId`) REFERENCES `goals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contributions_goalId` ON `contributions_new` (`goalId`)")
        db.execSQL("INSERT INTO `contributions_new` SELECT `id`, `goalId`, CAST(`amount` * 100 AS INTEGER), `dateEpochMillis`, `investmentType`, `type`, `note` FROM `contributions`")
        db.execSQL("DROP TABLE `contributions`")
        db.execSQL("ALTER TABLE `contributions_new` RENAME TO `contributions`")

        db.execSQL("CREATE TABLE IF NOT EXISTS `commitments_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `monthlyAmount` INTEGER NOT NULL, `category` TEXT NOT NULL)")
        db.execSQL("INSERT INTO `commitments_new` SELECT `id`, `name`, CAST(`monthlyAmount` * 100 AS INTEGER), `category` FROM `commitments`")
        db.execSQL("DROP TABLE `commitments`")
        db.execSQL("ALTER TABLE `commitments_new` RENAME TO `commitments`")

        db.execSQL("CREATE TABLE IF NOT EXISTS `user_settings_new` (`id` INTEGER PRIMARY KEY NOT NULL, `userName` TEXT NOT NULL, `monthlyIncome` INTEGER NOT NULL, `currencySymbol` TEXT NOT NULL, `themeMode` TEXT NOT NULL, `allocationLimitWarningEnabled` INTEGER NOT NULL)")
        db.execSQL("INSERT INTO `user_settings_new` SELECT `id`, `userName`, CAST(`monthlyIncome` * 100 AS INTEGER), `currencySymbol`, `themeMode`, `allocationLimitWarningEnabled` FROM `user_settings`")
        db.execSQL("DROP TABLE `user_settings`")
        db.execSQL("ALTER TABLE `user_settings_new` RENAME TO `user_settings`")

        db.execSQL("CREATE TABLE IF NOT EXISTS `monthly_records_new` (`monthYear` TEXT PRIMARY KEY NOT NULL, `monthlyIncome` INTEGER, `note` TEXT)")
        db.execSQL("INSERT INTO `monthly_records_new` SELECT `monthYear`, CASE WHEN `monthlyIncome` IS NOT NULL THEN CAST(`monthlyIncome` * 100 AS INTEGER) ELSE NULL END, `note` FROM `monthly_records`")
        db.execSQL("DROP TABLE `monthly_records`")
        db.execSQL("ALTER TABLE `monthly_records_new` RENAME TO `monthly_records`")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `goals_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetPrice` INTEGER NOT NULL, `targetDateEpochMillis` INTEGER NOT NULL, `alreadySavedAmount` INTEGER NOT NULL, `category` TEXT NOT NULL, `priority` TEXT NOT NULL, `expectedReturnRate` INTEGER NOT NULL, `status` TEXT NOT NULL, `completedDateEpochMillis` INTEGER, `finalPurchasePrice` INTEGER, `iconName` TEXT NOT NULL, `createdAtEpochMillis` INTEGER NOT NULL)")
        db.execSQL("INSERT INTO `goals_new` SELECT `id`, `name`, `targetPrice`, `targetDateEpochMillis`, `alreadySavedAmount`, `category`, `priority`, `expectedReturnRate`, `status`, `completedDateEpochMillis`, `finalPurchasePrice`, `iconName`, `createdAtEpochMillis` FROM `goals`")
        db.execSQL("DROP TABLE `goals`")
        db.execSQL("ALTER TABLE `goals_new` RENAME TO `goals`")

        db.execSQL("CREATE TABLE IF NOT EXISTS `contributions_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `goalId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `dateEpochMillis` INTEGER NOT NULL, `investmentType` TEXT NOT NULL, `type` TEXT NOT NULL, `note` TEXT, FOREIGN KEY(`goalId`) REFERENCES `goals`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_contributions_goalId` ON `contributions_new` (`goalId`)")
        db.execSQL("INSERT INTO `contributions_new` SELECT `id`, `goalId`, `amount`, `dateEpochMillis`, `investmentType`, `type`, `note` FROM `contributions`")
        db.execSQL("DROP TABLE `contributions`")
        db.execSQL("ALTER TABLE `contributions_new` RENAME TO `contributions`")
    }
}

@Database(
    entities = [UserSettings::class, Goal::class, Contribution::class, Commitment::class, MonthlyRecord::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun plannerDao(): PlannerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "purchase_planner.db"
                )
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
