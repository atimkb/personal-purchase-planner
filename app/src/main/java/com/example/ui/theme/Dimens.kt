package com.example.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Dimens {
    // Spacing Scale
    val spacingXxs: Dp = 2.dp
    val spacingXs: Dp = 4.dp
    val spacingSm: Dp = 8.dp
    val spacingMd: Dp = 16.dp
    val spacingLg: Dp = 24.dp
    val spacingXl: Dp = 32.dp
    val spacingXxl: Dp = 40.dp

    // Interactive Component Minimum Dimensions & Content Paddings
    val buttonMinHeight: Dp = 48.dp
    val buttonHorizontalPadding: Dp = 16.dp
    val buttonVerticalPadding: Dp = 12.dp
    val buttonContentPadding = PaddingValues(horizontal = buttonHorizontalPadding, vertical = buttonVerticalPadding)

    val chipMinHeight: Dp = 36.dp
    val chipHorizontalPadding: Dp = 12.dp
    val chipVerticalPadding: Dp = 8.dp
    val chipContentPadding = PaddingValues(horizontal = chipHorizontalPadding, vertical = chipVerticalPadding)

    val dropdownMinHeight: Dp = 48.dp

    // Common Icon Sizes
    val iconSizeSm: Dp = 18.dp
    val iconSizeMd: Dp = 24.dp
    val iconSizeLg: Dp = 32.dp

    // Corner Radii
    val cardCornerRadius: Dp = 16.dp
    val buttonCornerRadius: Dp = 12.dp
}
