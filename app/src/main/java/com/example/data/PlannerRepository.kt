package com.example.data

import kotlinx.coroutines.flow.Flow

class PlannerRepository(private val dao: PlannerDao) {

    val userSettings: Flow<UserSettings?> = dao.getUserSettings()
    val allGoals: Flow<List<Goal>> = dao.getAllGoals()
    val allContributions: Flow<List<Contribution>> = dao.getAllContributions()
    val allCommitments: Flow<List<Commitment>> = dao.getAllCommitments()
    val allMonthlyRecords: Flow<List<MonthlyRecord>> = dao.getAllMonthlyRecords()

    fun getGoalById(id: Int): Flow<Goal?> = dao.getGoalById(id)
    fun getContributionsForGoal(goalId: Int): Flow<List<Contribution>> = dao.getContributionsForGoal(goalId)
    fun getMonthlyRecord(monthYear: String): Flow<MonthlyRecord?> = dao.getMonthlyRecord(monthYear)

    suspend fun upsertUserSettings(settings: UserSettings) = dao.upsertUserSettings(settings)
    suspend fun upsertMonthlyRecord(record: MonthlyRecord) = dao.upsertMonthlyRecord(record)

    suspend fun insertGoal(goal: Goal): Long = dao.insertGoal(goal)
    suspend fun updateGoal(goal: Goal) = dao.updateGoal(goal)
    suspend fun deleteGoal(id: Int) = dao.deleteGoal(id)

    suspend fun insertContribution(contribution: Contribution): Long = dao.insertContribution(contribution)
    suspend fun deleteContribution(id: Int) = dao.deleteContribution(id)

    suspend fun insertCommitment(commitment: Commitment): Long = dao.insertCommitment(commitment)
    suspend fun deleteCommitment(id: Int) = dao.deleteCommitment(id)

    suspend fun prePopulateSampleDataIfEmpty() {
        val defaultUser = UserSettings(
            id = 1,
            userName = "Amit",
            monthlyIncome = 5000000L, // ₹50,000 in paise
            currencySymbol = "₹",
            themeMode = "SYSTEM"
        )
        dao.upsertUserSettings(defaultUser)
    }

    suspend fun loadDemoSampleData() {
        dao.deleteAllGoals()
        dao.deleteAllContributions()
        dao.deleteAllCommitments()

        val goals = listOf(
            Goal(
                id = 1,
                name = "Ice Cream Maker",
                targetPrice = 500000L, // ₹5,000
                targetDateEpochMillis = System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000),
                alreadySavedAmount = 0L,
                category = "Kitchen",
                priority = "MEDIUM",
                expectedReturnRate = 8L,
                status = "ACTIVE"
            ),
            Goal(
                id = 2,
                name = "Laptop",
                targetPrice = 6000000L, // ₹60,000
                targetDateEpochMillis = System.currentTimeMillis() + (300L * 24 * 60 * 60 * 1000),
                alreadySavedAmount = 0L,
                category = "Electronics",
                priority = "HIGH",
                expectedReturnRate = 10L,
                status = "ACTIVE"
            )
        )

        val contributions = listOf(
            Contribution(goalId = 1, amount = 85000L, investmentType = "Savings", note = "July savings contribution"), // ₹850
            Contribution(goalId = 1, amount = 80000L, investmentType = "Mutual Fund", note = "June SIP"), // ₹800
            Contribution(goalId = 1, amount = 328000L, investmentType = "Mutual Fund", type = "VALUE_UPDATE", note = "Current portfolio value update"), // ₹3,280
            Contribution(goalId = 2, amount = 400000L, investmentType = "Mutual Fund", note = "Laptop SIP"), // ₹4,000
            Contribution(goalId = 2, amount = 1890000L, investmentType = "Mutual Fund", type = "VALUE_UPDATE", note = "Current portfolio value update") // ₹18,900
        )

        goals.forEach { dao.insertGoal(it) }
        contributions.forEach { dao.insertContribution(it) }
    }

    suspend fun clearAllCommitments() {
        dao.deleteAllCommitments()
    }

    suspend fun resetAllData() {
        dao.deleteAllGoals()
        dao.deleteAllContributions()
        dao.deleteAllCommitments()
    }

    suspend fun clearAllDataCompletely() {
        dao.deleteAllGoals()
        dao.deleteAllContributions()
        dao.deleteAllCommitments()
    }
}
