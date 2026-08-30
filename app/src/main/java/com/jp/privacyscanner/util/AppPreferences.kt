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

    private companion object {
        const val KEY_ONBOARDING = "onboarding_completed"
    }
}
