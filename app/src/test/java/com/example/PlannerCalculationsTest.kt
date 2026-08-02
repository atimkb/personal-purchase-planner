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
        assertEquals("₹5,000", PlannerCalculations.formatCurrency(500000L, "₹"))
        assertEquals("₹1,50,000", PlannerCalculations.formatCurrency(15000000L, "₹"))
        assertEquals("$5,000", PlannerCalculations.formatCurrency(500000L, "$"))
        assertEquals("₹5,000.50", PlannerCalculations.formatCurrency(500050L, "₹"))
    }

    @Test
    fun testGoalLedgerCalculations() {
        val goal = Goal(
            id = 1,
            name = "Test Goal",
            targetPrice = 500000L, // ₹5000
            targetDateEpochMillis = System.currentTimeMillis() + 86400000L * 30L,
            alreadySavedAmount = 100000L // ₹1000
        )

        val contribs = listOf(
            Contribution(goalId = 1, amount = 200000L, type = "CONTRIBUTION"), // ₹2000
            Contribution(goalId = 1, amount = 50000L, type = "WITHDRAWAL"), // ₹500
            Contribution(goalId = 1, amount = 320000L, type = "VALUE_UPDATE") // ₹3200
        )

        val ledger = PlannerCalculations.calculateGoalLedger(goal, contribs)

        // Contributed: 1000 + 2000 - 500 = 2500 (250000L paise)
        assertEquals(250000L, ledger.totalContributed)
        // Latest value update = 3200 (320000L paise)
        assertEquals(320000L, ledger.latestActualValue)
        // Gain = 3200 - 2500 = 700 (70000L paise)
        assertEquals(70000L, ledger.gainAmount)
        // Funding progress = (2500 / 5000) * 100 = 50%
        assertEquals(50L, ledger.fundingProgressPercentage)
        // Current value progress = (3200 / 5000) * 100 = 64%
        assertEquals(64L, ledger.currentValueProgressPercentage)
    }

    @Test
    fun testAffordabilityAndTimelineExtension() {
        // Target: 10,000 (1000000L), currentValue: 0, 10 months remaining, return rate 0% => required 1000/mo (100000L)
        val aff = PlannerCalculations.calculateAffordability(
            targetPrice = 1000000L,
            currentValue = 0L,
            monthsRemaining = 10,
            returnRatePcent = 0L,
            availableMonthlyCapacity = 80000L // ₹800
        )

        assertEquals(100000L, aff.requiredMonthly)
        assertFalse(aff.isAffordable)
        assertEquals(20000L, aff.capacityShortfall)

        // Timeline extension check
        val extension = PlannerCalculations.calculateEarliestAffordableTargetDate(
            remainingTarget = 1000000L,
            availableCapacity = 80000L
        )

        assertTrue(extension.isPossible)
        assertEquals(13, extension.monthsNeeded)

        // Zero/negative capacity check
        val blockedExtension = PlannerCalculations.calculateEarliestAffordableTargetDate(
            remainingTarget = 1000000L,
            availableCapacity = 0L
        )
        assertFalse(blockedExtension.isPossible)
    }

    @Test
    fun testAllocationMetrics() {
        val commitments = listOf(
            Commitment(id = 1, name = "Rent", monthlyAmount = 1500000L, category = "Housing")
        )

        val actualContribs = listOf(
            Contribution(goalId = 1, amount = 300000L, type = "CONTRIBUTION")
        )

        val metrics = PlannerCalculations.calculateMonthlyAllocations(
            monthlyIncome = 5000000L,
            commitments = commitments,
            activeSuggestedGoalContributions = 500000L,
            actualContributionsThisMonth = actualContribs
        )

        assertEquals(5000000L, metrics.plannedIncome)
        assertEquals(1500000L, metrics.plannedCommitments)
        assertEquals(500000L, metrics.plannedGoalContributions)
        assertEquals(3000000L, metrics.plannedUnallocated)

        assertEquals(300000L, metrics.actualGoalContributions)
        assertEquals(3200000L, metrics.actualUnallocated)
    }

    @Test
    fun testNegativeUnallocatedAllocationMetrics() {
        val commitments = listOf(
            Commitment(id = 1, name = "Rent", monthlyAmount = 30000L, category = "Housing")
        )

        val actualContribs = listOf(
            Contribution(goalId = 1, amount = 25000L, type = "CONTRIBUTION")
        )

        val metrics = PlannerCalculations.calculateMonthlyAllocations(
            monthlyIncome = 50000L,
            commitments = commitments,
            activeSuggestedGoalContributions = 25000L,
            actualContributionsThisMonth = actualContribs
        )

        assertEquals(-5000L, metrics.actualUnallocated)
        assertEquals(-5000L, metrics.plannedUnallocated)
    }
}
