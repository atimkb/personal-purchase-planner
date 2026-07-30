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
            monthlyIncome = 50000.0,
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
                targetPrice = 5000.0,
                targetDateEpochMillis = System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000),
                alreadySavedAmount = 0.0,
                category = "Kitchen",
                priority = "MEDIUM",
                expectedReturnRate = 8.0,
                status = "ACTIVE",
                currentManualValue = 3280.0
            ),
            Goal(
                id = 2,
                name = "Laptop",
                targetPrice = 60000.0,
                targetDateEpochMillis = System.currentTimeMillis() + (300L * 24 * 60 * 60 * 1000),
                alreadySavedAmount = 0.0,
                category = "Electronics",
                priority = "HIGH",
                expectedReturnRate = 10.0,
                status = "ACTIVE",
                currentManualValue = 18900.0
            )
        )

        val contributions = listOf(
            Contribution(goalId = 1, amount = 850.0, investmentType = "Savings", note = "July savings contribution"),
            Contribution(goalId = 1, amount = 800.0, investmentType = "Mutual Fund", note = "June SIP"),
            Contribution(goalId = 2, amount = 4000.0, investmentType = "Mutual Fund", note = "Laptop SIP")
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
