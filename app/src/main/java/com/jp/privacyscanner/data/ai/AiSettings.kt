package com.jp.privacyscanner.data.ai

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Guarda a chave da API do Claude de forma cifrada (EncryptedSharedPreferences).
 *
 * A chave nunca sai do dispositivo exceto nos cabeçalhos das chamadas à própria
 * API do Claude, feitas pelo utilizador com a sua conta. Se a cifra falhar
 * (dispositivos raros com Keystore problemático), recorre a preferências
 * normais para não bloquear a funcionalidade — documentado para transparência.
 */
class AiSettings(context: Context) {

    private val prefs: SharedPreferences = runCatching {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "ai_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrElse {
        context.getSharedPreferences("ai_prefs_fallback", Context.MODE_PRIVATE)
    }

    var apiKey: String
        get() = prefs.getString(KEY_API, "").orEmpty()
        set(value) = prefs.edit().putString(KEY_API, value.trim()).apply()

    val isConfigured: Boolean get() = apiKey.isNotBlank()

    fun clear() = prefs.edit().remove(KEY_API).apply()

    private companion object {
        const val KEY_API = "claude_api_key"
    }
}
