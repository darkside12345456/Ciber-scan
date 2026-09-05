package com.jp.privacyscanner.data.monitoring

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Liga e desliga a monitorização periódica através do WorkManager.
 *
 * O intervalo mínimo permitido pelo WorkManager é 15 minutos; usamos um valor
 * folgado (por omissão diário) para poupar bateria — a deteção de novas
 * permissões não precisa de ser instantânea.
 */
object MonitoringScheduler {

    private const val WORK_NAME = "privacy_monitoring_periodic"

    fun enable(context: Context, intervalHours: Long = 24) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED) // análise é local
            .build()

        val request = PeriodicWorkRequestBuilder<MonitoringWorker>(
            intervalHours, TimeUnit.HOURS
        ).setConstraints(constraints).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun disable(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
