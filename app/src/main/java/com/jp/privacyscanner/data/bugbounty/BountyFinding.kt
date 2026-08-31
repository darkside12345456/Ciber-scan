package com.jp.privacyscanner.data.bugbounty

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Um achado (finding) associado a um programa. Os campos seguem a estrutura
 * típica de um relatório de bug bounty, de modo a gerar um rascunho pronto a
 * submeter.
 */
@Entity(
    tableName = "bounty_finding",
    indices = [Index("programId")]
)
data class BountyFinding(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val programId: Long,
    val title: String,
    val severity: String = Severity.MEDIUM.name,
    val status: String = FindingStatus.DRAFT.name,
    val affectedAsset: String = "",
    val description: String = "",
    val steps: String = "",
    val impact: String = "",
    val remediation: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val severityEnum: Severity get() = Severity.fromNameOrDefault(severity)
    val statusEnum: FindingStatus get() = FindingStatus.fromNameOrDefault(status)
}
