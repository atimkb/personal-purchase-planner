package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Int = 1,
    val userName: String = "Amit",
    val monthlyIncome: Double = 50000.0,
    val currencySymbol: String = "₹",
    val themeMode: String = "SYSTEM", // "LIGHT", "DARK", "SYSTEM"
    val allocationLimitWarningEnabled: Boolean = true
)
