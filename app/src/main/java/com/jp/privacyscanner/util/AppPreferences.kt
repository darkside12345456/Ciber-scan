package com.jp.privacyscanner.util

import android.content.Context

/**
 * Preferências locais simples (SharedPreferences). Guarda apenas flags de UI —
 * nada de dados do utilizador — coerente com o princípio de processamento local.
 */
class AppPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    var onboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING, value).apply()

    /** Se a monitorização contínua em segundo plano está ativa. */
    var monitoringEnabled: Boolean
        get() = prefs.getBoolean(KEY_MONITORING, false)
        set(value) = prefs.edit().putBoolean(KEY_MONITORING, value).apply()

    /**
     * Último snapshot de permissões sensíveis concedidas, usado pelo worker
     * para detetar alterações entre scans. Uma cópia defensiva é devolvida.
     */
    var permissionSnapshot: Set<String>
        get() = prefs.getStringSet(KEY_SNAPSHOT, emptySet())?.toSet() ?: emptySet()
        set(value) = prefs.edit().putStringSet(KEY_SNAPSHOT, value).apply()

    private companion object {
        const val KEY_ONBOARDING = "onboarding_completed"
        const val KEY_MONITORING = "monitoring_enabled"
        const val KEY_SNAPSHOT = "permission_snapshot"
    }
}
