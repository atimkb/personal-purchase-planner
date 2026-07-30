package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetPrice: Double,
    val targetDateEpochMillis: Long,
    val alreadySavedAmount: Double = 0.0,
    val category: String = "Other", // "Kitchen", "Electronics", "Vehicle", "Travel", "Fashion", "Home", "Other"
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val expectedReturnRate: Double = 8.0, // annual return %
    val status: String = "ACTIVE", // "ACTIVE", "PAUSED", "COMPLETED", "CANCELLED"
    val currentManualValue: Double? = null,
    val completedDateEpochMillis: Long? = null,
    val finalPurchasePrice: Double? = null,
    val iconName: String = "default",
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
