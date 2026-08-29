package com.jp.privacyscanner.ui.theme

import androidx.compose.ui.graphics.Color
import com.jp.privacyscanner.data.model.RiskLevel

// Paleta base
val Primary = Color(0xFF1B5E8C)
val PrimaryDark = Color(0xFF0D3B5C)
val Secondary = Color(0xFF2E7D6B)

// Cores por nível de risco — usadas em scores, chips e barras.
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
