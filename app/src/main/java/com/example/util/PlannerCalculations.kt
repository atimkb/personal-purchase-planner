package com.example.util

import com.example.data.Commitment
import com.example.data.Contribution
import com.example.data.Goal
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToLong

data class GoalLedgerMetrics(
    val totalContributed: Double,
    val latestActualValue: Double,
    val gainAmount: Double,
    val gainPercentage: Double,
    val fundingProgressPercentage: Double,
    val currentValueProgressPercentage: Double,
    val remainingRequirement: Double
)

data class ExtendTimelineResult(
    val isPossible: Boolean,
    val monthsNeeded: Int,
    val suggestedTargetDateMillis: Long,
    val explanationMessage: String
)

data class AffordabilityResult(
    val requiredMonthly: Double,
    val isAffordable: Boolean,
    val capacityShortfall: Double,
    val remainingCapacityAfterGoal: Double
)

data class AllocationMetrics(
    val plannedIncome: Double,
    val plannedCommitments: Double,
    val plannedGoalContributions: Double,
    val plannedUnallocated: Double,
    val actualIncome: Double,
    val actualCommitments: Double,
    val actualGoalContributions: Double,
    val actualUnallocated: Double
)

object PlannerCalculations {

    fun formatCurrency(amount: Double, symbol: String = "₹"): String {
        val longVal = amount.roundToLong()
        val formatted = if (symbol == "₹") {
            formatIndianCurrency(longVal)
        } else {
            val nf = NumberFormat.getIntegerInstance(Locale.US)
            nf.format(longVal)
        }
        return "$symbol$formatted"
    }

    private fun formatIndianCurrency(value: Long): String {
        if (value < 0) return "-" + formatIndianCurrency(-value)
        val str = value.toString()
        if (str.length <= 3) return str

        val lastThree = str.substring(str.length - 3)
        val rest = str.substring(0, str.length - 3)
        val sb = StringBuilder()

        var count = 0
        for (i in rest.length - 1 downTo 0) {
            sb.append(rest[i])
            count++
            if (count % 2 == 0 && i != 0) {
                sb.append(",")
            }
        }
        return sb.reverse().toString() + "," + lastThree
    }

    fun calculateMonthsRemaining(targetDateMillis: Long, fromDateMillis: Long = System.currentTimeMillis()): Int {
        val zone = ZoneId.systemDefault()
        val targetDate = Instant.ofEpochMilli(targetDateMillis).atZone(zone).toLocalDate()
        val fromDate = Instant.ofEpochMilli(fromDateMillis).atZone(zone).toLocalDate()

        val months = ChronoUnit.MONTHS.between(fromDate.withDayOfMonth(1), targetDate.withDayOfMonth(1)).toInt()
        return max(1, months)
    }

    fun calculateGoalLedger(
        goal: Goal,
        contributions: List<Contribution>
    ): GoalLedgerMetrics {
        var contribTotal = goal.alreadySavedAmount
        var latestValueUpdate: Double? = null

        contributions.forEach { contrib ->
            when (contrib.type) {
                "WITHDRAWAL" -> contribTotal -= kotlin.math.abs(contrib.amount)
                "VALUE_UPDATE" -> latestValueUpdate = contrib.amount
                else -> contribTotal += contrib.amount
            }
        }

        val totalContributed = max(0.0, contribTotal)
        val latestActualValue = goal.currentManualValue ?: latestValueUpdate ?: totalContributed
        val gainAmount = latestActualValue - totalContributed
        val gainPercentage = if (totalContributed > 0) (gainAmount / totalContributed) * 100.0 else 0.0

        val fundingProgressPct = if (goal.targetPrice > 0) ((totalContributed / goal.targetPrice) * 100.0).coerceIn(0.0, 100.0) else 0.0
        val currentValueProgressPct = if (goal.targetPrice > 0) ((latestActualValue / goal.targetPrice) * 100.0).coerceIn(0.0, 100.0) else 0.0
        val remainingReq = max(0.0, goal.targetPrice - latestActualValue)

        return GoalLedgerMetrics(
            totalContributed = totalContributed,
            latestActualValue = latestActualValue,
            gainAmount = gainAmount,
            gainPercentage = gainPercentage,
            fundingProgressPercentage = fundingProgressPct,
            currentValueProgressPercentage = currentValueProgressPct,
            remainingRequirement = remainingReq
        )
    }

    fun calculateMonthlyRequiredContribution(
        targetPrice: Double,
        currentValue: Double,
        monthsRemaining: Int,
        expectedReturnRatePcent: Double = 8.0
    ): Double {
        val remainingToSave = max(0.0, targetPrice - currentValue)
        if (remainingToSave <= 0) return 0.0
        if (monthsRemaining <= 0) return remainingToSave

        val annualRate = expectedReturnRatePcent / 100.0
        if (annualRate <= 0.001) {
            return remainingToSave / monthsRemaining
        }

        val monthlyRate = annualRate / 12.0
        val n = monthsRemaining.toDouble()
        val factor = (1 + monthlyRate).pow(n) - 1
        if (factor <= 0) return remainingToSave / monthsRemaining

        val pmt = (remainingToSave * monthlyRate) / factor
        return max(0.0, pmt)
    }

