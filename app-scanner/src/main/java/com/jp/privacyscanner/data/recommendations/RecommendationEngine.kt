package com.jp.privacyscanner.data.recommendations

import com.jp.privacyscanner.data.model.AppInfo
import com.jp.privacyscanner.data.model.PermissionCategory
import com.jp.privacyscanner.data.model.PermissionInfo
import com.jp.privacyscanner.data.model.RiskLevel

/**
 * Gera recomendações acionáveis a partir da análise de uma app.
 *
 * Regra: só recomendamos rever permissões que a app *efetivamente detém*
 * (concedidas) e que são sensíveis (risco médio ou superior). Permissões
 * apenas declaradas não geram ruído. As recomendações vêm ordenadas da mais
 * urgente para a menos urgente.
 */
object RecommendationEngine {

    fun forApp(app: AppInfo): List<Recommendation> {
        return app.permissions
            .asSequence()
            .filter { it.granted && it.riskLevel >= RiskLevel.MEDIUM }
            .distinctBy { it.category }              // uma recomendação por categoria
            .map { toRecommendation(it) }
            .sortedByDescending { it.severity.ordinal }
            .toList()
    }

    private fun toRecommendation(perm: PermissionInfo): Recommendation {
        val verb = when (perm.riskLevel) {
            RiskLevel.CRITICAL -> "Desliga já"
            RiskLevel.HIGH -> "Considera desligar"
            else -> "Revê"
        }
        return Recommendation(
            category = perm.category,
            severity = perm.riskLevel,
            title = "$verb: ${perm.category.label.lowercase()}",
            rationale = actionHint(perm.category) + " " + perm.explanation
        )
    }

    /** Pista de ação específica por categoria, à frente da explicação genérica. */
    private fun actionHint(category: PermissionCategory): String = when (category) {
        PermissionCategory.LOCATION ->
            "Se esta app não precisa de saber onde estás, retira-lhe a localização (ou muda para \"só ao usar\")."
        PermissionCategory.MICROPHONE ->
            "Se não fazes chamadas nem gravas nesta app, desliga o microfone."
        PermissionCategory.CAMERA ->
            "Se não usas a câmara aqui, desliga-a — podes voltar a permitir quando precisares."
        PermissionCategory.CONTACTS ->
            "Retira o acesso aos contactos se a app funciona sem eles."
        PermissionCategory.SMS ->
            "Acesso a SMS é raramente legítimo — retira já, salvo se for a tua app de mensagens."
        PermissionCategory.CALL_LOG ->
            "Retira o acesso ao registo de chamadas se a app não é de telefone/contactos."
        PermissionCategory.PHONE ->
            "Revê o acesso ao telefone; poucas apps precisam mesmo dele."
        PermissionCategory.STORAGE ->
            "Limita o acesso a ficheiros ao estritamente necessário."
        PermissionCategory.CALENDAR ->
            "Retira o acesso ao calendário se a app não gere eventos."
        PermissionCategory.BODY_SENSORS ->
            "Dados de saúde são sensíveis — mantém o acesso só em apps de saúde/fitness de confiança."
        else ->
            "Revê se esta permissão é mesmo necessária para o que usas na app."
    }
}
