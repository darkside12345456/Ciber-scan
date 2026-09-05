package com.jp.privacyscanner.data.bugbounty

/** Estado de um achado no ciclo de vida de uma submissão de bug bounty. */
enum class FindingStatus(val label: String) {
    DRAFT("Rascunho"),
    REPORTED("Submetido"),
    TRIAGED("Em triagem"),
    RESOLVED("Resolvido"),
    DUPLICATE("Duplicado"),
    NOT_APPLICABLE("Não aplicável");

    companion object {
        fun fromNameOrDefault(name: String?): FindingStatus =
            entries.firstOrNull { it.name == name } ?: DRAFT
    }
}
