package com.jp.privacyscanner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SnapshotDao {

    @Query("SELECT entry FROM permission_snapshot")
    suspend fun all(): List<String>

    @Query("DELETE FROM permission_snapshot")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<PermissionSnapshotEntry>)

    /** Substitui todo o snapshot de forma atómica. */
    @Transaction
    suspend fun replaceAll(entries: List<String>) {
        clear()
        insertAll(entries.map { PermissionSnapshotEntry(it) })
    }
}
