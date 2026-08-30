package com.jp.privacyscanner

import android.app.Application
import com.jp.privacyscanner.data.monitoring.MonitoringNotifier

/**
 * Classe Application. Cria o canal de notificações usado pela monitorização
 * contínua em segundo plano.
 */
class PrivacyScannerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MonitoringNotifier.ensureChannel(this)
    }
}
