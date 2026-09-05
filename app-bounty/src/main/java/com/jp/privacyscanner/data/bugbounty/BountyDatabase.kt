package com.jp.privacyscanner.data.bugbounty

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** Base de dados da app de Bug Bounty (programas e achados). */
@Database(
    entities = [BountyProgram::class, BountyFinding::class],
    version = 1,
    exportSchema = false
)
abstract class BountyDatabase : RoomDatabase() {

    abstract fun bountyDao(): BountyDao

    companion object {
        @Volatile private var instance: BountyDatabase? = null

        fun get(context: Context): BountyDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    BountyDatabase::class.java,
                    "bug_bounty.db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { instance = it }
            }
    }
}
