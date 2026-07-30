package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Notifications
import com.example.ui.theme.EmeraldPrimary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GoalWithCalculations
import com.example.ui.PlannerUiState
import com.example.ui.components.GoalCategoryIcon
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.SuccessGreen
import com.example.util.PlannerCalculations
import java.util.Calendar

@Composable
fun DashboardScreen(
    state: PlannerUiState,
    onGoalClick: (Int) -> Unit,
    onNewGoalClick: () -> Unit,
    onViewAllOverviewClick: () -> Unit,
    onMonthSelect: (String) -> Unit
) {
    var showMonthDropdown by remember { mutableStateOf(false) }
    val monthsList = listOf("July 2026", "August 2026", "September 2026", "October 2026", "November 2026", "December 2026")

    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val salutation = when (currentHour) {
        in 0..11 -> "Good morning,"
        in 12..16 -> "Good afternoon,"
        else -> "Good evening,"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Header with Salutation and Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$salutation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = state.userSettings.userName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(text = " 👋", fontSize = 20.sp)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { showMonthDropdown = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = state.selectedMonthYear,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select month",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showMonthDropdown,
                                onDismissRequest = { showMonthDropdown = false }
                            ) {
                                monthsList.forEach { month ->
                                    DropdownMenuItem(
                                        text = { Text(month) },
                                        onClick = {
                                            onMonthSelect(month)
                                            showMonthDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(onClick = { /* Notification */ }) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Dashboard Main Card (Income & Available)
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                                    text = PlannerCalculations.formatCurrency(
                                        state.dashboardSummary.monthlyIncome,
                                        state.userSettings.currencySymbol
                                    ),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Circular Progress Badge
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(70.dp)
                            ) {
                                val progress = (state.dashboardSummary.availablePercentage / 100f).coerceIn(0f, 1f)
                                val animatedProgress by animateFloatAsState(
                                    targetValue = progress,
                                    animationSpec = tween(durationMillis = 800),
                                    label = "progress"
                                )

                                CircularProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                    strokeWidth = 7.dp,
                                    strokeCap = StrokeCap.Round
                                )

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${state.dashboardSummary.availablePercentage}%",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "of income",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Column {
                            Text(
                                text = "Available this month",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = PlannerCalculations.formatCurrency(
                                    state.dashboardSummary.availableThisMonth,
                                    state.userSettings.currencySymbol
                                ),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Allocated vs Remaining footer bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Allocated",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = PlannerCalculations.formatCurrency(
                                        state.dashboardSummary.totalAllocated,
                                        state.userSettings.currencySymbol
                                    ),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(30.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            )

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = PlannerCalculations.formatCurrency(
                                        state.dashboardSummary.availableThisMonth,
                                        state.userSettings.currencySymbol
                                    ),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Custom Income vs Goal Distribution Chart Card
            item {
                IncomeVsGoalDistributionChart(state = state)
            }

            // Quick Overview Section
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Overview",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = onViewAllOverviewClick) {
                            Text("View All", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val income = state.dashboardSummary.monthlyIncome
                    val goalPcent = if (income > 0) ((state.dashboardSummary.totalGoalAllocations / income) * 100).toInt() else 0
                    val commitPcent = if (income > 0) ((state.dashboardSummary.totalCommitments / income) * 100).toInt() else 0
                    val otherPcent = if (income > 0) ((state.dashboardSummary.totalOtherAllocations / income) * 100).toInt() else 0

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OverviewItemRow(
                            icon = Icons.Default.Flag,
                            title = "Goals",
                            subtitle = "$goalPcent% of income allocated",
                            amount = PlannerCalculations.formatCurrency(
                                state.dashboardSummary.totalGoalAllocations,
                                state.userSettings.currencySymbol
                            ),
                            iconBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            iconTint = MaterialTheme.colorScheme.primary,
                            onClick = onViewAllOverviewClick
                        )

                        OverviewItemRow(
                            icon = Icons.Default.Autorenew,
                            title = "Recurring",
                            subtitle = "$commitPcent% of income",
                            amount = PlannerCalculations.formatCurrency(
                                state.dashboardSummary.totalCommitments,
                                state.userSettings.currencySymbol
                            ),
                            iconBg = SuccessGreen.copy(alpha = 0.15f),
                            iconTint = SuccessGreen,
                            onClick = onViewAllOverviewClick
                        )

                        OverviewItemRow(
                            icon = Icons.Default.Category,
                            title = "Other",
                            subtitle = "$otherPcent% of income",
                            amount = PlannerCalculations.formatCurrency(
                                state.dashboardSummary.totalOtherAllocations,
                                state.userSettings.currencySymbol
                            ),
                            iconBg = Color(0xFFF3E8FF),
                            iconTint = Color(0xFF9333EA),
                            onClick = onViewAllOverviewClick
                        )
                    }
                }
            }

            // YOUR GOALS Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR GOALS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(onClick = onNewGoalClick) {
                        Text("+ New Goal", fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (state.activeGoals.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "No Purchase Goals Active",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Set up your target purchase, monthly budget, and growth timeline to calculate your exact required savings!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onNewGoalClick,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Create Your First Goal", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                items(state.activeGoals) { goalCalc ->
                    DashboardGoalCard(
                        goalCalc = goalCalc,
                        currencySymbol = state.userSettings.currencySymbol,
                        onClick = { onGoalClick(goalCalc.goal.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button for New Goal
        FloatingActionButton(
            onClick = onNewGoalClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_goal_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Goal")
        }
    }
}

@Composable
fun OverviewItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    amount: String,
    iconBg: Color,
    iconTint: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(iconBg, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = amount,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DashboardGoalCard(
    goalCalc: GoalWithCalculations,
    currencySymbol: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("goal_item_${goalCalc.goal.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GoalCategoryIcon(
                        goalName = goalCalc.goal.name,
                        category = goalCalc.goal.category
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = goalCalc.goal.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${PlannerCalculations.formatCurrency(goalCalc.currentValue, currencySymbol)} / ${PlannerCalculations.formatCurrency(goalCalc.goal.targetPrice, currencySymbol)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Progress Badge Ring
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${goalCalc.progressPercentage}%",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { (goalCalc.progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${goalCalc.monthsRemaining} months left",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = PlannerCalculations.formatDate(goalCalc.goal.targetDateEpochMillis, "30 MMM yyyy"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncomeVsGoalDistributionChart(
    state: PlannerUiState
) {
    val summary = state.dashboardSummary
    val income = summary.monthlyIncome
    val currency = state.userSettings.currencySymbol

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Chart",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Income & Goal Distribution",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Income allocation vs target goal completion",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // High-contrast badge for total monthly income
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = PlannerCalculations.formatCurrency(income, currency),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // High Contrast Legend Badges
            val goalPcent = if (income > 0) ((summary.totalGoalAllocations / income) * 100).toInt() else 0
            val commitPcent = if (income > 0) ((summary.totalCommitments / income) * 100).toInt() else 0
            val unallocated = income - summary.totalAllocated
            val unallocatedPcent = if (income > 0) maxOf(0, ((unallocated / income) * 100).toInt()) else 100

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ChartLegendPill(
                    label = "Goal SIPs",
                    value = "$goalPcent%",
                    color = EmeraldPrimary
                )
                ChartLegendPill(
                    label = "Recurring",
                    value = "$commitPcent%",
                    color = SuccessGreen
                )
                ChartLegendPill(
                    label = "Surplus",
                    value = "$unallocatedPcent%",
                    color = Color(0xFFEAB308)
                )
            }

            // High-Contrast Stacked Allocation Bar Chart Visualization
            val goalWeight = if (income > 0) (summary.totalGoalAllocations / income).toFloat().coerceIn(0f, 1f) else 0f
            val commitWeight = if (income > 0) (summary.totalCommitments / income).toFloat().coerceIn(0f, 1f) else 0f
            val surplusWeight = if (income > 0) ((income - summary.totalAllocated) / income).toFloat().coerceAtLeast(0f) else 1f

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (goalWeight > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(goalWeight)
                            .fillMaxHeight()
                            .background(EmeraldPrimary)
                    )
                }
                if (commitWeight > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(commitWeight)
                            .fillMaxHeight()
                            .background(SuccessGreen)
                    )
                }
                if (surplusWeight > 0f) {
                    Box(
                        modifier = Modifier
                            .weight(surplusWeight)
                            .fillMaxHeight()
                            .background(Color(0xFFEAB308))
                    )
                }
            }

            // Custom Stacked & Comparative Bar Visualization with High-Contrast Text Labels
            val goals = state.activeGoals
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Goal Target Progress vs Allocation",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (goals.isEmpty()) {
                    Text(
                        text = "No active goals created yet. Add a goal to visualize target progress distribution!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    goals.forEach { goalCalc ->
                        GoalChartProgressBarRow(
                            goalCalc = goalCalc,
                            currencySymbol = currency
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChartLegendPill(
    label: String,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ) {
            Text(
                text = "$label: $value",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
fun GoalChartProgressBarRow(
    goalCalc: GoalWithCalculations,
    currencySymbol: String
) {
    val progressFraction = (goalCalc.progressPercentage / 100f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = goalCalc.goal.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(8.dp))

            // High Contrast Label Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = "${goalCalc.progressPercentage}% Saved",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    maxLines = 1,
                    softWrap = false
                )
            }
        }

        // Custom High-Contrast Progress Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressFraction)
                    .clip(RoundedCornerShape(6.dp))
                    .background(EmeraldPrimary)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Saved: ${PlannerCalculations.formatCurrency(goalCalc.currentValue, currencySymbol)}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Target: ${PlannerCalculations.formatCurrency(goalCalc.goal.targetPrice, currencySymbol)}",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f, fill = false),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
