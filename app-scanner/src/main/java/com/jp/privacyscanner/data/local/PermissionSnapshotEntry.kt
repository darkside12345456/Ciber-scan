package com.jp.privacyscanner.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Uma entrada do snapshot de permissões sensíveis concedidas, no formato
 * `packageName|permissaoRaw`. Guardado em Room (e não em SharedPreferences)
 * porque pode ter milhares de entradas — reescrever um Set inteiro nas prefs a
 * cada gravação era caro — e porque descreve o dispositivo do utilizador em
 * detalhe, ficando assim fora dos backups (ver data_extraction_rules.xml).
 */
@Entity(tableName = "permission_snapshot")
data class PermissionSnapshotEntry(
    @PrimaryKey val entry: String
)
