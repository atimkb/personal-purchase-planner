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
    val totalContributed: Long,
    val currentValue: Long,
    val gainAmount: Long,
    val gainPercentage: Long,
    val fundingProgressPercentage: Long,
    val currentValueProgressPercentage: Long,
    val monthsRemaining: Int,
    val suggestedMonthlyContribution: Long,
    val projectedTargetValue: Long,
    val expectedSurplusOrShortfall: Long,
    val progressPercentage: Int,
    val isTargetReached: Boolean
)

data class DashboardSummary(
    val monthlyIncome: Long = 5000000L,
    val totalGoalAllocations: Long = 0L,
    val totalCommitments: Long = 0L,
    val totalOtherAllocations: Long = 0L,
    val totalAllocated: Long = 0L,
    val availableThisMonth: Long = 0L,
    val allocatedPercentage: Int = 0,
    val availablePercentage: Int = 100,
    val isOverAllocated: Boolean = false,
    val totalActuallyRecorded: Long = 0L
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
    val selectedMonthYear: String = PlannerCalculations.formatMonthYear(),
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

    private val _selectedMonthYear = MutableStateFlow(PlannerCalculations.formatMonthYear())

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

        // Month calendar boundaries: first moment to last moment of selected month
        val monthBounds = PlannerCalculations.getMonthBounds(monthYear)
        val (startOfMonth, endOfMonth) = monthBounds ?: Pair(0L, Long.MAX_VALUE)

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
            } else 0L

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

        // Goals active during the selected month: created <= endOfMonth AND not completed before startOfMonth
        val goalsActiveInMonth = goalsWithCalc.filter { calc ->
            val g = calc.goal
            g.createdAtEpochMillis <= endOfMonth &&
            (g.completedDateEpochMillis == null || g.completedDateEpochMillis >= startOfMonth) &&
            g.status != "CANCELLED"
        }

        // PLANNED goal allocations for selected month
        val totalGoalAllocations = goalsActiveInMonth.filter { it.goal.status == "ACTIVE" }.sumOf { it.suggestedMonthlyContribution }

        // Commitments active during selected month: created <= endOfMonth
        val totalCommitments = domainData.commitmentsList
            .filter { it.category != "Other" && it.createdAtEpochMillis <= endOfMonth }
            .sumOf { it.monthlyAmount }
        val totalOther = domainData.commitmentsList
            .filter { it.category == "Other" && it.createdAtEpochMillis <= endOfMonth }
            .sumOf { it.monthlyAmount }

        // ACTUALLY RECORDED contributions during selected month:
        // Contribution entries whose dateEpochMillis falls within [startOfMonth, endOfMonth]
        val contributionsThisMonth = domainData.contributionsList.filter { 
            it.dateEpochMillis in startOfMonth..endOfMonth 
        }
        val actualAdditions = contributionsThisMonth.filter { it.type == "CONTRIBUTION" }.sumOf { it.amount }
        val actualWithdrawals = contributionsThisMonth.filter { it.type == "WITHDRAWAL" }.sumOf { it.amount }
        val totalActuallyRecorded = max(0L, actualAdditions - actualWithdrawals)

        // ACTUAL allocated = actually recorded goal contributions + recurring commitments + other commitments
        val actualAllocated = totalActuallyRecorded + totalCommitments + totalOther
        val available = effectiveMonthlyIncome - actualAllocated
        val availablePcent = if (effectiveMonthlyIncome > 0L) {
            ((available * 100L) / effectiveMonthlyIncome).toInt()
        } else 0
        val allocatedPcent = if (effectiveMonthlyIncome > 0L) {
            ((actualAllocated * 100L) / effectiveMonthlyIncome).toInt()
        } else 0

        val summary = DashboardSummary(
            monthlyIncome = effectiveMonthlyIncome,
            totalGoalAllocations = totalGoalAllocations,
            totalCommitments = totalCommitments,
            totalOtherAllocations = totalOther,
            totalAllocated = actualAllocated,
            availableThisMonth = available,
            allocatedPercentage = allocatedPcent,
            availablePercentage = availablePcent,
            isOverAllocated = actualAllocated > effectiveMonthlyIncome || available < 0L,
            totalActuallyRecorded = totalActuallyRecorded
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

    fun setMonthlyIncomeForSelectedMonth(monthYear: String, incomeInPaise: Long) {
        viewModelScope.launch {
            repository.upsertMonthlyRecord(
                com.example.data.MonthlyRecord(monthYear = monthYear, monthlyIncome = incomeInPaise)
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

    fun completeGoal(goalId: Int, finalPurchasePriceInPaise: Long) {
        viewModelScope.launch {
            val goalWithCalc = uiState.value.goals.find { it.goal.id == goalId }
            if (goalWithCalc != null) {
                val updatedGoal = goalWithCalc.goal.copy(
                    status = "COMPLETED",
                    completedDateEpochMillis = System.currentTimeMillis(),
                    finalPurchasePrice = finalPurchasePriceInPaise
                )
                repository.updateGoal(updatedGoal)
            }
        }
    }

    fun addContribution(contribution: Contribution) {
        viewModelScope.launch {
            repository.insertContribution(contribution)
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
