package com.jp.privacyscanner.data.monitoring

import android.content.Context
import com.jp.privacyscanner.data.local.AppDatabase

/**
 * Acesso ao snapshot de permissões, agora persistido em Room em vez de
 * SharedPreferences (ver [com.jp.privacyscanner.data.local.PermissionSnapshotEntry]).
 */
class SnapshotStore(context: Context) {

    private val dao = AppDatabase.get(context).snapshotDao()

    suspend fun load(): Set<String> = dao.all().toSet()

    suspend fun save(entries: Set<String>) = dao.replaceAll(entries.toList())
}
