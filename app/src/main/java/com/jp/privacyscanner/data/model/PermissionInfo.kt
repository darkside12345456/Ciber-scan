package com.jp.privacyscanner.data.model

/**
 * Uma permissão declarada por uma app, já enriquecida com metadados do nosso
 * catálogo (categoria, explicação em linguagem simples e nível de risco).
 *
 * @param rawName      Nome técnico Android, ex.: android.permission.CAMERA.
 * @param granted      Se está atualmente concedida ao utilizador.
 * @param category     Categoria de sensibilidade (ver [PermissionCategory]).
 * @param riskLevel    Risco intrínseco desta permissão.
 * @param explanation  Explicação curta e sem jargão do que a permissão permite.
 */
data class PermissionInfo(
    val rawName: String,
    val granted: Boolean,
    val category: PermissionCategory,
    val riskLevel: RiskLevel,
    val explanation: String
) {
    /** Nome legível curto, ex.: "CAMERA" a partir de "android.permission.CAMERA". */
    val shortName: String
        get() = rawName.substringAfterLast('.')
}
