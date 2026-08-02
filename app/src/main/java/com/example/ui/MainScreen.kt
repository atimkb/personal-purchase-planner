package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import com.example.util.PlannerCalculations
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.data.Goal
import com.example.ui.dialogs.AddContributionDialog
import com.example.ui.dialogs.AddGoalDialog
import com.example.ui.dialogs.GoalCompletedDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GoalDetailScreen
import com.example.ui.screens.GoalsListScreen
import com.example.ui.screens.PlanScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PurchasePlannerTheme

sealed class NavDestination(val label: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Home : NavDestination("Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Goals : NavDestination("Goals", Icons.Filled.Flag, Icons.Outlined.Flag)
    object Plan : NavDestination("Plan", Icons.Filled.Calculate, Icons.Outlined.Calculate)
    object Settings : NavDestination("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainScreen(viewModel: PlannerViewModel) {
    val state by viewModel.uiState.collectAsState()

    val isDarkTheme = when (state.userSettings.themeMode) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
    }

    PurchasePlannerTheme(darkTheme = isDarkTheme) {
        var selectedNavIndex by remember { mutableIntStateOf(0) }
        val destinations = listOf(
            NavDestination.Home,
            NavDestination.Goals,
            NavDestination.Plan,
            NavDestination.Settings
        )

        var selectedGoalIdForDetail by remember { mutableStateOf<Int?>(null) }
        var showAddEditGoalScreen by remember { mutableStateOf(false) }
        var goalToEdit by remember { mutableStateOf<Goal?>(null) }
        var goalForContribution by remember { mutableStateOf<Goal?>(null) }
        var goalCompletedForCelebration by remember { mutableStateOf<GoalWithCalculations?>(null) }
        var goalToDeleteId by remember { mutableStateOf<Int?>(null) }

        // Back Gesture Handling:
        BackHandler(enabled = showAddEditGoalScreen) {
            showAddEditGoalScreen = false
            goalToEdit = null
        }

        BackHandler(enabled = selectedGoalIdForDetail != null) {
            selectedGoalIdForDetail = null
        }

        BackHandler(enabled = selectedNavIndex != 0 && !showAddEditGoalScreen && selectedGoalIdForDetail == null) {
            selectedNavIndex = 0
        }

        val activeDetailGoalCalc = selectedGoalIdForDetail?.let { id ->
            state.goals.find { it.goal.id == id }
        }

        Scaffold(
            bottomBar = {
                if (selectedGoalIdForDetail == null && !showAddEditGoalScreen) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        destinations.forEachIndexed { index, dest ->
                            val isSelected = selectedNavIndex == index
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedNavIndex = index },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (showAddEditGoalScreen) {
                    com.example.ui.screens.AddEditGoalScreen(
                        currencySymbol = state.userSettings.currencySymbol,
                        existingGoal = goalToEdit,
                        availableMonthlyCapacity = state.dashboardSummary.availableThisMonth,
                        onBackClick = {
                            showAddEditGoalScreen = false
                            goalToEdit = null
                        },
                        onSaveGoal = { goal ->
                            if (goal.id == 0) {
                                viewModel.addGoal(goal)
                            } else {
                                viewModel.updateGoal(goal)
                            }
                            showAddEditGoalScreen = false
                            goalToEdit = null
                        }
                    )
                } else if (activeDetailGoalCalc != null) {
                    GoalDetailScreen(
                        goalCalc = activeDetailGoalCalc,
                        currencySymbol = state.userSettings.currencySymbol,
                        onBackClick = { selectedGoalIdForDetail = null },
                        onAddContributionClick = { goalForContribution = activeDetailGoalCalc.goal },
                        onEditGoalClick = {
                            goalToEdit = activeDetailGoalCalc.goal
                            showAddEditGoalScreen = true
                        },
                        onDeleteGoalClick = { goalToDeleteId = activeDetailGoalCalc.goal.id },
                        onPauseResumeClick = {
                            if (activeDetailGoalCalc.goal.status == "PAUSED") {
                                viewModel.resumeGoal(activeDetailGoalCalc.goal.id)
                            } else {
                                viewModel.pauseGoal(activeDetailGoalCalc.goal.id)
                            }
                        },
                        onCompleteGoalClick = {
                            viewModel.completeGoal(
                                activeDetailGoalCalc.goal.id,
                                activeDetailGoalCalc.goal.targetPrice
                            )
                            goalCompletedForCelebration = activeDetailGoalCalc
                        },
                        onDeleteContributionClick = { contribId ->
                            viewModel.deleteContribution(contribId)
                        }
                    )
                } else {
                    when (selectedNavIndex) {
                        0 -> DashboardScreen(
                            state = state,
                            onGoalClick = { goalId -> selectedGoalIdForDetail = goalId },
                            onNewGoalClick = {
                                goalToEdit = null
                                showAddEditGoalScreen = true
                            },
                            onMonthSelect = { month -> viewModel.setSelectedMonthYear(month) }
                        )
                        1 -> GoalsListScreen(
                            state = state,
                            onGoalClick = { goalId -> selectedGoalIdForDetail = goalId },
                            onNewGoalClick = {
                                goalToEdit = null
                                showAddEditGoalScreen = true
                            }
                        )
                        2 -> PlanScreen(
                            state = state,
                            onAddCommitment = { viewModel.addCommitment(it) },
                            onDeleteCommitment = { viewModel.deleteCommitment(it) },
                            onClearAllCommitments = { viewModel.clearAllCommitments() }
                        )
                        3 -> SettingsScreen(
                            state = state,
                            onUpdateUserSettings = { viewModel.updateUserSettings(it) },
                            onResetSampleData = { viewModel.resetSampleData() },
                            onClearAllData = { viewModel.clearAllData() }
                        )
                    }
                }
            }
        }

        // Dialogs
        if (goalForContribution != null) {
            val targetGoal = goalForContribution!!
            val goalCalc = state.goals.find { it.goal.id == targetGoal.id }
            val recommendedAmountPaise = goalCalc?.suggestedMonthlyContribution ?: run {
                val months = PlannerCalculations.calculateMonthsRemaining(targetGoal.targetDateEpochMillis)
                PlannerCalculations.calculateMonthlyRequiredContribution(
                    targetPrice = targetGoal.targetPrice,
                    currentValue = targetGoal.alreadySavedAmount,
                    monthsRemaining = months,
                    expectedReturnRatePcent = targetGoal.expectedReturnRate
                )
            }
            val finalSuggestedPaise = if (recommendedAmountPaise > 0L) recommendedAmountPaise else {
                val remaining = maxOf(0L, targetGoal.targetPrice - targetGoal.alreadySavedAmount)
                if (remaining > 0L) remaining else 100000L
            }

            AddContributionDialog(
                goal = targetGoal,
                currencySymbol = state.userSettings.currencySymbol,
                suggestedAmount = finalSuggestedPaise,
                onDismiss = { goalForContribution = null },
                onAddContribution = { contrib ->
                    viewModel.addContribution(contrib)
                    goalForContribution = null
                }
            )
        }

        if (goalCompletedForCelebration != null) {
            GoalCompletedDialog(
                goalCalc = goalCompletedForCelebration!!,
                currencySymbol = state.userSettings.currencySymbol,
                onDismiss = { goalCompletedForCelebration = null }
            )
        }

        if (goalToDeleteId != null) {
            AlertDialog(
                onDismissRequest = { goalToDeleteId = null },
                title = { Text("Delete Goal?") },
                text = { Text("Are you sure you want to delete this goal and its contribution history?") },
                confirmButton = {
                    Button(onClick = {
                        val id = goalToDeleteId!!
                        viewModel.deleteGoal(id)
                        goalToDeleteId = null
                        if (selectedGoalIdForDetail == id) {
                            selectedGoalIdForDetail = null
                        }
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { goalToDeleteId = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
