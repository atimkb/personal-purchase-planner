package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Commitment
import com.example.ui.GoalWithCalculations
import com.example.ui.PlannerUiState
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.util.PlannerCalculations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickOverviewScreen(
    state: PlannerUiState,
    onBackClick: () -> Unit,
    onDeleteCommitment: ((Int) -> Unit)? = null,
    onClearAllCommitments: (() -> Unit)? = null
) {
    val summary = state.dashboardSummary
    val baseIncome = summary.monthlyIncome
    val currency = state.userSettings.currencySymbol

    // Interactive Income Simulator state
    var simulatedIncomeFactor by remember { mutableFloatStateOf(1.0f) }
    val income = baseIncome * simulatedIncomeFactor

    val goalPcent = if (income > 0) ((summary.totalGoalAllocations / income) * 100).toInt() else 0
    val commitPcent = if (income > 0) ((summary.totalCommitments / income) * 100).toInt() else 0
    val otherPcent = if (income > 0) ((summary.totalOtherAllocations / income) * 100).toInt() else 0
    val unallocated = income - summary.totalAllocated
    val unallocatedPcent = if (income > 0) maxOf(0, ((unallocated / income) * 100).toInt()) else 100

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Complete Financial Review",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Infographic Donut Chart & Executive Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (summary.isOverAllocated) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Monthly Income",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = PlannerCalculations.formatCurrency(income, currency),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (state.commitments.isNotEmpty() && onClearAllCommitments != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                OutlinedButton(
                                    onClick = { onClearAllCommitments() },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteSweep,
                                        contentDescription = "Clear All",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear Commitments", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }

                        // Donut Ring Chart Infographic
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Canvas Donut
                            Box(
                                modifier = Modifier.size(110.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(100.dp)) {
                                    val strokeWidth = 16.dp.toPx()
                                    val diameter = size.minDimension - strokeWidth
                                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                                    val arcSize = Size(diameter, diameter)

                                    if (income <= 0 || summary.totalAllocated <= 0) {
                                        drawArc(
                                            color = Color.LightGray.copy(alpha = 0.4f),
                                            startAngle = 0f,
                                            sweepAngle = 360f,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth)
                                        )
                                    } else {
                                        var currentAngle = -90f

                                        val gFraction = (summary.totalGoalAllocations / income).toFloat().coerceIn(0f, 1f)
                                        val cFraction = (summary.totalCommitments / income).toFloat().coerceIn(0f, 1f)
                                        val oFraction = (summary.totalOtherAllocations / income).toFloat().coerceIn(0f, 1f)
                                        val uFraction = maxOf(0f, 1f - (gFraction + cFraction + oFraction))

                                        if (gFraction > 0f) {
                                            val sweep = gFraction * 360f
                                            drawArc(color = EmeraldPrimary, startAngle = currentAngle, sweepAngle = sweep, useCenter = false, style = Stroke(width = strokeWidth), topLeft = topLeft, size = arcSize)
                                            currentAngle += sweep
                                        }
                                        if (cFraction > 0f) {
                                            val sweep = cFraction * 360f
                                            drawArc(color = SuccessGreen, startAngle = currentAngle, sweepAngle = sweep, useCenter = false, style = Stroke(width = strokeWidth), topLeft = topLeft, size = arcSize)
                                            currentAngle += sweep
                                        }
                                        if (oFraction > 0f) {
                                            val sweep = oFraction * 360f
                                            drawArc(color = WarningAmber, startAngle = currentAngle, sweepAngle = sweep, useCenter = false, style = Stroke(width = strokeWidth), topLeft = topLeft, size = arcSize)
                                            currentAngle += sweep
                                        }
                                        if (uFraction > 0f) {
                                            val sweep = uFraction * 360f
                                            drawArc(color = Color.LightGray.copy(alpha = 0.4f), startAngle = currentAngle, sweepAngle = sweep, useCenter = false, style = Stroke(width = strokeWidth), topLeft = topLeft, size = arcSize)
                                        }
                                    }
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${100 - unallocatedPcent}%",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Allocated",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OverviewLegendItem(color = EmeraldPrimary, label = "Goals ($goalPcent%)", amount = PlannerCalculations.formatCurrency(summary.totalGoalAllocations, currency))
                                OverviewLegendItem(color = SuccessGreen, label = "Commitments ($commitPcent%)", amount = PlannerCalculations.formatCurrency(summary.totalCommitments, currency))
                                OverviewLegendItem(color = WarningAmber, label = "Other ($otherPcent%)", amount = PlannerCalculations.formatCurrency(summary.totalOtherAllocations, currency))
                                OverviewLegendItem(color = Color.Gray, label = "Unallocated ($unallocatedPcent%)", amount = PlannerCalculations.formatCurrency(maxOf(0.0, unallocated), currency))
                            }
                        }

                        // Surplus Status Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (summary.isOverAllocated) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else SuccessGreen.copy(alpha = 0.12f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (summary.isOverAllocated) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (summary.isOverAllocated) MaterialTheme.colorScheme.error else SuccessGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (summary.isOverAllocated) "Over-allocated Budget" else "Unallocated Monthly Surplus",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = PlannerCalculations.formatCurrency(unallocated, currency),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (summary.isOverAllocated) MaterialTheme.colorScheme.error else SuccessGreen,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Income Sensitivity Simulator Card (EXTRA FEATURE)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Income Scenario Simulator",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${(simulatedIncomeFactor * 100).toInt()}% Scale",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Text(
                            text = "Test how changes in your income affect your savings surplus live:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Slider(
                            value = simulatedIncomeFactor,
                            onValueChange = { simulatedIncomeFactor = it },
                            valueRange = 0.5f..2.0f,
                            steps = 15,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("50% Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Baseline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("200% Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Infographic: Wealth Growth & Accumulation Projection Canvas Chart
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "36-Month Wealth Projection",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Project wealth curve based on active SIPs
                        val monthlyGoalInvest = summary.totalGoalAllocations
                        val projected12Mos = PlannerCalculations.calculateProjectedTargetValue(0.0, monthlyGoalInvest, 12, 9.0)
                        val projected36Mos = PlannerCalculations.calculateProjectedTargetValue(0.0, monthlyGoalInvest, 36, 9.0)

                        Text(
                            text = "Projected accumulation: ${PlannerCalculations.formatCurrency(projected12Mos, currency)} (1 yr) • ${PlannerCalculations.formatCurrency(projected36Mos, currency)} (3 yrs)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Canvas Growth Chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val primaryColor = MaterialTheme.colorScheme.primary
                            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height

                                // Draw baseline horizontal lines
                                drawLine(color = gridColor, start = Offset(0f, h * 0.25f), end = Offset(w, h * 0.25f))
                                drawLine(color = gridColor, start = Offset(0f, h * 0.5f), end = Offset(w, h * 0.5f))
                                drawLine(color = gridColor, start = Offset(0f, h * 0.75f), end = Offset(w, h * 0.75f))

                                // Draw Growth Curve Path
                                val path = Path().apply {
                                    moveTo(0f, h * 0.9f)
                                    cubicTo(
                                        w * 0.35f, h * 0.8f,
                                        w * 0.65f, h * 0.45f,
                                        w, h * 0.1f
                                    )
                                }

                                drawPath(
                                    path = path,
                                    color = primaryColor,
                                    style = Stroke(width = 3.dp.toPx())
                                )
                            }
                        }
                    }
                }
            }

            // Purchase Goals Breakdown
            item {
                ReviewSectionHeader(
                    title = "Active Purchase Goals",
                    subtitle = "Monthly SIP & savings required based on purchase dates",
                    icon = Icons.Default.Flag,
                    amount = PlannerCalculations.formatCurrency(summary.totalGoalAllocations, currency)
                )
            }

            if (state.activeGoals.isEmpty()) {
                item {
                    EmptyReviewCard(message = "No purchase goals created yet. Add goals to calculate accurate monthly contributions!")
                }
            } else {
                items(state.activeGoals) { goalCalc ->
                    GoalReviewRowItem(goalCalc = goalCalc, currency = currency)
                }
            }

            // Fixed Commitments Breakdown
            item {
                ReviewSectionHeader(
                    title = "Monthly Commitments & Bills",
                    subtitle = "Recurring fixed monthly expenses",
                    icon = Icons.Default.CreditCard,
                    amount = PlannerCalculations.formatCurrency(summary.totalCommitments, currency)
                )
            }

            val commitmentsOnly = state.commitments.filter { it.category != "Other" }
            if (commitmentsOnly.isEmpty()) {
                item {
                    EmptyReviewCard(message = "No recurring monthly commitments added. You can add your custom bills or subscriptions anytime!")
                }
            } else {
                items(commitmentsOnly) { commitment ->
                    DeletableCommitmentRow(
                        commitment = commitment,
                        currency = currency,
                        onDeleteClick = { onDeleteCommitment?.invoke(commitment.id) }
                    )
                }
            }

            // Other Expenses
            item {
                ReviewSectionHeader(
                    title = "Other Planned Allocations",
                    subtitle = "Variable or miscellaneous monthly budget allocations",
                    icon = Icons.Default.AccountBalanceWallet,
                    amount = PlannerCalculations.formatCurrency(summary.totalOtherAllocations, currency)
                )
            }

            val otherOnly = state.commitments.filter { it.category == "Other" }
            if (otherOnly.isEmpty()) {
                item {
                    EmptyReviewCard(message = "No other allocations specified.")
                }
            } else {
                items(otherOnly) { commitment ->
                    DeletableCommitmentRow(
                        commitment = commitment,
                        currency = currency,
                        onDeleteClick = { onDeleteCommitment?.invoke(commitment.id) }
                    )
                }
            }

            // Health Metrics Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Financial Health Indicators",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        val savingsRatePcent = if (income > 0) (((summary.totalGoalAllocations + maxOf(0.0, unallocated)) / income) * 100).toInt() else 0
                        HealthMetricRow(
                            label = "Goal Investment Ratio",
                            value = "$goalPcent%",
                            description = "Target: 20-40% for goal accumulation"
                        )

                        HealthMetricRow(
                            label = "Fixed Commitment Ratio",
                            value = "$commitPcent%",
                            description = "Target: Below 50% for healthy cash flow"
                        )

                        HealthMetricRow(
                            label = "Total Potential Savings Rate",
                            value = "$savingsRatePcent%",
                            description = "Combined goals + unallocated surplus"
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun OverviewLegendItem(color: Color, label: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = amount,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
fun ReviewSectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    amount: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun EmptyReviewCard(message: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun GoalReviewRowItem(goalCalc: GoalWithCalculations, currency: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goalCalc.goal.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Target: ${PlannerCalculations.formatCurrency(goalCalc.goal.targetPrice, currency)} • ${goalCalc.monthsRemaining} mos remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${PlannerCalculations.formatCurrency(goalCalc.suggestedMonthlyContribution, currency)} / mo",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "${goalCalc.goal.expectedReturnRate.toInt()}% return rate",
                    style = MaterialTheme.typography.labelSmall,
                    color = SuccessGreen,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}

@Composable
fun DeletableCommitmentRow(
    commitment: Commitment,
    currency: String,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = commitment.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = commitment.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${PlannerCalculations.formatCurrency(commitment.monthlyAmount, currency)} / mo",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Commitment",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun HealthMetricRow(label: String, value: String, description: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
            Text(text = value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        }
        Text(text = description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
    }
}
