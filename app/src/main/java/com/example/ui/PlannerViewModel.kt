package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Commitment
import com.example.data.Contribution
import com.example.data.Goal
import com.example.data.PlannerRepository
import com.example.data.UserSettings
import com.example.util.PlannerCalculations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max

private data class Tuple5<A, B, C, D, E>(
    val userSettingsOrNull: A,
    val goalsList: B,
    val contributionsList: C,
    val commitmentsList: D,
    val monthlyRecordsList: E
)

data class GoalWithCalculations(
    val goal: Goal,
    val contributions: List<Contribution>,
    val totalContributed: Double,
    val currentValue: Double,
    val gainAmount: Double,
    val gainPercentage: Double,
    val fundingProgressPercentage: Double,
    val currentValueProgressPercentage: Double,
    val monthsRemaining: Int,
    val suggestedMonthlyContribution: Double,
    val projectedTargetValue: Double,
    val expectedSurplusOrShortfall: Double,
    val progressPercentage: Int,
    val isTargetReached: Boolean
)

data class DashboardSummary(
    val monthlyIncome: Double = 50000.0,
    val totalGoalAllocations: Double = 0.0,
    val totalCommitments: Double = 0.0,
    val totalOtherAllocations: Double = 0.0,
    val totalAllocated: Double = 0.0,
    val availableThisMonth: Double = 0.0,
    val allocatedPercentage: Int = 0,
    val availablePercentage: Int = 100,
    val isOverAllocated: Boolean = false
)

data class PlannerUiState(
    val userSettings: UserSettings = UserSettings(),
    val goals: List<GoalWithCalculations> = emptyList(),
    val activeGoals: List<GoalWithCalculations> = emptyList(),
    val pausedGoals: List<GoalWithCalculations> = emptyList(),
    val completedGoals: List<GoalWithCalculations> = emptyList(),
    val allContributions: List<Contribution> = emptyList(),
    val commitments: List<Commitment> = emptyList(),
    val dashboardSummary: DashboardSummary = DashboardSummary(),
    val selectedMonthYear: String = "July 2026",
    val isLoading: Boolean = true
)

