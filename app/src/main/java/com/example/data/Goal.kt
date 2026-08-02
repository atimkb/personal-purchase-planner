package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetPrice: Long, // in paise
    val targetDateEpochMillis: Long,
    val alreadySavedAmount: Long = 0L, // in paise
    val category: String = "Other", // "Kitchen", "Electronics", "Vehicle", "Travel", "Fashion", "Home", "Other"
    val priority: String = "MEDIUM", // "HIGH", "MEDIUM", "LOW"
    val expectedReturnRate: Long = 8L, // annual return % as integer
    val status: String = "ACTIVE", // "ACTIVE", "PAUSED", "COMPLETED", "CANCELLED"
    val completedDateEpochMillis: Long? = null,
    val finalPurchasePrice: Long? = null, // in paise
    val iconName: String = "default",
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