    fun calculateProjectedTargetValue(
        currentValue: Double,
        monthlyContribution: Double,
        monthsRemaining: Int,
        expectedReturnRatePcent: Double = 8.0
    ): Double {
        val annualRate = expectedReturnRatePcent / 100.0
        val n = monthsRemaining.toDouble()

        if (annualRate <= 0.001) {
            return currentValue + (monthlyContribution * n)
        }

        val monthlyRate = annualRate / 12.0
        val compoundedCurrent = currentValue * (1 + monthlyRate).pow(n)
        val futureValueContributions = monthlyContribution * (((1 + monthlyRate).pow(n) - 1) / monthlyRate)

        return compoundedCurrent + futureValueContributions
    }

    fun calculateAffordability(
        targetPrice: Double,
        currentValue: Double,
        monthsRemaining: Int,
        returnRatePcent: Double,
        availableMonthlyCapacity: Double
    ): AffordabilityResult {
        val required = calculateMonthlyRequiredContribution(
            targetPrice = targetPrice,
            currentValue = currentValue,
            monthsRemaining = monthsRemaining,
            expectedReturnRatePcent = returnRatePcent
        )

        val capacityShortfall = max(0.0, required - max(0.0, availableMonthlyCapacity))
        val remainingAfterGoal = availableMonthlyCapacity - required
        val isAffordable = required <= availableMonthlyCapacity

        return AffordabilityResult(
            requiredMonthly = required,
            isAffordable = isAffordable,
            capacityShortfall = capacityShortfall,
            remainingCapacityAfterGoal = remainingAfterGoal
        )
    }

    fun calculateEarliestAffordableTargetDate(
        remainingTarget: Double,
        availableCapacity: Double,
        fromDateMillis: Long = System.currentTimeMillis()
    ): ExtendTimelineResult {
        if (remainingTarget <= 0) {
            return ExtendTimelineResult(
                isPossible = true,
                monthsNeeded = 0,
                suggestedTargetDateMillis = fromDateMillis,
                explanationMessage = "Goal target is already reached or 0!"
            )
        }

        if (availableCapacity <= 0) {
            return ExtendTimelineResult(
                isPossible = false,
                monthsNeeded = -1,
                suggestedTargetDateMillis = fromDateMillis,
                explanationMessage = "Your commitments and existing goal allocations consume all available monthly income. Reduce commitments or increase income to make room for this goal."
            )
        }

        val monthsNeeded = ceil(remainingTarget / availableCapacity).toInt().coerceAtLeast(1)
        val zone = ZoneId.systemDefault()
        val fromDate = Instant.ofEpochMilli(fromDateMillis).atZone(zone).toLocalDate()
        val targetDate = fromDate.plusMonths(monthsNeeded.toLong())
        val targetMillis = targetDate.atStartOfDay(zone).toInstant().toEpochMilli()

        return ExtendTimelineResult(
            isPossible = true,
            monthsNeeded = monthsNeeded,
            suggestedTargetDateMillis = targetMillis,
            explanationMessage = "At your current available capacity of ${formatCurrency(availableCapacity)}, you can reach this target in $monthsNeeded months."
        )
    }

    fun calculateMonthlyAllocations(
        monthlyIncome: Double,
        commitments: List<Commitment>,
        activeSuggestedGoalContributions: Double,
        actualContributionsThisMonth: List<Contribution>
    ): AllocationMetrics {
        val plannedCommitments = commitments.sumOf { it.monthlyAmount }
        val plannedGoalContribs = activeSuggestedGoalContributions
        val plannedUnallocated = monthlyIncome - plannedCommitments - plannedGoalContribs

        val actualCommitments = plannedCommitments
        val actualGoalContribs = actualContributionsThisMonth.sumOf { it.amount }
        val actualUnallocated = monthlyIncome - actualCommitments - actualGoalContribs

        return AllocationMetrics(
            plannedIncome = monthlyIncome,
            plannedCommitments = plannedCommitments,
            plannedGoalContributions = plannedGoalContribs,
            plannedUnallocated = max(0.0, plannedUnallocated),
            actualIncome = monthlyIncome,
            actualCommitments = actualCommitments,
            actualGoalContributions = actualGoalContribs,
            actualUnallocated = max(0.0, actualUnallocated)
        )
    }

    fun formatDate(epochMillis: Long, pattern: String = "d MMM yyyy"): String {
        val localDate = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val formatter = DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
        return localDate.format(formatter)
    }

    fun formatMonthYear(epochMillis: Long = System.currentTimeMillis()): String {
        val localDate = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        return localDate.format(formatter)
    }
}
