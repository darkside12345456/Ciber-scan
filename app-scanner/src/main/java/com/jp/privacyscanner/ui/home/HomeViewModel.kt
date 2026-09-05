package com.jp.privacyscanner.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jp.privacyscanner.data.model.AppInfo
import com.jp.privacyscanner.data.monitoring.MonitoringScheduler
import com.jp.privacyscanner.domain.PrivacyRepository
import com.jp.privacyscanner.util.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Estado observável do ecrã principal. */
data class HomeUiState(
    val isScanning: Boolean = false,
    val hasScanned: Boolean = false,
    val globalScore: Int = 100,
    val apps: List<AppInfo> = emptyList(),
    val includeSystemApps: Boolean = false,
    val monitoringEnabled: Boolean = false,
    val error: String? = null
) {
    val riskyAppCount: Int
        get() = apps.count { it.privacyScore < 55 }
}

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = PrivacyRepository(app)
    private val prefs = AppPreferences(app)

    private val _uiState = MutableStateFlow(
        HomeUiState(monitoringEnabled = prefs.monitoringEnabled)
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun scan() {
        if (_uiState.value.isScanning) return
        _uiState.value = _uiState.value.copy(isScanning = true, error = null)
        viewModelScope.launch {
            runCatching {
                repository.runScan(includeSystemApps = _uiState.value.includeSystemApps)
            }.onSuccess { result ->
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    hasScanned = true,
                    globalScore = result.globalScore,
                    apps = result.apps
                )
            }.onFailure { e ->
                _uiState.value = _uiState.value.copy(
                    isScanning = false,
                    error = e.message ?: "Ocorreu um erro durante a análise."
                )
            }
        }
    }

    fun toggleSystemApps(include: Boolean) {
        _uiState.value = _uiState.value.copy(includeSystemApps = include)
        if (_uiState.value.hasScanned) scan()
    }

    fun findApp(packageName: String): AppInfo? =
        _uiState.value.apps.firstOrNull { it.packageName == packageName }

    /** Liga/desliga a monitorização contínua em segundo plano. */
    fun toggleMonitoring(enabled: Boolean) {
        prefs.monitoringEnabled = enabled
        _uiState.value = _uiState.value.copy(monitoringEnabled = enabled)
        val context = getApplication<Application>()
        if (enabled) MonitoringScheduler.enable(context) else MonitoringScheduler.disable(context)
    }
}
