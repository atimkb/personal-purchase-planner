package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import com.example.ui.theme.ComponentTextStyles
import com.example.ui.theme.Dimens
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.Goal
import com.example.ui.theme.DangerRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.WarningAmber
import com.example.util.PlannerCalculations

enum class GoalStep(val stepNumber: Int, val title: String) {
    NAME_AND_PRICE(1, "Goal Details"),
    TIMELINE_AND_CATEGORY(2, "Timeline & Category"),
    STRATEGY(3, "Expected Return"),
    SUMMARY(4, "Projection & Confirm")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
@Composable
fun AddEditGoalScreen(
    currencySymbol: String,
    existingGoal: Goal? = null,
    availableMonthlyCapacity: Long = 1800000L, // in paise (₹18,000)
    onBackClick: () -> Unit,
    onSaveGoal: (Goal) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(1) }
    var name by remember { mutableStateOf(existingGoal?.name ?: "") }
    var targetPriceText by remember { mutableStateOf(existingGoal?.let { (it.targetPrice / 100L).toString() } ?: "") }
    var alreadySavedText by remember { mutableStateOf(existingGoal?.let { (it.alreadySavedAmount / 100L).toString() } ?: "0") }
    var targetMonths by remember { mutableIntStateOf(6) }
    var expectedReturn by remember { mutableFloatStateOf(existingGoal?.expectedReturnRate?.toFloat() ?: 8.0f) }
    var selectedCategory by remember { mutableStateOf(existingGoal?.category ?: "Electronics") }
    var selectedPriority by remember { mutableStateOf(existingGoal?.priority ?: "MEDIUM") }

    val categories = listOf("Kitchen", "Electronics", "Vehicle", "Travel", "Fashion", "Home", "Other")
    val priorities = listOf("HIGH", "MEDIUM", "LOW")

    val targetPricePaise = (targetPriceText.toLongOrNull() ?: 0L) * 100L
    val alreadySavedPaise = (alreadySavedText.toLongOrNull() ?: 0L) * 100L

    // Dynamic Calculations
    val requiredMonthlyPaise = PlannerCalculations.calculateMonthlyRequiredContribution(
        targetPrice = targetPricePaise,
        currentValue = alreadySavedPaise,
        monthsRemaining = maxOf(1, targetMonths),
        expectedReturnRatePcent = expectedReturn.toLong()
    )

    val projectedFinalPaise = PlannerCalculations.calculateProjectedTargetValue(
        currentValue = alreadySavedPaise,
        monthlyContribution = requiredMonthlyPaise,
        monthsRemaining = maxOf(1, targetMonths),
        expectedReturnRatePcent = expectedReturn.toLong()
    )

    val totalSavedAndInvestedPaise = alreadySavedPaise + (requiredMonthlyPaise * targetMonths)
    val estimatedGrowthPaise = projectedFinalPaise - totalSavedAndInvestedPaise

