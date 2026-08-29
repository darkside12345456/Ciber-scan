package com.jp.privacyscanner.data.model

/**
 * Nível de risco usado tanto para permissões individuais como para o score
 * agregado de uma app. A ordem enum é do menor para o maior risco.
 */
enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL;

    companion object {
        /** Converte um score 0–100 (100 = mais privado) no nível correspondente. */
        fun fromScore(score: Int): RiskLevel = when {
            score >= 80 -> LOW
            score >= 55 -> MEDIUM
            score >= 30 -> HIGH
            else -> CRITICAL
        }
    }
}
