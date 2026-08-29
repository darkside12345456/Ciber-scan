package com.jp.privacyscanner.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Um ponto no histórico da evolução do score global do dispositivo.
 * Alimenta a funcionalidade premium "Histórico" (relatório, secção 3).
 */
@Entity(tableName = "score_history")
data class ScoreHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val globalScore: Int,
    val analyzedApps: Int,
    val riskyApps: Int
)
