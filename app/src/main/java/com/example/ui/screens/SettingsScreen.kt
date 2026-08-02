package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettings
import com.example.ui.PlannerUiState
import com.example.ui.theme.DangerRed
import com.example.util.PlannerCalculations

@Composable
fun SettingsScreen(
    state: PlannerUiState,
    onUpdateUserSettings: (UserSettings) -> Unit,
    onResetSampleData: () -> Unit,
    onClearAllData: () -> Unit
) {
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showEditIncomeDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showResetDataConfirm by remember { mutableStateOf(false) }
    var showClearDataConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Section: General
        item {
            Text(
                text = "General",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Person,
                        title = "Profile",
                        subtitle = state.userSettings.userName,
                        onClick = { showEditProfileDialog = true }
                    )

                    SettingsRowItem(
                        icon = Icons.Default.AttachMoney,
                        title = "Income & Currency",
                        subtitle = "${PlannerCalculations.formatCurrency(state.userSettings.monthlyIncome, state.userSettings.currencySymbol)} (${state.userSettings.currencySymbol})",
                        onClick = { showEditIncomeDialog = true }
                    )

                    SettingsRowItem(
                        icon = Icons.Default.Palette,
                        title = "Theme",
                        subtitle = state.userSettings.themeMode,
                        onClick = { showThemeDialog = true }
                    )
                }
            }
        }

        // Section: Data Management
        item {
            Text(
                text = "Data Management",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Refresh,
                        title = "Pre-populate Sample Data",
                        subtitle = "Reset goals and sample dataset to demo blueprint state",
                        onClick = { showResetDataConfirm = true }
                    )

                    SettingsRowItem(
                        icon = Icons.Default.DeleteForever,
                        title = "Clear All Data",
                        subtitle = "Wipe all goals, contributions and commitments",
                        titleColor = DangerRed,
                        onClick = { showClearDataConfirm = true }
                    )
                }
            }
        }

        // Section: About
        item {
            Text(
                text = "About",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = "About App",
                        subtitle = "Personal Purchase Planner v1.0",
                        onClick = {}
                    )

                    SettingsRowItem(
                        icon = Icons.Default.Help,
                        title = "Philosophy",
                        subtitle = "Save & invest now, buy later without debt!",
                        onClick = {}
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Dialogs
    if (showEditProfileDialog) {
        var name by remember { mutableStateOf(state.userSettings.userName) }
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile Name") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank()) {
                        onUpdateUserSettings(state.userSettings.copy(userName = name.trim()))
                        showEditProfileDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showEditIncomeDialog) {
        var incomeText by remember { mutableStateOf((state.userSettings.monthlyIncome / 100L).toString()) }
        var currency by remember { mutableStateOf(state.userSettings.currencySymbol) }
        val currencies = listOf("₹", "$", "€", "£", "¥")

        AlertDialog(
            onDismissRequest = { showEditIncomeDialog = false },
            title = { Text("Income & Currency") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = incomeText,
                        onValueChange = { incomeText = it },
                        label = { Text("Monthly Income") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Currency Symbol", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        currencies.forEach { sym ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (currency == sym) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { currency = sym }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = sym,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (currency == sym) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val incomeRupees = incomeText.toLongOrNull() ?: (state.userSettings.monthlyIncome / 100L)
                    val incomePaise = incomeRupees * 100L
                    onUpdateUserSettings(state.userSettings.copy(monthlyIncome = incomePaise, currencySymbol = currency))
                    showEditIncomeDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditIncomeDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showThemeDialog) {
        val themes = listOf("SYSTEM", "LIGHT", "DARK")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    themes.forEach { theme ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateUserSettings(state.userSettings.copy(themeMode = theme))
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = state.userSettings.themeMode == theme,
                                onClick = {
                                    onUpdateUserSettings(state.userSettings.copy(themeMode = theme))
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(theme, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showResetDataConfirm) {
        AlertDialog(
            onDismissRequest = { showResetDataConfirm = false },
            title = { Text("Reset Sample Data?") },
            text = { Text("This will restore default sample goals and dataset matching the app blueprint.") },
            confirmButton = {
                Button(onClick = {
                    onResetSampleData()
                    showResetDataConfirm = false
                }) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDataConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will remove all goals, contributions, and commitments permanently.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDataConfirm = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) { Text("Clear All") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SettingsRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    titleColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = titleColor
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
