package com.jp.privacyscanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jp.privacyscanner.data.bugbounty.BountyDao
import com.jp.privacyscanner.data.bugbounty.BountyFinding
import com.jp.privacyscanner.data.bugbounty.BountyProgram

@Database(
    entities = [ScoreHistory::class, BountyProgram::class, BountyFinding::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scoreHistoryDao(): ScoreHistoryDao
    abstract fun bountyDao(): BountyDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "privacy_scanner.db"
                )
                    // App recente e dados apenas locais: recriar em mudança de
                    // esquema é aceitável e evita migrações manuais nesta fase.
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
