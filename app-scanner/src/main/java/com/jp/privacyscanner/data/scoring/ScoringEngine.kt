package com.jp.privacyscanner.data.scoring

import com.jp.privacyscanner.data.model.PermissionInfo
import com.jp.privacyscanner.data.model.RiskLevel

/**
 * Motor de pontuação de privacidade — o "cérebro" da app (relatório, Fase 2).
 *
 * Regra: cada app começa com 100 (perfeitamente privada) e perde pontos por
 * cada permissão sensível que efetivamente detém. Uma permissão apenas
 * *declarada* mas *não concedida* penaliza pouco (risco potencial); uma
 * permissão *concedida* penaliza a totalidade do seu peso (risco real).
 *
 * Os critérios estão centralizados e documentados de propósito: a coerência e
 * a transparência do score são o diferencial face a apps genéricas.
 */
object ScoringEngine {

    /** Multiplicador do peso conforme o nível de risco da permissão. */
    private fun riskMultiplier(level: RiskLevel): Double = when (level) {
        RiskLevel.LOW -> 0.4
        RiskLevel.MEDIUM -> 1.0
        RiskLevel.HIGH -> 1.8
        RiskLevel.CRITICAL -> 2.6
    }

    /** Fração do peso aplicada quando a permissão está apenas declarada, não concedida. */
    private const val DECLARED_ONLY_FACTOR = 0.25

    /**
     * Calcula o score de privacidade (0–100) de uma app a partir das suas
     * permissões já enriquecidas. 100 = mais privado.
     */
    fun scoreForApp(permissions: List<PermissionInfo>): Int {
        var penalty = 0.0
        for (perm in permissions) {
            val base = perm.category.baseWeight * riskMultiplier(perm.riskLevel)
            penalty += if (perm.granted) base else base * DECLARED_ONLY_FACTOR
        }
        return (100 - penalty).coerceIn(0.0, 100.0).toInt()
    }

    /**
     * Score global do dispositivo: média ponderada em que as apps mais
     * arriscadas pesam mais, para o número refletir o "elo mais fraco" em vez
     * de ser diluído por dezenas de apps inócuas.
     */
    fun globalScore(appScores: List<Int>): Int {
        if (appScores.isEmpty()) return 100
        var weightedSum = 0.0
        var weightTotal = 0.0
        for (score in appScores) {
            // Quanto menor o score da app, maior o seu peso no total.
            val weight = 1.0 + (100 - score) / 50.0
            weightedSum += score * weight
            weightTotal += weight
        }
        return (weightedSum / weightTotal).toInt().coerceIn(0, 100)
    }
}
