package com.jp.privacyscanner.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.privacyscanner.data.model.AppInfo
import com.jp.privacyscanner.ui.components.RiskChip
import com.jp.privacyscanner.ui.components.ScoreGauge

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAppClick: (String) -> Unit,
    onToggleMonitoring: (Boolean) -> Unit,
    bottomBar: @Composable () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Scanner de Privacidade") }) },
        bottomBar = bottomBar,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.scan() },
                icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                text = { Text(if (state.hasScanned) "Analisar de novo" else "Analisar") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            when {
                state.isScanning && state.apps.isEmpty() -> LoadingState()
                !state.hasScanned -> EmptyState()
                else -> ResultsList(state, viewModel, onAppClick, onToggleMonitoring)
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("A analisar as apps instaladas…")
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Analisa a privacidade do teu telemóvel",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Vamos ver que permissões as tuas apps têm e onde estão os riscos. " +
                "Tudo é processado localmente — nada sai do teu dispositivo.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ResultsList(
    state: HomeUiState,
    viewModel: HomeViewModel,
    onAppClick: (String) -> Unit,
    onToggleMonitoring: (Boolean) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { GlobalScoreCard(state) }
        item { MonitoringCard(state, onToggleMonitoring) }
        item { SystemAppsToggle(state, viewModel) }
        item {
            Text(
                "Apps por risco",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(state.apps, key = { it.packageName }) { app ->
            AppRow(app, onClick = { onAppClick(app.packageName) })
        }
        item { Spacer(Modifier.height(80.dp)) } // espaço para o FAB
    }
}

@Composable
private fun GlobalScoreCard(state: HomeUiState) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreGauge(score = state.globalScore, size = 96)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Score de privacidade",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${state.apps.size} apps analisadas",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${state.riskyAppCount} a precisar de atenção",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun MonitoringCard(state: HomeUiState, onToggle: (Boolean) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Monitorização contínua",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Avisa-te quando uma app passa a ter permissões sensíveis.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.width(12.dp))
            Switch(
                checked = state.monitoringEnabled,
                onCheckedChange = onToggle
            )
        }
    }
}

@Composable
private fun SystemAppsToggle(state: HomeUiState, viewModel: HomeViewModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Incluir apps de sistema", modifier = Modifier.weight(1f))
        Switch(
            checked = state.includeSystemApps,
            onCheckedChange = { viewModel.toggleSystemApps(it) }
        )
    }
}

@Composable
private fun AppRow(app: AppInfo, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreGauge(score = app.privacyScore, size = 48)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    app.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${app.grantedSensitivePermissions.size} permissões sensíveis ativas",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            RiskChip(app.riskLevel)
        }
    }
}
