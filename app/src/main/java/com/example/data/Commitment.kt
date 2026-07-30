package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "commitments")
data class Commitment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val monthlyAmount: Double,
    val category: String = "Subscriptions" // "Subscriptions", "Commitments", "Other"
)
