package com.jp.privacyscanner.domain

import android.content.Context
import com.jp.privacyscanner.data.local.AppDatabase
import com.jp.privacyscanner.data.local.ScoreHistory
import com.jp.privacyscanner.data.model.AppInfo
import com.jp.privacyscanner.data.model.RiskLevel
import com.jp.privacyscanner.data.scanner.AppScanner
import com.jp.privacyscanner.data.scoring.ScoringEngine
import kotlinx.coroutines.flow.Flow

/**
 * Ponto único de acesso à lógica de dados: corre o scan, calcula o score
 * global e guarda o histórico. Mantém as ViewModels finas.
 */
class PrivacyRepository(context: Context) {

    private val scanner = AppScanner(context)
    private val historyDao = AppDatabase.get(context).scoreHistoryDao()

    /** Resultado completo de uma análise ao dispositivo. */
    data class ScanResult(
        val apps: List<AppInfo>,
        val globalScore: Int
    )

    suspend fun runScan(includeSystemApps: Boolean = false): ScanResult {
        val apps = scanner.scanInstalledApps(includeSystemApps)
        val global = ScoringEngine.globalScore(apps.map { it.privacyScore })
        val risky = apps.count { it.riskLevel == RiskLevel.HIGH || it.riskLevel == RiskLevel.CRITICAL }
        historyDao.insert(
            ScoreHistory(
                timestamp = System.currentTimeMillis(),
                globalScore = global,
                analyzedApps = apps.size,
                riskyApps = risky
            )
        )
        return ScanResult(apps, global)
    }

    fun history(): Flow<List<ScoreHistory>> = historyDao.recent()
}
