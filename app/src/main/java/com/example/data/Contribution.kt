package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "contributions",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["goalId"])]
)
data class Contribution(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val goalId: Int,
    val amount: Long, // in paise
    val dateEpochMillis: Long = System.currentTimeMillis(),
    val investmentType: String = "Savings", // "Savings", "Mutual Fund", "Stock", "FD / RD", "Other"
    val type: String = "CONTRIBUTION", // "CONTRIBUTION", "WITHDRAWAL", "VALUE_UPDATE"
    val note: String? = null
)
