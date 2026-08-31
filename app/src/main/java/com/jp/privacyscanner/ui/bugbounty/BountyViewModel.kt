package com.jp.privacyscanner.ui.bugbounty

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jp.privacyscanner.data.bugbounty.BountyFinding
import com.jp.privacyscanner.data.bugbounty.BountyProgram
import com.jp.privacyscanner.data.bugbounty.BountyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/** ViewModel único para todo o fluxo de bug bounty. */
class BountyViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = BountyRepository(app)

    fun programs(): Flow<List<BountyProgram>> = repo.programs()
    fun program(id: Long): Flow<BountyProgram?> = repo.program(id)
    fun findings(programId: Long): Flow<List<BountyFinding>> = repo.findings(programId)
    fun finding(id: Long): Flow<BountyFinding?> = repo.finding(id)

    fun addProgram(name: String, platform: String) = viewModelScope.launch {
        repo.saveProgram(BountyProgram(name = name.trim(), platform = platform.trim()))
    }

    fun updateProgram(program: BountyProgram) = viewModelScope.launch {
        repo.updateProgram(program)
    }

    fun deleteProgram(program: BountyProgram) = viewModelScope.launch {
        repo.deleteProgram(program)
    }

    fun toggleChecklist(program: BountyProgram, itemId: String) = viewModelScope.launch {
        repo.toggleChecklistItem(program, itemId)
    }

    fun saveFinding(finding: BountyFinding, onSaved: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = repo.saveFinding(finding)
        onSaved(id)
    }

    fun deleteFinding(finding: BountyFinding) = viewModelScope.launch {
        repo.deleteFinding(finding)
    }
}
