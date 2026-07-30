package com.example

import com.example.data.Commitment
import com.example.data.Contribution
import com.example.data.Goal
import com.example.util.PlannerCalculations
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlannerCalculationsTest {

    @Test
    fun testCurrencyFormatting() {
        assertEquals("₹5,000", PlannerCalculations.formatCurrency(5000.0, "₹"))
        assertEquals("₹1,50,000", PlannerCalculations.formatCurrency(150000.0, "₹"))
        assertEquals("$5,000", PlannerCalculations.formatCurrency(5000.0, "$"))
    }

    @Test
    fun testGoalLedgerCalculations() {
        val goal = Goal(
            id = 1,
            name = "Test Goal",
            targetPrice = 5000.0,
            alreadySavedAmount = 1000.0
        )

        val contribs = listOf(
            Contribution(goalId = 1, amount = 2000.0, type = "CONTRIBUTION"),
            Contribution(goalId = 1, amount = 500.0, type = "WITHDRAWAL"),
            Contribution(goalId = 1, amount = 3200.0, type = "VALUE_UPDATE")
        )

        val ledger = PlannerCalculations.calculateGoalLedger(goal, contribs)

        // Contributed: 1000 + 2000 - 500 = 2500
        assertEquals(2500.0, ledger.totalContributed, 0.01)
        // Latest value update = 3200
        assertEquals(3200.0, ledger.latestActualValue, 0.01)
        // Gain = 3200 - 2500 = 700
        assertEquals(700.0, ledger.gainAmount, 0.01)
        // Funding progress = (2500 / 5000) * 100 = 50%
        assertEquals(50.0, ledger.fundingProgressPercentage, 0.01)
        // Current value progress = (3200 / 5000) * 100 = 64%
        assertEquals(64.0, ledger.currentValueProgressPercentage, 0.01)
    }

    @Test
    fun testAffordabilityAndTimelineExtension() {
        // Target: 10,000, currentValue: 0, 10 months remaining, return rate 0% => required 1000/mo
        val aff = PlannerCalculations.calculateAffordability(
            targetPrice = 10000.0,
            currentValue = 0.0,
            monthsRemaining = 10,
            returnRatePcent = 0.0,
            availableMonthlyCapacity = 800.0
        )

        assertEquals(1000.0, aff.requiredMonthly, 0.01)
        assertFalse(aff.isAffordable)
        assertEquals(200.0, aff.capacityShortfall, 0.01)

        // Timeline extension check
        val extension = PlannerCalculations.calculateEarliestAffordableTargetDate(
            remainingTarget = 10000.0,
            availableCapacity = 800.0
        )

        assertTrue(extension.isPossible)
        // 10000 / 800 = 12.5 => 13 months
        assertEquals(13, extension.monthsNeeded)

        // Zero/negative capacity check
        val blockedExtension = PlannerCalculations.calculateEarliestAffordableTargetDate(
            remainingTarget = 10000.0,
            availableCapacity = 0.0
        )
        assertFalse(blockedExtension.isPossible)
    }

    @Test
    fun testAllocationMetrics() {
        val commitments = listOf(
            Commitment(id = 1, name = "Rent", monthlyAmount = 15000.0, category = "Housing")
        )

        val actualContribs = listOf(
            Contribution(goalId = 1, amount = 3000.0, type = "CONTRIBUTION")
        )

        val metrics = PlannerCalculations.calculateMonthlyAllocations(
            monthlyIncome = 50000.0,
            commitments = commitments,
            activeSuggestedGoalContributions = 5000.0,
            actualContributionsThisMonth = actualContribs
        )

        assertEquals(50000.0, metrics.plannedIncome, 0.01)
        assertEquals(15000.0, metrics.plannedCommitments, 0.01)
        assertEquals(5000.0, metrics.plannedGoalContributions, 0.01)
        assertEquals(30000.0, metrics.plannedUnallocated, 0.01)

        assertEquals(3000.0, metrics.actualGoalContributions, 0.01)
        assertEquals(32000.0, metrics.actualUnallocated, 0.01)
    }
}