    val step1Valid = name.isNotBlank() && targetPricePaise > 0L
    val exceedsCapacity = requiredMonthlyPaise > availableMonthlyCapacity && availableMonthlyCapacity > 0L

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
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.spacingMd),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                            contentPadding = Dimens.buttonContentPadding,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = Dimens.buttonMinHeight)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconSizeSm)
                            )
                            Spacer(modifier = Modifier.width(Dimens.spacingXs))
                            Text("Previous", style = ComponentTextStyles.secondaryButton)
                        }
                    }

                    if (currentStep < 4) {
                        Button(
                            onClick = { currentStep++ },
                            enabled = if (currentStep == 1) step1Valid else true,
                            shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = Dimens.buttonContentPadding,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = Dimens.buttonMinHeight)
                        ) {
                            Text("Next", style = ComponentTextStyles.primaryButton)
                            Spacer(modifier = Modifier.width(Dimens.spacingXs))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(Dimens.iconSizeSm)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (step1Valid) {
                                    val targetEpoch = System.currentTimeMillis() + (targetMonths.toLong() * 30 * 24 * 60 * 60 * 1000)
                                    val goalToSave = existingGoal?.copy(
                                        name = name.trim(),
                                        targetPrice = targetPricePaise,
                                        alreadySavedAmount = alreadySavedPaise,
                                        targetDateEpochMillis = targetEpoch,
                                        expectedReturnRate = expectedReturn.toLong(),
                                        category = selectedCategory,
                                        priority = selectedPriority
                                    ) ?: Goal(
                                        name = name.trim(),
                                        targetPrice = targetPricePaise,
                                        alreadySavedAmount = alreadySavedPaise,
                                        targetDateEpochMillis = targetEpoch,
                                        expectedReturnRate = expectedReturn.toLong(),
                                        category = selectedCategory,
                                        priority = selectedPriority
                                    )
                                    onSaveGoal(goalToSave)
                                }
                            },
                            enabled = step1Valid,
                            shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = Dimens.buttonContentPadding,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = Dimens.buttonMinHeight)
                                .testTag("save_goal_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(Dimens.iconSizeSm))
                            Spacer(modifier = Modifier.width(Dimens.spacingXs))
                            Text(
                                text = if (existingGoal == null) "Create Goal" else "Save Changes",
                                style = ComponentTextStyles.primaryButton
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Step Progress Indicator Bar
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Step $currentStep of 4: ${GoalStep.entries[currentStep - 1].title}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${(currentStep * 25)}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LinearProgressIndicator(
                        progress = { currentStep / 4f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    )
                }
            }

            // Animated Step Content
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "GoalStepTransition"
            ) { step ->
                when (step) {
                    1 -> Step1GoalDetailsContent(
                        name = name,
                        onNameChange = { name = it },
                        targetPriceText = targetPriceText,
                        onTargetPriceChange = { targetPriceText = it },
                        alreadySavedText = alreadySavedText,
                        onAlreadySavedChange = { alreadySavedText = it },
                        currencySymbol = currencySymbol
                    )
                    2 -> Step2TimelineCategoryContent(
                        targetMonths = targetMonths,
                        onMonthsChange = { targetMonths = it },
                        categories = categories,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        priorities = priorities,
                        selectedPriority = selectedPriority,
                        onPrioritySelected = { selectedPriority = it }
                    )
                    3 -> Step3ReturnStrategyContent(
                        expectedReturn = expectedReturn,
                        onReturnChange = { expectedReturn = it },
                        targetMonths = targetMonths,
                        requiredMonthlyPaise = requiredMonthlyPaise,
                        availableMonthlyCapacity = availableMonthlyCapacity,
                        targetPricePaise = targetPricePaise,
                        alreadySavedPaise = alreadySavedPaise,
                        currencySymbol = currencySymbol,
                        onExtendMonths = { targetMonths = it }
                    )
                    4 -> Step4SummaryProjectionContent(
                        name = name,
                        targetPricePaise = targetPricePaise,
                        alreadySavedPaise = alreadySavedPaise,
                        targetMonths = targetMonths,
                        expectedReturn = expectedReturn,
                        category = selectedCategory,
                        priority = selectedPriority,
                        requiredMonthlyPaise = requiredMonthlyPaise,
                        projectedFinalPaise = projectedFinalPaise,
                        estimatedGrowthPaise = estimatedGrowthPaise,
                        currencySymbol = currencySymbol
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Step1GoalDetailsContent(
    name: String,
    onNameChange: (String) -> Unit,
    targetPriceText: String,
    onTargetPriceChange: (String) -> Unit,
    alreadySavedText: String,
    onAlreadySavedChange: (String) -> Unit,
    currencySymbol: String
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "What are you saving for?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Goal Name") },
                placeholder = { Text("e.g. New Laptop, Travel, Bike") },
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
                onValueChange = onTargetPriceChange,
                label = { Text("Target Price ($currencySymbol)") },
                placeholder = { Text("e.g. 50000") },
                prefix = { Text("$currencySymbol ") },
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
                onValueChange = onAlreadySavedChange,
                label = { Text("Already Saved Amount ($currencySymbol)") },
                placeholder = { Text("0") },
                prefix = { Text("$currencySymbol ") },
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step2TimelineCategoryContent(
    targetMonths: Int,
    onMonthsChange: (Int) -> Unit,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    priorities: List<String>,
    selectedPriority: String,
    onPrioritySelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Target Timeline Card
        Card(
            shape = RoundedCornerShape(20.dp),
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
                    Text(
                        text = "Target Horizon",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    val yearsText = if (targetMonths >= 12) {
                        val yrs = targetMonths / 12f
                        if (targetMonths % 12 == 0) "${targetMonths / 12} Yrs" else "%.1f Yrs".format(yrs)
                    } else null

                    Text(
                        text = if (yearsText != null) "$targetMonths Months ($yearsText)" else "$targetMonths Months",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Slider(
                        value = targetMonths.toFloat(),
                        onValueChange = { onMonthsChange(it.toInt().coerceIn(1, 60)) },
                        valueRange = 1f..60f,
                        steps = 58,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 Month", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("2.5 Years (30m)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("5 Years (60m)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Quick Preset Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(3 to "3 mos", 6 to "6 mos", 12 to "1 yr", 18 to "1.5 yrs", 24 to "2 yrs", 36 to "3 yrs", 60 to "5 yrs")
                    presets.forEach { (months, label) ->
                        val isSelected = targetMonths == months
                        FilterChip(
                            selected = isSelected,
                            onClick = { onMonthsChange(months) },
                            label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                // Estimated Maturity Date Badge
                val targetEpoch = System.currentTimeMillis() + (targetMonths.toLong() * 30 * 24 * 60 * 60 * 1000)
                val formattedTargetDate = PlannerCalculations.formatMonthYear(targetEpoch)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Estimated Maturity Date",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formattedTargetDate,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Category and Priority Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Category & Priority",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Column {
                    Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { onCategorySelected(cat) },
                                label = { Text(cat, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                Column {
                    Text("Priority Level", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        priorities.forEach { prio ->
                            val isSelected = selectedPriority == prio
                            FilterChip(
                                selected = isSelected,
                                onClick = { onPrioritySelected(prio) },
                                label = { Text(prio, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step3ReturnStrategyContent(
    expectedReturn: Float,
    onReturnChange: (Float) -> Unit,
    targetMonths: Int,
    requiredMonthlyPaise: Long,
    availableMonthlyCapacity: Long,
    targetPricePaise: Long,
    alreadySavedPaise: Long,
    currencySymbol: String,
    onExtendMonths: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(20.dp),
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
                    Text(
                        text = "Expected Return Rate",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${expectedReturn.toInt()}% p.a.",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Slider(
                    value = expectedReturn,
                    onValueChange = onReturnChange,
                    valueRange = 0f..20f,
                    steps = 19,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Return Presets",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val returnPresets = listOf(
                        0f to "0% (Cash / Savings)",
                        4f to "4% (Savings A/C)",
                        7f to "7% (FD / RD)",
                        10f to "10% (Balanced Mutual Fund)",
                        12f to "12% (Equity SIP)",
                        15f to "15% (Stocks)"
                    )
                    returnPresets.forEach { (rate, label) ->
                        val isSelected = expectedReturn == rate
                        FilterChip(
                            selected = isSelected,
                            onClick = { onReturnChange(rate) },
                            label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // Short Horizon Warning
        if (targetMonths <= 6 && expectedReturn > 0f) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = WarningAmber.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, WarningAmber.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Short Timeline Note: Market investments fluctuate over short horizons (<6 mos). Consider keeping short-term funds in liquid high-yield savings.",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Capacity Exceeded Warning
        val exceedsCapacity = requiredMonthlyPaise > availableMonthlyCapacity && availableMonthlyCapacity > 0L
        if (exceedsCapacity) {
            val diffPaise = requiredMonthlyPaise - availableMonthlyCapacity
            val suggestedMonths = if (availableMonthlyCapacity > 0L) {
                val rem = targetPricePaise - alreadySavedPaise
                ((rem + availableMonthlyCapacity - 1L) / availableMonthlyCapacity).toInt().coerceAtLeast(targetMonths + 1)
            } else targetMonths + 6

            Surface(
                shape = RoundedCornerShape(16.dp),
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
                            text = "Exceeds Available Monthly Capacity",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = DangerRed
                        )
                    }

                    Text(
                        text = "Required contribution of ${PlannerCalculations.formatCurrency(requiredMonthlyPaise, currencySymbol)}/mo exceeds your available monthly capacity of ${PlannerCalculations.formatCurrency(availableMonthlyCapacity, currencySymbol)}/mo by ${PlannerCalculations.formatCurrency(diffPaise, currencySymbol)}/mo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = { onExtendMonths(suggestedMonths) },
                        shape = RoundedCornerShape(Dimens.buttonCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = Dimens.buttonContentPadding,
                        modifier = Modifier.heightIn(min = Dimens.buttonMinHeight)
                    ) {
                        Text(
                            text = "Extend to $suggestedMonths Months to Fit",
                            style = ComponentTextStyles.primaryButton
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Step4SummaryProjectionContent(
    name: String,
    targetPricePaise: Long,
    alreadySavedPaise: Long,
    targetMonths: Int,
    expectedReturn: Float,
    category: String,
    priority: String,
    requiredMonthlyPaise: Long,
    projectedFinalPaise: Long,
    estimatedGrowthPaise: Long,
    currencySymbol: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Goal Highlights Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Goal Summary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Goal Name", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Target Price", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(PlannerCalculations.formatCurrency(targetPricePaise, currencySymbol), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Initial Savings", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(PlannerCalculations.formatCurrency(alreadySavedPaise, currencySymbol), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Target Timeline", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$targetMonths Months", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Category & Priority", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$category • $priority", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        // Wealth Projection Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Wealth Projection",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${expectedReturn.toInt()}% p.a. return",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Required Monthly SIP:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = PlannerCalculations.formatCurrency(requiredMonthlyPaise, currencySymbol) + " / mo",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Projection Breakdown at Maturity ($targetMonths mos)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val totalVal = maxOf(1L, projectedFinalPaise)
                        val initialWeight = (alreadySavedPaise.toFloat() / totalVal.toFloat()).coerceIn(0f, 1f)
                        val sipWeight = ((requiredMonthlyPaise * targetMonths).toFloat() / totalVal.toFloat()).coerceIn(0f, 1f)
                        val growthWeight = (maxOf(0L, estimatedGrowthPaise).toFloat() / totalVal.toFloat()).coerceIn(0f, 1f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                        ) {
                            if (initialWeight > 0f) {
                                Surface(
                                    color = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier
                                        .weight(initialWeight)
                                        .fillMaxHeight()
                                ) {}
                            }
                            if (sipWeight > 0f) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .weight(sipWeight)
                                        .fillMaxHeight()
                                ) {}
                            }
                            if (growthWeight > 0f) {
                                Surface(
                                    color = SuccessGreen,
                                    modifier = Modifier
                                        .weight(growthWeight)
                                        .fillMaxHeight()
                                ) {}
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Initial Saved", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(PlannerCalculations.formatCurrency(alreadySavedPaise, currencySymbol), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total SIP Contributions ($targetMonths mos)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(PlannerCalculations.formatCurrency(requiredMonthlyPaise * targetMonths, currencySymbol), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }

                        if (estimatedGrowthPaise > 0L) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Estimated Growth (${expectedReturn.toInt()}% p.a.)", style = MaterialTheme.typography.bodySmall, color = SuccessGreen)
                                Text("+" + PlannerCalculations.formatCurrency(estimatedGrowthPaise, currencySymbol), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = SuccessGreen)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Projected Wealth", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                                Text(PlannerCalculations.formatCurrency(projectedFinalPaise, currencySymbol), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
