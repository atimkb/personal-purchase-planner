package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Geometric Balance Color Palette (Emerald, Slate, Amber)
val EmeraldPrimary = Color(0xFF059669)       // Deep Emerald Green #059669
val EmeraldPrimaryLight = Color(0xFF10B981)  // Emerald Green #10B981
val EmeraldSecondary = Color(0xFF34D399)     // Bright Mint Emerald #34D399
val EmeraldContainer = Color(0xFFECFDF5)     // Light Emerald Tint #ECFDF5

val SlateDark = Color(0xFF0F172A)            // Slate 900 #0F172A
val Slate800 = Color(0xFF1E293B)             // Slate 800 #1E293B
val Slate700 = Color(0xFF334155)             // Slate 700 #334155
val Slate400 = Color(0xFF94A3B8)             // Slate 400 #94A3B8
val Slate200 = Color(0xFFE2E8F0)             // Slate 200 #E2E8F0
val Slate100 = Color(0xFFF1F5F9)             // Slate 100 #F1F5F9
val Slate50 = Color(0xFFF8FAFC)              // Slate 50 #F8FAFC

val AmberAccent = Color(0xFFF59E0B)          // Amber 500 #F59E0B
val AmberContainer = Color(0xFFFFFBEB)       // Amber 50 #FFFBEB

// Backwards compatibility aliases for existing Composables
val IndigoPrimary = EmeraldPrimary
val IndigoPrimaryVariant = EmeraldPrimaryLight
val IndigoSecondary = EmeraldSecondary
val IndigoPrimaryDark = EmeraldSecondary
val IndigoSecondaryDark = EmeraldPrimaryLight

val BackgroundLight = Slate50
val SurfaceLight = Color.White
val SurfaceVariantLight = Slate100
val TextPrimaryLight = SlateDark
val TextSecondaryLight = Color(0xFF334155) // Slate 700 high contrast
val BorderLight = Slate200

val BackgroundDark = SlateDark
val SurfaceDark = Slate800
val SurfaceVariantDark = Slate700
val TextPrimaryDark = Slate50
val TextSecondaryDark = Color(0xFFCBD5E1) // Slate 300 boosted contrast
val BorderDark = Slate700

// Status & Accent Colors
val SuccessGreen = EmeraldPrimaryLight
val SuccessGreenBg = EmeraldContainer
val WarningAmber = AmberAccent
val DangerRed = Color(0xFFEF4444)
val InfoBlue = Color(0xFF3B82F6)

val CardGradientsStart = EmeraldPrimary
val CardGradientsEnd = Color(0xFF047857)

