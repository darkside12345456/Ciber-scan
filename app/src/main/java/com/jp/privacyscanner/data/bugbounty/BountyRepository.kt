package com.jp.privacyscanner.data.bugbounty

import android.content.Context
import com.jp.privacyscanner.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow

/** Acesso aos dados de bug bounty. Tudo é guardado localmente (Room). */
class BountyRepository(context: Context) {

    private val dao = AppDatabase.get(context).bountyDao()

    fun programs(): Flow<List<BountyProgram>> = dao.programs()
    fun program(id: Long): Flow<BountyProgram?> = dao.program(id)
    suspend fun saveProgram(program: BountyProgram): Long = dao.upsertProgram(program)
    suspend fun updateProgram(program: BountyProgram) = dao.updateProgram(program)

    suspend fun deleteProgram(program: BountyProgram) {
        dao.deleteFindingsOfProgram(program.id)
        dao.deleteProgram(program)
    }

    /** Alterna um item da checklist de um programa e persiste. */
    suspend fun toggleChecklistItem(program: BountyProgram, itemId: String) {
        val ids = program.completedIds.toMutableSet()
        if (!ids.add(itemId)) ids.remove(itemId)
        dao.updateProgram(program.copy(completedChecklist = ids.joinToString(",")))
    }

    fun findings(programId: Long): Flow<List<BountyFinding>> = dao.findings(programId)
    fun finding(id: Long): Flow<BountyFinding?> = dao.finding(id)

    suspend fun saveFinding(finding: BountyFinding): Long =
        dao.upsertFinding(finding.copy(updatedAt = System.currentTimeMillis()))

    suspend fun deleteFinding(finding: BountyFinding) = dao.deleteFinding(finding)
}
