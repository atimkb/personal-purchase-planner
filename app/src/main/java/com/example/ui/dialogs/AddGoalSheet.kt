package com.example.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.Goal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGoalDialog(
    currencySymbol: String,
    existingGoal: Goal? = null,
    onDismiss: () -> Unit,
    onSaveGoal: (Goal) -> Unit
) {
    var name by remember { mutableStateOf(existingGoal?.name ?: "") }
    var targetPriceText by remember { mutableStateOf(existingGoal?.let { (it.targetPrice / 100L).toString() } ?: "") }
    var alreadySavedText by remember { mutableStateOf(existingGoal?.let { (it.alreadySavedAmount / 100L).toString() } ?: "0") }
    var targetMonths by remember { mutableIntStateOf(6) }
    var expectedReturn by remember { mutableFloatStateOf(existingGoal?.expectedReturnRate?.toFloat() ?: 8.0f) }
    var selectedCategory by remember { mutableStateOf(existingGoal?.category ?: "Electronics") }
    var selectedPriority by remember { mutableStateOf(existingGoal?.priority ?: "MEDIUM") }

    val categories = listOf("Kitchen", "Electronics", "Vehicle", "Travel", "Fashion", "Home", "Other")
    val priorities = listOf("HIGH", "MEDIUM", "LOW")
    val monthOptions = listOf(3, 6, 12, 18, 24, 36)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_goal_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existingGoal == null) "What are you planning for?" else "Edit Goal",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    placeholder = { Text("e.g. Ice Cream Maker") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("goal_name_input")
                )

                // Target Price
                OutlinedTextField(
                    value = targetPriceText,
                    onValueChange = { targetPriceText = it },
                    label = { Text("Target Price ($currencySymbol)") },
                    placeholder = { Text("5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("goal_price_input")
                )

                // Target Months Selection
                Column {
                    Text(
                        text = "I want it in: $targetMonths months",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        monthOptions.forEach { months ->
                            FilterChip(
                                selected = targetMonths == months,
                                onClick = { targetMonths = months },
                                label = { Text("$months mos") }
                            )
                        }
                    }
                }

                // Already Saved
                OutlinedTextField(
                    value = alreadySavedText,
                    onValueChange = { alreadySavedText = it },
                    label = { Text("Already saved ($currencySymbol)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Expected Return Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Expected annual return",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${expectedReturn.toInt()}% p.a.",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = expectedReturn,
                        onValueChange = { expectedReturn = it },
                        valueRange = 0f..20f,
                        steps = 19
                    )
                }

                // Category Selection
                Column {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                // Priority Selection
                Column {
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        priorities.forEach { prio ->
                            FilterChip(
                                selected = selectedPriority == prio,
                                onClick = { selectedPriority = prio },
                                label = { Text(prio) }
                            )
                        }
                    }
                }

                // Save Goal Button
                val priceRupees = targetPriceText.toLongOrNull() ?: 0L
                val pricePaise = priceRupees * 100L
                val alreadySavedPaise = (alreadySavedText.toLongOrNull() ?: 0L) * 100L
                val isValid = name.isNotBlank() && pricePaise > 0L

                Button(
                    onClick = {
                        if (isValid) {
                            val targetEpoch = System.currentTimeMillis() + (targetMonths.toLong() * 30 * 24 * 60 * 60 * 1000)
                            val goalToSave = existingGoal?.copy(
                                name = name.trim(),
                                targetPrice = pricePaise,
                                alreadySavedAmount = alreadySavedPaise,
                                targetDateEpochMillis = targetEpoch,
                                expectedReturnRate = expectedReturn.toLong(),
                                category = selectedCategory,
                                priority = selectedPriority
                            ) ?: Goal(
                                name = name.trim(),
                                targetPrice = pricePaise,
                                alreadySavedAmount = alreadySavedPaise,
                                targetDateEpochMillis = targetEpoch,
                                expectedReturnRate = expectedReturn.toLong(),
                                category = selectedCategory,
                                priority = selectedPriority
                            )
                            onSaveGoal(goalToSave)
                        }
                    },
                    enabled = isValid,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_goal_btn")
                ) {
                    Text(
                        text = if (existingGoal == null) "Create Goal" else "Save Changes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
