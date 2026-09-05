package com.jp.privacyscanner.data.model

/**
 * Representa uma app instalada no dispositivo, com o resultado da análise de
 * privacidade já calculado.
 *
 * @param packageName   Identificador único, ex.: com.whatsapp.
 * @param appName       Nome apresentável ao utilizador.
 * @param isSystemApp   Se é app de sistema (pré-instalada / da plataforma).
 * @param installedAt   Timestamp de instalação (epoch millis), 0 se desconhecido.
 * @param versionName   Versão apresentável, pode ser null.
 * @param permissions   Permissões declaradas, já enriquecidas.
 * @param privacyScore  Score 0–100 (100 = mais privado) calculado pelo motor.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val installedAt: Long,
    val versionName: String?,
    val permissions: List<PermissionInfo>,
    val privacyScore: Int
) {
    val riskLevel: RiskLevel get() = RiskLevel.fromScore(privacyScore)

    /** Permissões sensíveis concedidas — o que interessa mostrar primeiro. */
    val grantedSensitivePermissions: List<PermissionInfo>
        get() = permissions.filter {
            it.granted && it.riskLevel != RiskLevel.LOW
        }.sortedByDescending { it.riskLevel.ordinal }
}
