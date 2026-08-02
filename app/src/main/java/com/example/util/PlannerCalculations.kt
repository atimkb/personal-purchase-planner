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
import kotlin.math.abs
import kotlin.math.max

data class GoalLedgerMetrics(
    val totalContributed: Long,
    val latestActualValue: Long,
    val gainAmount: Long,
    val gainPercentage: Long,
    val fundingProgressPercentage: Long,
    val currentValueProgressPercentage: Long,
    val remainingRequirement: Long
)

data class ExtendTimelineResult(
    val isPossible: Boolean,
    val monthsNeeded: Int,
    val suggestedTargetDateMillis: Long,
    val explanationMessage: String
)

data class AffordabilityResult(
    val requiredMonthly: Long,
    val isAffordable: Boolean,
    val capacityShortfall: Long,
    val remainingCapacityAfterGoal: Long
)

data class AllocationMetrics(
    val plannedIncome: Long,
    val plannedCommitments: Long,
    val plannedGoalContributions: Long,
    val plannedUnallocated: Long,
    val actualIncome: Long,
    val actualCommitments: Long,
    val actualGoalContributions: Long,
    val actualUnallocated: Long
)

object PlannerCalculations {

    fun formatCurrency(amountInPaise: Long, symbol: String = "₹"): String {
        val absAmount = abs(amountInPaise)
        val rupees = absAmount / 100L
        val remainder = absAmount % 100L
        val formattedRupees = if (symbol == "₹") {
            formatIndianCurrency(rupees)
        } else {
            val nf = NumberFormat.getIntegerInstance(Locale.US)
            nf.format(rupees)
        }
        val formatted = if (remainder == 0L) {
            "$symbol$formattedRupees"
        } else {
            "$symbol$formattedRupees.${remainder.toString().padStart(2, '0')}"
        }
        return if (amountInPaise < 0L) "-$formatted" else formatted
    }

    private fun formatIndianCurrency(value: Long): String {
        if (value < 0L) return "-" + formatIndianCurrency(-value)
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
        var latestValueUpdate: Long? = null

        contributions.forEach { contrib ->
            when (contrib.type) {
                "WITHDRAWAL" -> contribTotal -= abs(contrib.amount)
                "VALUE_UPDATE" -> latestValueUpdate = contrib.amount
                else -> contribTotal += contrib.amount
            }
        }

        val totalContributed = max(0L, contribTotal)
        val latestActualValue = latestValueUpdate ?: totalContributed
        val gainAmount = latestActualValue - totalContributed
        val gainPercentage = if (totalContributed > 0L) (gainAmount * 100L) / totalContributed else 0L

        val fundingProgressPct = if (goal.targetPrice > 0L) ((totalContributed * 100L) / goal.targetPrice).coerceIn(0L, 100L) else 0L
        val currentValueProgressPct = if (goal.targetPrice > 0L) ((latestActualValue * 100L) / goal.targetPrice).coerceIn(0L, 100L) else 0L
        val remainingReq = max(0L, goal.targetPrice - latestActualValue)

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
        targetPrice: Long,
        currentValue: Long,
        monthsRemaining: Int,
        expectedReturnRatePcent: Long = 8L
    ): Long {
        val remainingToSave = max(0L, targetPrice - currentValue)
        if (remainingToSave <= 0L) return 0L
        if (monthsRemaining <= 0) return remainingToSave

        val n = monthsRemaining.toLong()
        if (expectedReturnRatePcent <= 0L) {
            return remainingToSave / n
        }

        val denom = 1200L * n + (expectedReturnRatePcent * n * (n + 1L)) / 2L
        if (denom <= 0L) return remainingToSave / n

        val pmt = (remainingToSave * 1200L) / denom
        return max(0L, pmt)
    }

    fun calculateProjectedTargetValue(
        currentValue: Long,
        monthlyContribution: Long,
        monthsRemaining: Int,
        expectedReturnRatePcent: Long = 8L
    ): Long {
        val n = monthsRemaining.toLong()
        val baseContributions = monthlyContribution * n
        if (expectedReturnRatePcent <= 0L) {
            return currentValue + baseContributions
        }

        val interestGain = (monthlyContribution * expectedReturnRatePcent * n * (n + 1L)) / 2400L
        val compoundedCurrent = currentValue + (currentValue * expectedReturnRatePcent * n) / 1200L

        return compoundedCurrent + baseContributions + interestGain
    }

    fun calculateAffordability(
        targetPrice: Long,
        currentValue: Long,
        monthsRemaining: Int,
        returnRatePcent: Long,
        availableMonthlyCapacity: Long
    ): AffordabilityResult {
        val required = calculateMonthlyRequiredContribution(
            targetPrice = targetPrice,
            currentValue = currentValue,
            monthsRemaining = monthsRemaining,
            expectedReturnRatePcent = returnRatePcent
        )

        val capacityShortfall = max(0L, required - max(0L, availableMonthlyCapacity))
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
        remainingTarget: Long,
        availableCapacity: Long,
        fromDateMillis: Long = System.currentTimeMillis()
    ): ExtendTimelineResult {
        if (remainingTarget <= 0L) {
            return ExtendTimelineResult(
                isPossible = true,
                monthsNeeded = 0,
                suggestedTargetDateMillis = fromDateMillis,
                explanationMessage = "Goal target is already reached or 0!"
            )
        }

        if (availableCapacity <= 0L) {
            return ExtendTimelineResult(
                isPossible = false,
                monthsNeeded = -1,
                suggestedTargetDateMillis = fromDateMillis,
                explanationMessage = "Your commitments and existing goal allocations consume all available monthly income. Reduce commitments or increase income to make room for this goal."
            )
        }

        val monthsNeeded = max(1, ((remainingTarget + availableCapacity - 1L) / availableCapacity).toInt())
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
        monthlyIncome: Long,
        commitments: List<Commitment>,
        activeSuggestedGoalContributions: Long,
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
            plannedUnallocated = plannedUnallocated,
            actualIncome = monthlyIncome,
            actualCommitments = actualCommitments,
            actualGoalContributions = actualGoalContribs,
            actualUnallocated = actualUnallocated
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

    fun getMonthBounds(monthYearStr: String, zone: ZoneId = ZoneId.systemDefault()): Pair<Long, Long>? {
        return try {
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
            val yearMonth = java.time.YearMonth.parse(monthYearStr, formatter)
            val startOfMonth = yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
            val endOfMonth = yearMonth.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1L
            Pair(startOfMonth, endOfMonth)
        } catch (e: Exception) {
            null
        }
    }
}
