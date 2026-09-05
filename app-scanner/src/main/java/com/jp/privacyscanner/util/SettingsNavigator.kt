package com.jp.privacyscanner.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

/**
 * Encaminha o utilizador para o ecrã de definições da app-alvo, onde pode
 * rever e revogar permissões manualmente.
 *
 * Nota importante (relatório, secção 2): NÃO conseguimos revogar permissões de
 * outras apps programaticamente. O máximo — e o correto — é levar o utilizador
 * diretamente ao sítio certo. É isto que este helper faz.
 */
object SettingsNavigator {

    /** Abre a página "Detalhes da app" das definições, para o pacote indicado. */
    fun openAppDetails(context: Context, packageName: String) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching { context.startActivity(intent) }
            .onFailure {
                Toast.makeText(
                    context,
                    "Não foi possível abrir as definições desta app.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}
