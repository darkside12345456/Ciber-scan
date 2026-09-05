package com.jp.privacyscanner.util

import android.content.Context

/**
 * Preferências locais simples (SharedPreferences). Guarda apenas flags de UI
 * (onboarding visto, monitorização ligada) — não guarda dados que descrevam o
 * dispositivo. O snapshot de permissões, que descreve as apps instaladas, vive
 * em Room (ver PermissionSnapshotEntry) precisamente por ser dado sensível e
 * potencialmente grande.
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

    private companion object {
        const val KEY_ONBOARDING = "onboarding_completed"
        const val KEY_MONITORING = "monitoring_enabled"
    }
}
