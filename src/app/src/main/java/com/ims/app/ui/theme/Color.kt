package com.ims.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary palette
val PrimaryBlue = Color(0xFF2563EB)
val PrimaryBlueDark = Color(0xFF1D4ED8)
val PrimaryBlueLight = Color(0xFF60A5FA)
val PrimaryBlueContainer = Color(0xFFDBEAFE)

// Secondary / Success
val SecondaryGreen = Color(0xFF10B981)
val SecondaryGreenDark = Color(0xFF059669)
val SecondaryGreenLight = Color(0xFF6EE7B7)
val SecondaryGreenContainer = Color(0xFFD1FAE5)

// Danger
val DangerRed = Color(0xFFEF4444)
val DangerRedDark = Color(0xFFDC2626)
val DangerRedLight = Color(0xFFFCA5A5)
val DangerRedContainer = Color(0xFFFEE2E2)

// Warning
val WarningAmber = Color(0xFFF59E0B)
val WarningAmberDark = Color(0xFFD97706)
val WarningAmberLight = Color(0xFFFCD34D)
val WarningAmberContainer = Color(0xFFFEF3C7)

// Background & Surface
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceLight = Color(0xFFF1F5F9)

// Text
val TextMain = Color(0xFF1E293B)
val TextSecondary = Color(0xFF475569)
val TextMuted = Color(0xFF64748B)
val TextOnPrimary = Color(0xFFFFFFFF)

// Dark mode colors
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF334155)
val DarkTextMain = Color(0xFFF1F5F9)
val DarkTextSecondary = Color(0xFFCBD5E1)

// Utility
val DividerColor = Color(0xFFE2E8F0)
val ShimmerBase = Color(0xFFE2E8F0)
val ShimmerHighlight = Color(0xFFF8FAFC)

/**
 * Returns an appropriate color for a grade string, supporting:
 * - GPA grades: A+, A, B+, B, C, D, F
 * - CCE grades: A1, A2, B1, B2, C1, C2, D, E (Needs Improvement)
 * - CWA grades: percentage strings like "85.5%"
 * - Custom/unknown grades: safe PrimaryBlue default
 */
fun gradeColor(grade: String): Color {
    return when {
        // GPA top grades
        grade in listOf("A+", "A") -> SecondaryGreen
        grade in listOf("B+", "B") -> PrimaryBlue
        grade in listOf("C") -> WarningAmber
        grade in listOf("D") -> WarningAmber
        grade == "F" -> DangerRed
        // CCE grades
        grade in listOf("A1", "A2") -> SecondaryGreen
        grade in listOf("B1", "B2") -> PrimaryBlue
        grade in listOf("C1", "C2") -> WarningAmber
        grade.contains("Needs Improvement", ignoreCase = true) -> DangerRed
        // CWA (percentage string like "85.5%")
        grade.endsWith("%") -> {
            val pct = grade.removeSuffix("%").toDoubleOrNull() ?: 0.0
            when {
                pct >= 80.0 -> SecondaryGreen
                pct >= 60.0 -> PrimaryBlue
                pct >= 40.0 -> WarningAmber
                else -> DangerRed
            }
        }
        // Safe default for custom grades
        else -> PrimaryBlue
    }
}