class PlannerViewModel(
    application: Application,
    private val repository: PlannerRepository
) : AndroidViewModel(application) {

    init {
        viewModelScope.launch {
            val user = repository.userSettings.firstOrNull()
            val goals = repository.allGoals.firstOrNull()
            if (user == null || goals.isNullOrEmpty()) {
                repository.prePopulateSampleDataIfEmpty()
            }
        }
    }

    private val _selectedMonthYear = MutableStateFlow("July 2026")

    private val domainDataFlow = combine(
        repository.userSettings,
        repository.allGoals,
        repository.allContributions,
        repository.allCommitments,
        repository.allMonthlyRecords
    ) { userSettingsOrNull, goalsList, contributionsList, commitmentsList, monthlyRecordsList ->
        Tuple5(userSettingsOrNull, goalsList, contributionsList, commitmentsList, monthlyRecordsList)
    }

    val uiState: StateFlow<PlannerUiState> = combine(
        domainDataFlow,
        _selectedMonthYear
    ) { domainData, monthYear ->
        val userSettings = domainData.userSettingsOrNull ?: UserSettings()
        val monthlyRecordForSelected = domainData.monthlyRecordsList.find { it.monthYear == monthYear }
        val effectiveMonthlyIncome = monthlyRecordForSelected?.monthlyIncome ?: userSettings.monthlyIncome

        val goalsWithCalc = domainData.goalsList.map { goal ->
            val goalContribs = domainData.contributionsList.filter { it.goalId == goal.id }
            val ledger = PlannerCalculations.calculateGoalLedger(goal, goalContribs)

            val monthsRem = PlannerCalculations.calculateMonthsRemaining(goal.targetDateEpochMillis)
            val suggestedContribution = if (goal.status == "ACTIVE") {
                PlannerCalculations.calculateMonthlyRequiredContribution(
                    targetPrice = goal.targetPrice,
                    currentValue = ledger.latestActualValue,
                    monthsRemaining = monthsRem,
                    expectedReturnRatePcent = goal.expectedReturnRate
                )
            } else 0.0

            val projectedTarget = PlannerCalculations.calculateProjectedTargetValue(
                currentValue = ledger.latestActualValue,
                monthlyContribution = suggestedContribution,
                monthsRemaining = monthsRem,
                expectedReturnRatePcent = goal.expectedReturnRate
            )

            val surplusShortfall = projectedTarget - goal.targetPrice
            val progressPcent = ledger.currentValueProgressPercentage.toInt().coerceIn(0, 100)

            GoalWithCalculations(
                goal = goal,
                contributions = goalContribs,
                totalContributed = ledger.totalContributed,
                currentValue = ledger.latestActualValue,
                gainAmount = ledger.gainAmount,
                gainPercentage = ledger.gainPercentage,
                fundingProgressPercentage = ledger.fundingProgressPercentage,
                currentValueProgressPercentage = ledger.currentValueProgressPercentage,
                monthsRemaining = monthsRem,
                suggestedMonthlyContribution = suggestedContribution,
                projectedTargetValue = projectedTarget,
                expectedSurplusOrShortfall = surplusShortfall,
                progressPercentage = progressPcent,
                isTargetReached = ledger.latestActualValue >= goal.targetPrice
            )
        }

        val active = goalsWithCalc.filter { it.goal.status == "ACTIVE" }
        val paused = goalsWithCalc.filter { it.goal.status == "PAUSED" }
        val completed = goalsWithCalc.filter { it.goal.status == "COMPLETED" }

        // Dashboard Summary calculations
        val totalGoalAllocations = active.sumOf { it.suggestedMonthlyContribution }
        val totalCommitments = domainData.commitmentsList.filter { it.category != "Other" }.sumOf { it.monthlyAmount }
        val totalOther = domainData.commitmentsList.filter { it.category == "Other" }.sumOf { it.monthlyAmount }

        val totalAllocated = totalGoalAllocations + totalCommitments + totalOther
        val available = effectiveMonthlyIncome - totalAllocated
        val availablePcent = if (effectiveMonthlyIncome > 0) {
            ((available / effectiveMonthlyIncome) * 100).toInt().coerceIn(0, 100)
        } else 0
        val allocatedPcent = 100 - availablePcent

        val summary = DashboardSummary(
            monthlyIncome = effectiveMonthlyIncome,
            totalGoalAllocations = totalGoalAllocations,
            totalCommitments = totalCommitments,
            totalOtherAllocations = totalOther,
            totalAllocated = totalAllocated,
            availableThisMonth = max(0.0, available),
            allocatedPercentage = allocatedPcent,
            availablePercentage = availablePcent,
            isOverAllocated = totalAllocated > effectiveMonthlyIncome
        )

        PlannerUiState(
            userSettings = userSettings.copy(monthlyIncome = effectiveMonthlyIncome),
            goals = goalsWithCalc,
            activeGoals = active,
            pausedGoals = paused,
            completedGoals = completed,
            allContributions = domainData.contributionsList,
            commitments = domainData.commitmentsList,
            dashboardSummary = summary,
            selectedMonthYear = monthYear,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlannerUiState()
    )

    fun setSelectedMonthYear(monthYear: String) {
        _selectedMonthYear.value = monthYear
    }

    fun setMonthlyIncomeForSelectedMonth(monthYear: String, income: Double) {
        viewModelScope.launch {
            repository.upsertMonthlyRecord(
                com.example.data.MonthlyRecord(monthYear = monthYear, monthlyIncome = income)
            )
        }
    }

    fun addGoal(goal: Goal) {
        viewModelScope.launch {
            repository.insertGoal(goal)
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteGoal(id: Int) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    fun pauseGoal(goalId: Int) {
        viewModelScope.launch {
            val goal = uiState.value.goals.find { it.goal.id == goalId }?.goal
            if (goal != null) {
                repository.updateGoal(goal.copy(status = "PAUSED"))
            }
        }
    }

    fun resumeGoal(goalId: Int) {
        viewModelScope.launch {
            val goal = uiState.value.goals.find { it.goal.id == goalId }?.goal
            if (goal != null) {
                repository.updateGoal(goal.copy(status = "ACTIVE"))
            }
        }
    }

    fun completeGoal(goalId: Int, finalPurchasePrice: Double) {
        viewModelScope.launch {
            val goalWithCalc = uiState.value.goals.find { it.goal.id == goalId }
            if (goalWithCalc != null) {
                val updatedGoal = goalWithCalc.goal.copy(
                    status = "COMPLETED",
                    completedDateEpochMillis = System.currentTimeMillis(),
                    finalPurchasePrice = finalPurchasePrice
                )
                repository.updateGoal(updatedGoal)
            }
        }
    }

    fun addContribution(contribution: Contribution) {
        viewModelScope.launch {
            repository.insertContribution(contribution)
            // Also update current manual value of goal to include new contribution
            val goalWithCalc = uiState.value.goals.find { it.goal.id == contribution.goalId }
            if (goalWithCalc != null) {
                val newCurrentVal = goalWithCalc.currentValue + contribution.amount
                repository.updateGoal(goalWithCalc.goal.copy(currentManualValue = newCurrentVal))
            }
        }
    }

    fun deleteContribution(id: Int) {
        viewModelScope.launch {
            repository.deleteContribution(id)
        }
    }

    fun addCommitment(commitment: Commitment) {
        viewModelScope.launch {
            repository.insertCommitment(commitment)
        }
    }

    fun deleteCommitment(id: Int) {
        viewModelScope.launch {
            repository.deleteCommitment(id)
        }
    }

    fun clearAllCommitments() {
        viewModelScope.launch {
            repository.clearAllCommitments()
        }
    }

    fun updateUserSettings(settings: UserSettings) {
        viewModelScope.launch {
            repository.upsertUserSettings(settings)
        }
    }

    fun resetSampleData() {
        viewModelScope.launch {
            repository.loadDemoSampleData()
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllDataCompletely()
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = AppDatabase.getDatabase(application)
            val repo = PlannerRepository(db.plannerDao())
            return PlannerViewModel(application, repo) as T
        }
    }
}
