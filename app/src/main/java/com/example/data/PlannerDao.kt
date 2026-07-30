package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlannerDao {
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettings(): Flow<UserSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserSettings(settings: UserSettings)

    @Query("SELECT * FROM goals ORDER BY id DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id")
    fun getGoalById(id: Int): Flow<Goal?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Int)

    @Query("SELECT * FROM contributions ORDER BY dateEpochMillis DESC")
    fun getAllContributions(): Flow<List<Contribution>>

    @Query("SELECT * FROM contributions WHERE goalId = :goalId ORDER BY dateEpochMillis DESC")
    fun getContributionsForGoal(goalId: Int): Flow<List<Contribution>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: Contribution): Long

    @Query("DELETE FROM contributions WHERE id = :id")
    suspend fun deleteContribution(id: Int)

    @Query("SELECT * FROM commitments ORDER BY id ASC")
    fun getAllCommitments(): Flow<List<Commitment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitment(commitment: Commitment): Long

    @Query("DELETE FROM commitments WHERE id = :id")
    suspend fun deleteCommitment(id: Int)

    @Query("SELECT * FROM monthly_records")
    fun getAllMonthlyRecords(): Flow<List<MonthlyRecord>>

    @Query("SELECT * FROM monthly_records WHERE monthYear = :monthYear")
    fun getMonthlyRecord(monthYear: String): Flow<MonthlyRecord?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMonthlyRecord(record: MonthlyRecord)

    @Query("DELETE FROM monthly_records")
    suspend fun deleteAllMonthlyRecords()

    @Query("DELETE FROM goals")
    suspend fun deleteAllGoals()

    @Query("DELETE FROM contributions")
    suspend fun deleteAllContributions()

    @Query("DELETE FROM commitments")
    suspend fun deleteAllCommitments()
}
