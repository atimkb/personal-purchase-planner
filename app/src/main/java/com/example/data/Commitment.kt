package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commitments")
data class Commitment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val monthlyAmount: Long, // in paise
    val category: String = "Subscriptions", // "Subscriptions", "Commitments", "Other"
    val createdAtEpochMillis: Long = System.currentTimeMillis()
)
