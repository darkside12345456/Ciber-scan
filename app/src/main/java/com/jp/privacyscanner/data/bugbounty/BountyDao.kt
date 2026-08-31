package com.jp.privacyscanner.data.bugbounty

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BountyDao {

    // ---- Programas ----
    @Query("SELECT * FROM bounty_program ORDER BY createdAt DESC")
    fun programs(): Flow<List<BountyProgram>>

    @Query("SELECT * FROM bounty_program WHERE id = :id")
    fun program(id: Long): Flow<BountyProgram?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgram(program: BountyProgram): Long

    @Update
    suspend fun updateProgram(program: BountyProgram)

    @Delete
    suspend fun deleteProgram(program: BountyProgram)

    @Query("DELETE FROM bounty_finding WHERE programId = :programId")
    suspend fun deleteFindingsOfProgram(programId: Long)

    // ---- Achados ----
    @Query("SELECT * FROM bounty_finding WHERE programId = :programId ORDER BY updatedAt DESC")
    fun findings(programId: Long): Flow<List<BountyFinding>>

    @Query("SELECT * FROM bounty_finding WHERE id = :id")
    fun finding(id: Long): Flow<BountyFinding?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFinding(finding: BountyFinding): Long

    @Delete
    suspend fun deleteFinding(finding: BountyFinding)
}
