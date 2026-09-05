package com.jp.privacyscanner.ui.theme

import androidx.compose.ui.graphics.Color
import com.jp.privacyscanner.data.model.RiskLevel

// Cores por nível de risco — usadas em scores, chips e barras do Scanner.
// (A paleta base Primary/Secondary vem de :core, em CorePalette.kt.)
val RiskLow = Color(0xFF2E9E5B)
val RiskMedium = Color(0xFFE0A200)
val RiskHigh = Color(0xFFE8710A)
val RiskCritical = Color(0xFFD93636)

/** Devolve a cor associada a um nível de risco. */
fun colorFor(level: RiskLevel): Color = when (level) {
    RiskLevel.LOW -> RiskLow
    RiskLevel.MEDIUM -> RiskMedium
    RiskLevel.HIGH -> RiskHigh
    RiskLevel.CRITICAL -> RiskCritical
}
