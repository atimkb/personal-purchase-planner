package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GoalCategoryIcon(
    goalName: String,
    category: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 26.dp
) {
    val (icon, bgColor, tintColor) = getGoalIconConfig(goalName, category)

    Box(
        modifier = modifier
            .size(size)
            .background(bgColor, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            tint = tintColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

fun getGoalIconConfig(name: String, category: String): Triple<ImageVector, Color, Color> {
    val lowerName = name.lowercase()
    return when {
        lowerName.contains("ice cream") || lowerName.contains("mixer") || lowerName.contains("kitchen") ->
            Triple(Icons.Default.Blender, Color(0xFFFCE7F3), Color(0xFFEC4899)) // Soft pink
        lowerName.contains("laptop") || lowerName.contains("computer") || category.equals("Electronics", ignoreCase = true) ->
            Triple(Icons.Default.Laptop, Color(0xFFE0E7FF), Color(0xFF4F46E5)) // Soft indigo
        lowerName.contains("bike") || lowerName.contains("cycle") ->
            Triple(Icons.Default.PedalBike, Color(0xFFFEF3C7), Color(0xFFD97706)) // Soft amber
        lowerName.contains("car") || category.equals("Vehicle", ignoreCase = true) ->
            Triple(Icons.Default.DirectionsCar, Color(0xFFFEE2E2), Color(0xFFEF4444)) // Soft red
        lowerName.contains("phone") || lowerName.contains("mobile") ->
            Triple(Icons.Default.Smartphone, Color(0xFFE0F2FE), Color(0xFF0284C7)) // Soft sky blue
        lowerName.contains("travel") || lowerName.contains("flight") || category.equals("Travel", ignoreCase = true) ->
            Triple(Icons.Default.FlightTakeoff, Color(0xFFDCFCE7), Color(0xFF16A34A)) // Soft emerald
        lowerName.contains("home") || category.equals("Home", ignoreCase = true) ->
            Triple(Icons.Default.Home, Color(0xFFF3E8FF), Color(0xFF9333EA)) // Soft purple
        lowerName.contains("fashion") || category.equals("Fashion", ignoreCase = true) ->
            Triple(Icons.Default.Checkroom, Color(0xFFFFE4E6), Color(0xFFE11D48)) // Soft rose
        else ->
            Triple(Icons.Default.ShoppingCart, Color(0xFFE2E8F0), Color(0xFF475569)) // Soft slate
    }
}
