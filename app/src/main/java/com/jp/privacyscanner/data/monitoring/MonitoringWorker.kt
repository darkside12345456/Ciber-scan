package com.jp.privacyscanner.data.monitoring

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jp.privacyscanner.data.scanner.AppScanner
import com.jp.privacyscanner.util.AppPreferences

/**
 * Executa periodicamente em segundo plano (via WorkManager): faz um scan,
 * compara com o snapshot anterior e, se alguma app passou a ter permissões
 * sensíveis novas, emite uma notificação. Atualiza depois o snapshot.
 *
 * Funcionalidade premium (relatório, secção 3). O gating free/premium é feito
 * a montante, ao decidir agendar ou não o worker.
 */
class MonitoringWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return runCatching {
            val prefs = AppPreferences(applicationContext)
            val apps = AppScanner(applicationContext).scanInstalledApps(includeSystemApps = false)

            val current = MonitoringDiff.buildSnapshot(apps)
            val previous = prefs.permissionSnapshot

            // Na primeira execução não há base de comparação: apenas guardamos.
            if (previous.isNotEmpty()) {
                val changes = MonitoringDiff.newlyGranted(previous, current)
                MonitoringNotifier.notifyChanges(applicationContext, changes)
            }

            prefs.permissionSnapshot = current
            Result.success()
        }.getOrElse {
            // Falhas transitórias (ex.: sistema ocupado) podem ser repetidas.
            Result.retry()
        }
    }
}
