package com.jp.privacyscanner.data.bugbounty

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Um programa de bug bounty em que o investigador participa. O âmbito é
 * guardado como texto (uma entrada por linha) para simplicidade.
 *
 * @param completedChecklist ids de itens da checklist já concluídos, em CSV.
 */
@Entity(tableName = "bounty_program")
data class BountyProgram(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val platform: String = "",
    val inScope: String = "",
    val outOfScope: String = "",
    val policyUrl: String = "",
    val completedChecklist: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val completedIds: Set<String>
        get() = completedChecklist.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()
}
