package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import com.example.ui.theme.ComponentTextStyles
import com.example.ui.theme.Dimens
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GoalWithCalculations
import com.example.ui.components.GoalCategoryIcon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.util.PlannerCalculations

@Composable
fun GoalDetailScreen(
    goalCalc: GoalWithCalculations,
    currencySymbol: String,
    onBackClick: () -> Unit,
    onAddContributionClick: () -> Unit,
    onEditGoalClick: () -> Unit,
    onDeleteGoalClick: () -> Unit,
    onPauseResumeClick: () -> Unit,
    onCompleteGoalClick: () -> Unit,
    onDeleteContributionClick: (Int) -> Unit
) {
    val goal = goalCalc.goal

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoalCategoryIcon(
                        goalName = goal.name,
                        category = goal.category,
                        size = 36.dp,
                        iconSize = 20.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row {
                    IconButton(onClick = onEditGoalClick) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDeleteGoalClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = DangerRed
                        )
                    }
                }
            }
        }

        // Hero Progress Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = PlannerCalculations.formatCurrency(goalCalc.currentValue, currencySymbol),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "of ${PlannerCalculations.formatCurrency(goal.targetPrice, currencySymbol)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { (goalCalc.progressPercentage / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(10.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                            strokeCap = StrokeCap.Round
                        )

                        Text(
                            text = "${goalCalc.progressPercentage}%",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Target Date Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Target Date",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = PlannerCalculations.formatDate(goal.targetDateEpochMillis, "30 MMM yyyy"),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Time Remaining Card
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Time Remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${goalCalc.monthsRemaining} months",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Overview Section Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "FINANCIAL PROGRESS BREAKDOWN",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val fundingPct = if (goal.targetPrice > 0L) ((goalCalc.totalContributed * 100L) / goal.targetPrice).toInt() else 0
                    val valuePct = if (goal.targetPrice > 0L) ((goalCalc.currentValue * 100L) / goal.targetPrice).toInt() else 0

                    DetailStatRow(
                        label = "Funding Progress (Contributed)",
                        value = "$fundingPct% (${PlannerCalculations.formatCurrency(goalCalc.totalContributed, currencySymbol)})"
                    )

                    DetailStatRow(
                        label = "Current Value Progress",
                        value = "$valuePct% (${PlannerCalculations.formatCurrency(goalCalc.currentValue, currencySymbol)})",
                        valueColor = MaterialTheme.colorScheme.primary
                    )

                    val isGainPositive = goalCalc.gainAmount >= 0L
                    val gainText = "${if (isGainPositive) "+" else ""}${PlannerCalculations.formatCurrency(goalCalc.gainAmount, currencySymbol)} investment ${if (isGainPositive) "gain" else "loss"}"
                    DetailStatRow(
                        label = "Investment Return",
                        value = gainText,
                        valueColor = if (isGainPositive) SuccessGreen else DangerRed
                    )
                }
            }
        }

        // Forecast Section Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "FORECAST",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    DetailStatRow(
                        label = "Expected value at target",
                        value = PlannerCalculations.formatCurrency(goalCalc.projectedTargetValue, currencySymbol)
                    )

                    DetailStatRow(
                        label = "Target price",
                        value = PlannerCalculations.formatCurrency(goal.targetPrice, currencySymbol)
                    )

                    val isSurplus = goalCalc.expectedSurplusOrShortfall >= 0L
                    val surplusText = "${if (isSurplus) "+" else ""}${PlannerCalculations.formatCurrency(goalCalc.expectedSurplusOrShortfall, currencySymbol)}"
                    DetailStatRow(
                        label = if (isSurplus) "Expected surplus" else "Expected shortfall",
                        value = surplusText,
                        valueColor = if (isSurplus) SuccessGreen else WarningAmber
                    )
                }
            }
        }

        // Decision Engine Banner (Ready or Behind)
        item {
            if (goalCalc.isTargetReached || goal.status == "COMPLETED") {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = SuccessGreen.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Goal Reached! ✓",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SuccessGreen
                            )
                        }

                        Text(
                            text = "You can now afford this purchase or cash out your returns!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = onCompleteGoalClick,
                                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                                contentPadding = Dimens.buttonContentPadding,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = Dimens.buttonMinHeight)
                            ) {
                                Text("Complete / Cash Out", style = ComponentTextStyles.primaryButton, color = Color.White)
                            }
                        }
                    }
                }
            } else if (goal.status == "PAUSED") {
                Card(
                    shape = RoundedCornerShape(Dimens.cardCornerRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.spacingMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Goal Paused ⏸️",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Monthly allocations are paused.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onPauseResumeClick,
                            shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                            contentPadding = Dimens.buttonContentPadding,
                            modifier = Modifier.heightIn(min = Dimens.buttonMinHeight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconSizeSm)
                            )
                            Spacer(modifier = Modifier.width(Dimens.spacingXs))
                            Text("Resume", style = ComponentTextStyles.primaryButton)
                        }
                    }
                }
            } else {
                // Suggested Monthly Contribution Banner
                Surface(
                    shape = RoundedCornerShape(Dimens.cardCornerRadius),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(Dimens.spacingMd),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "This month suggestion",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${PlannerCalculations.formatCurrency(goalCalc.suggestedMonthlyContribution, currencySymbol)} / month",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        OutlinedButton(
                            onClick = onPauseResumeClick,
                            shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                            contentPadding = Dimens.buttonContentPadding,
                            modifier = Modifier.heightIn(min = Dimens.buttonMinHeight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PauseCircle,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconSizeSm)
                            )
                            Spacer(modifier = Modifier.width(Dimens.spacingXs))
                            Text("Pause", style = ComponentTextStyles.secondaryButton)
                        }
                    }
                }
            }
        }

        // Add Contribution Primary Button
        item {
            Button(
                onClick = onAddContributionClick,
                shape = RoundedCornerShape(Dimens.cardCornerRadius),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = Dimens.buttonContentPadding,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = Dimens.buttonMinHeight)
                    .testTag("add_contribution_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconSizeMd)
                )
                Spacer(modifier = Modifier.width(Dimens.spacingSm))
                Text(
                    text = "+ Add Contribution",
                    style = ComponentTextStyles.primaryButton
                )
            }
        }

        // Contributions History Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "CONTRIBUTIONS HISTORY",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (goalCalc.contributions.isEmpty()) {
            item {
                Text(
                    text = "No recorded contributions yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(goalCalc.contributions) { contrib ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = PlannerCalculations.formatDate(contrib.dateEpochMillis, "d MMMM yyyy"),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = contrib.investmentType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                if (!contrib.note.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "• ${contrib.note}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "+${PlannerCalculations.formatCurrency(contrib.amount, currencySymbol)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            )

                            IconButton(
                                onClick = { onDeleteContributionClick(contrib.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete contribution",
                                    tint = DangerRed.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun DetailStatRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = valueColor
        )
    }
}
