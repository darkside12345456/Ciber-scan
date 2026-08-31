package com.jp.privacyscanner.data.bugbounty

/**
 * Severidade de um achado (finding), alinhada com a escala habitual das
 * plataformas de bug bounty. A ordem enum vai do menor para o maior impacto.
 */
enum class Severity(val label: String) {
    INFO("Informativa"),
    LOW("Baixa"),
    MEDIUM("Média"),
    HIGH("Alta"),
    CRITICAL("Crítica");

    companion object {
        fun fromNameOrDefault(name: String?): Severity =
            entries.firstOrNull { it.name == name } ?: MEDIUM
    }
}
