package com.jp.privacyscanner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreHistoryDao {

    @Insert
    suspend fun insert(entry: ScoreHistory)

    @Query("SELECT * FROM score_history ORDER BY timestamp DESC LIMIT :limit")
    fun recent(limit: Int = 60): Flow<List<ScoreHistory>>

    @Query("DELETE FROM score_history WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}
