package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.GoalWithCalculations
import com.example.ui.screens.DetailStatRow
import com.example.ui.theme.SuccessGreen
import com.example.util.PlannerCalculations

@Composable
fun GoalCompletedDialog(
    goalCalc: GoalWithCalculations,
    currencySymbol: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("goal_completed_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Victory Checkmark Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(SuccessGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Text(
                    text = "Goal Completed! 🎉",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = goalCalc.goal.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailStatRow(
                        label = "Total Contributed",
                        value = PlannerCalculations.formatCurrency(goalCalc.totalContributed, currencySymbol)
                    )

                    DetailStatRow(
                        label = "Final Value",
                        value = PlannerCalculations.formatCurrency(goalCalc.currentValue, currencySymbol)
                    )

                    val gain = goalCalc.gainAmount
                    val pcent = if (goalCalc.totalContributed > 0) (gain / goalCalc.totalContributed) * 100 else 0.0
                    DetailStatRow(
                        label = "Investment Gain",
                        value = "+${PlannerCalculations.formatCurrency(gain, currencySymbol)} (${String.format("%.1f", pcent)}%)",
                        valueColor = SuccessGreen
                    )

                    val purchasePrice = goalCalc.goal.finalPurchasePrice ?: goalCalc.goal.targetPrice
                    DetailStatRow(
                        label = "Purchase Price",
                        value = PlannerCalculations.formatCurrency(purchasePrice, currencySymbol)
                    )

                    val remainingSurplus = goalCalc.currentValue - purchasePrice
                    DetailStatRow(
                        label = "Remaining Surplus",
                        value = PlannerCalculations.formatCurrency(remainingSurplus, currencySymbol),
                        valueColor = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Great!",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
