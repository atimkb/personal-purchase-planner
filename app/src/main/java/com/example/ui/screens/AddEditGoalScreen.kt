package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Goal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.util.PlannerCalculations

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditGoalScreen(
    currencySymbol: String,
    existingGoal: Goal? = null,
    availableMonthlyCapacity: Double = 18000.0,
    onBackClick: () -> Unit,
    onSaveGoal: (Goal) -> Unit
) {
    var name by remember { mutableStateOf(existingGoal?.name ?: "") }
    var targetPriceText by remember { mutableStateOf(existingGoal?.targetPrice?.toInt()?.toString() ?: "") }
    var alreadySavedText by remember { mutableStateOf(existingGoal?.alreadySavedAmount?.toInt()?.toString() ?: "0") }
    var targetMonths by remember { mutableIntStateOf(6) }
    var expectedReturn by remember { mutableFloatStateOf(existingGoal?.expectedReturnRate?.toFloat() ?: 8.0f) }
    var selectedCategory by remember { mutableStateOf(existingGoal?.category ?: "Electronics") }
    var selectedPriority by remember { mutableStateOf(existingGoal?.priority ?: "MEDIUM") }
    var showAdvancedSettings by remember { mutableStateOf(false) }

    val categories = listOf("Kitchen", "Electronics", "Vehicle", "Travel", "Fashion", "Home", "Other")
    val priorities = listOf("HIGH", "MEDIUM", "LOW")
    val monthOptions = listOf(3, 6, 12, 18, 24, 36)

    val targetPrice = targetPriceText.toDoubleOrNull() ?: 0.0
    val alreadySaved = alreadySavedText.toDoubleOrNull() ?: 0.0

    // Dynamic Live Calculations based on input
    val requiredMonthly = PlannerCalculations.calculateMonthlyRequiredContribution(
        targetPrice = targetPrice,
        currentValue = alreadySaved,
        monthsRemaining = maxOf(1, targetMonths),
        expectedReturnRatePcent = expectedReturn.toDouble()
    )

    val projectedFinal = PlannerCalculations.calculateProjectedTargetValue(
        currentValue = alreadySaved,
        monthlyContribution = requiredMonthly,
        monthsRemaining = maxOf(1, targetMonths),
        expectedReturnRatePcent = expectedReturn.toDouble()
    )

    val totalSavedAndInvested = alreadySaved + (requiredMonthly * targetMonths)
    val estimatedGrowth = projectedFinal - totalSavedAndInvested

    val isValid = name.isNotBlank() && targetPrice > 0
    val exceedsCapacity = requiredMonthly > availableMonthlyCapacity && availableMonthlyCapacity > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (existingGoal == null) "Create Purchase Goal" else "Edit Goal",
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
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Card 1: Goal Basics
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "1. What are you planning for?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Goal Name") },
                            placeholder = { Text("e.g. New Laptop, PlayStation, Camera") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_name_input")
                        )

                        OutlinedTextField(
                            value = targetPriceText,
                            onValueChange = { targetPriceText = it },
                            label = { Text("Target Price ($currencySymbol)") },
                            placeholder = { Text("e.g. 50000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_price_input")
                        )

                        OutlinedTextField(
                            value = alreadySavedText,
                            onValueChange = { alreadySavedText = it },
                            label = { Text("Already Saved Amount ($currencySymbol)") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Card 2: Timeline
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
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
                            Text(
                                text = "2. Target Timeline",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "$targetMonths Months",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            monthOptions.forEach { months ->
                                val isSelected = targetMonths == months
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { targetMonths = months },
                                    label = {
                                        Text(
                                            text = "$months mos",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        // Short Horizon Risk Warning
                        if (targetMonths <= 6 && expectedReturn > 0.0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = WarningAmber.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Short Timeline Note: Market investments fluctuate over short horizons (<6 mos). Consider keeping short-term funds in high-yield savings.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Card 3: Advanced Planning Settings (Expandable)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAdvancedSettings = !showAdvancedSettings },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Advanced Settings (Expected Return, Category)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (showAdvancedSettings) "Hide ▲" else "Show ▼",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (showAdvancedSettings) {
                            // Return Rate Slider
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Expected Annual Return Assumption",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${expectedReturn.toInt()}% p.a.",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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

                            // Category Chips
                            Column {
                                Text(
                                    text = "Category",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    categories.forEach { cat ->
                                        val isSelected = selectedCategory == cat
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedCategory = cat },
                                            label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                labelColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }

                            // Priority Chips
                            Column {
                                Text(
                                    text = "Priority Level",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    priorities.forEach { prio ->
                                        val isSelected = selectedPriority == prio
                                        FilterChip(
                                            selected = isSelected,
                                            onClick = { selectedPriority = prio },
                                            label = { Text(prio, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                                labelColor = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Affordability Capacity Warning Card ("Can I afford this goal?")
            if (exceedsCapacity) {
                item {
                    val diff = requiredMonthly - availableMonthlyCapacity
                    // Calculate extended months to fit capacity
                    val suggestedMonths = if (availableMonthlyCapacity > 0) {
                        kotlin.math.ceil((targetPrice - alreadySaved) / availableMonthlyCapacity).toInt().coerceAtLeast(targetMonths + 1)
                    } else targetMonths + 6

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = DangerRed.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = DangerRed,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Plan Doesn't Currently Fit Capacity",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DangerRed
                                )
                            }

                            Text(
                                text = "Required contribution of ${PlannerCalculations.formatCurrency(requiredMonthly, currencySymbol)}/mo exceeds your available capacity of ${PlannerCalculations.formatCurrency(availableMonthlyCapacity, currencySymbol)}/mo by ${PlannerCalculations.formatCurrency(diff, currencySymbol)}/mo.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Button(
                                onClick = { targetMonths = suggestedMonths },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = "Extend to $suggestedMonths Months to Fit",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // Card 4: Dynamic Math Live Preview Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Calculation Preview",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Required Monthly Contribution:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = PlannerCalculations.formatCurrency(requiredMonthly, currencySymbol) + " / mo",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (estimatedGrowth > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Projected Target Growth (${expectedReturn.toInt()}% p.a.):",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "+" + PlannerCalculations.formatCurrency(estimatedGrowth, currencySymbol),
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }
            }

            // Save Goal CTA Button
            item {
                Button(
                    onClick = {
                        if (isValid) {
                            val targetEpoch = System.currentTimeMillis() + (targetMonths.toLong() * 30 * 24 * 60 * 60 * 1000)
                            val goalToSave = existingGoal?.copy(
                                name = name.trim(),
                                targetPrice = targetPrice,
                                alreadySavedAmount = alreadySaved,
                                targetDateEpochMillis = targetEpoch,
                                expectedReturnRate = expectedReturn.toDouble(),
                                category = selectedCategory,
                                priority = selectedPriority
                            ) ?: Goal(
                                name = name.trim(),
                                targetPrice = targetPrice,
                                alreadySavedAmount = alreadySaved,
                                targetDateEpochMillis = targetEpoch,
                                expectedReturnRate = expectedReturn.toDouble(),
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
                        .height(54.dp)
                        .testTag("save_goal_btn")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (existingGoal == null) "Create Goal (${PlannerCalculations.formatCurrency(requiredMonthly, currencySymbol)}/mo)" else "Save Changes",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
