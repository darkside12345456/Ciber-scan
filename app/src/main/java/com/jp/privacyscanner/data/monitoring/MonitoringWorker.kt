package com.jp.privacyscanner.data.monitoring

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jp.privacyscanner.data.scanner.AppScanner

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
        return try {
            val store = SnapshotStore(applicationContext)
            val apps = AppScanner(applicationContext).scanInstalledApps(includeSystemApps = false)

            val current = MonitoringDiff.buildSnapshot(apps)
            val previous = store.load()

            // Na primeira execução não há base de comparação: apenas guardamos.
            if (previous.isNotEmpty()) {
                val changes = MonitoringDiff.newlyGranted(previous, current)
                MonitoringNotifier.notifyChanges(applicationContext, changes)
            }

            store.save(current)
            Result.success()
        } catch (e: android.os.DeadObjectException) {
            // Sistema momentaneamente indisponível — vale a pena repetir.
            Result.retry()
        } catch (e: android.os.TransactionTooLargeException) {
            Result.retry()
        } catch (e: Exception) {
            // Erros permanentes (ex.: bug de programação) não se resolvem por
            // repetição: devolver failure evita um ciclo infinito a gastar
            // bateria e deixa a falha registada. O trabalho periódico volta a
            // correr no próximo intervalo agendado.
            Result.failure()
        }
    }
}
