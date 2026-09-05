package com.jp.privacyscanner

import com.jp.privacyscanner.data.model.PermissionCategory
import com.jp.privacyscanner.data.model.PermissionInfo
import com.jp.privacyscanner.data.model.RiskLevel
import com.jp.privacyscanner.data.scoring.ScoringEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Testes ao motor de scoring. Documentam e travam o comportamento esperado
 * dos critérios de pontuação — o "cérebro" da app.
 */
class ScoringEngineTest {

    private fun perm(
        category: PermissionCategory,
        risk: RiskLevel,
        granted: Boolean
    ) = PermissionInfo(
        rawName = "android.permission.TEST",
        granted = granted,
        category = category,
        riskLevel = risk,
        explanation = ""
    )

    @Test
    fun `app sem permissoes tem score maximo`() {
        assertEquals(100, ScoringEngine.scoreForApp(emptyList()))
    }

    @Test
    fun `permissao concedida penaliza mais do que apenas declarada`() {
        val granted = ScoringEngine.scoreForApp(
            listOf(perm(PermissionCategory.LOCATION, RiskLevel.HIGH, granted = true))
        )
        val declaredOnly = ScoringEngine.scoreForApp(
            listOf(perm(PermissionCategory.LOCATION, RiskLevel.HIGH, granted = false))
        )
        assertTrue("Concedida deve ter score inferior", granted < declaredOnly)
    }

    @Test
    fun `permissoes criticas concedidas baixam bastante o score`() {
        val score = ScoringEngine.scoreForApp(
            listOf(
                perm(PermissionCategory.SMS, RiskLevel.CRITICAL, granted = true),
                perm(PermissionCategory.LOCATION, RiskLevel.CRITICAL, granted = true),
                perm(PermissionCategory.MICROPHONE, RiskLevel.HIGH, granted = true)
            )
        )
        assertTrue("Esperado score de alto risco, obtido $score", score < 55)
    }

    @Test
    fun `score global reflete o elo mais fraco`() {
        // Uma app muito arriscada no meio de apps seguras deve puxar o score
        // para baixo mais do que uma média simples faria.
        val simpleAverage = listOf(100, 100, 100, 10).average().toInt() // 77
        val global = ScoringEngine.globalScore(listOf(100, 100, 100, 10))
        assertTrue("Ponderado ($global) deve ser <= média simples ($simpleAverage)", global <= simpleAverage)
    }

    @Test
    fun `score global de lista vazia e 100`() {
        assertEquals(100, ScoringEngine.globalScore(emptyList()))
    }
}
