package com.jp.privacyscanner.ui.bugbounty

import androidx.compose.ui.graphics.Color
import com.jp.privacyscanner.data.bugbounty.Severity

/** Cor associada a cada severidade de achado. */
fun severityColor(severity: Severity): Color = when (severity) {
    Severity.INFO -> Color(0xFF6B7280)
    Severity.LOW -> Color(0xFF2E9E5B)
    Severity.MEDIUM -> Color(0xFFE0A200)
    Severity.HIGH -> Color(0xFFE8710A)
    Severity.CRITICAL -> Color(0xFFD93636)
}
