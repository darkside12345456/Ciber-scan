package com.jp.privacyscanner

import com.jp.privacyscanner.data.model.AppInfo
import com.jp.privacyscanner.data.model.PermissionCategory
import com.jp.privacyscanner.data.model.PermissionInfo
import com.jp.privacyscanner.data.model.RiskLevel
import com.jp.privacyscanner.data.recommendations.RecommendationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationEngineTest {

    private fun perm(
        raw: String,
        category: PermissionCategory,
        risk: RiskLevel,
        granted: Boolean
    ) = PermissionInfo(raw, granted, category, risk, "explicação")

    private fun app(vararg perms: PermissionInfo) = AppInfo(
        packageName = "com.exemplo",
        appName = "Exemplo",
        isSystemApp = false,
        installedAt = 0,
        versionName = "1.0",
        permissions = perms.toList(),
        privacyScore = 50
    )

    @Test
    fun `so recomenda permissoes concedidas`() {
        val recs = RecommendationEngine.forApp(
            app(perm("A", PermissionCategory.CAMERA, RiskLevel.HIGH, granted = false))
        )
        assertTrue("Permissão não concedida não deve gerar recomendação", recs.isEmpty())
    }

    @Test
    fun `ignora permissoes de baixo risco`() {
        val recs = RecommendationEngine.forApp(
            app(perm("A", PermissionCategory.NETWORK, RiskLevel.LOW, granted = true))
        )
        assertTrue(recs.isEmpty())
    }

    @Test
    fun `recomendacoes vem ordenadas por severidade`() {
        val recs = RecommendationEngine.forApp(
            app(
                perm("A", PermissionCategory.CONTACTS, RiskLevel.MEDIUM, granted = true),
                perm("B", PermissionCategory.SMS, RiskLevel.CRITICAL, granted = true),
                perm("C", PermissionCategory.CAMERA, RiskLevel.HIGH, granted = true)
            )
        )
        assertEquals(3, recs.size)
        assertEquals(RiskLevel.CRITICAL, recs[0].severity)
        assertEquals(RiskLevel.HIGH, recs[1].severity)
        assertEquals(RiskLevel.MEDIUM, recs[2].severity)
    }

    @Test
    fun `uma recomendacao por categoria`() {
        val recs = RecommendationEngine.forApp(
            app(
                perm("A", PermissionCategory.LOCATION, RiskLevel.HIGH, granted = true),
                perm("B", PermissionCategory.LOCATION, RiskLevel.CRITICAL, granted = true)
            )
        )
        assertEquals(1, recs.size)
    }
}
