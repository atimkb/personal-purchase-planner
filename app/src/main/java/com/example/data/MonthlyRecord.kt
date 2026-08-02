package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_records")
data class MonthlyRecord(
    @PrimaryKey val monthYear: String, // e.g. "July 2026", "August 2026"
    val monthlyIncome: Long? = null, // in paise
    val note: String? = null
)
