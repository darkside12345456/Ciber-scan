package com.jp.privacyscanner.ui.detail

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.jp.privacyscanner.data.model.AppInfo
import com.jp.privacyscanner.data.model.PermissionInfo
import com.jp.privacyscanner.data.recommendations.Recommendation
import com.jp.privacyscanner.data.recommendations.RecommendationEngine
import com.jp.privacyscanner.ui.components.RiskChip
import com.jp.privacyscanner.ui.components.ScoreGauge
import com.jp.privacyscanner.ui.theme.colorFor
import com.jp.privacyscanner.util.SettingsNavigator

@Composable
fun AppDetailScreen(
    app: AppInfo?,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(app?.appName ?: "Detalhe") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        if (app == null) {
            Column(
                Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) { Text("App não encontrada. Corre a análise de novo.") }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { HeaderCard(app) }
            item {
                Button(
                    onClick = { SettingsNavigator.openAppDetails(context, app.packageName) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Gerir permissões nas Definições")
                }
            }

            val recommendations = RecommendationEngine.forApp(app)
            if (recommendations.isNotEmpty()) {
                item { SectionTitle("Recomendações") }
                items(recommendations) { RecommendationCard(it) }
            } else {
                item { AllClearCard() }
            }

            val sensitive = app.permissions
                .filter { it.riskLevel != com.jp.privacyscanner.data.model.RiskLevel.LOW }
                .sortedWith(
                    compareByDescending<PermissionInfo> { it.granted }
                        .thenByDescending { it.riskLevel.ordinal }
                )

            if (sensitive.isNotEmpty()) {
                item { SectionTitle("Permissões sensíveis") }
                items(sensitive) { PermissionCard(it) }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun HeaderCard(app: AppInfo) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScoreGauge(score = app.privacyScore, size = 88)
            Spacer(Modifier.width(16.dp))
            Column {
                RiskChip(app.riskLevel)
                Spacer(Modifier.height(6.dp))
                Text(
                    app.packageName,
                    style = MaterialTheme.typography.bodySmall
                )
                app.versionName?.let {
                    Text("Versão $it", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "${app.permissions.size} permissões declaradas",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun RecommendationCard(rec: Recommendation) {
    val color = colorFor(rec.severity)
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            // Faixa lateral colorida a sinalizar a urgência.
            Spacer(
                Modifier
                    .width(6.dp)
                    .height(72.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                    .background(color)
            )
            Column(Modifier.padding(12.dp)) {
                Text(
                    rec.title.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                Spacer(Modifier.height(4.dp))
                Text(rec.rationale, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun AllClearCard() {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Sem ações urgentes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Esta app não tem permissões sensíveis concedidas que justifiquem alterações. Bom sinal.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun PermissionCard(perm: PermissionInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    perm.category.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                RiskChip(perm.riskLevel)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                if (perm.granted) "● Concedida" else "○ Declarada (não concedida)",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(6.dp))
            Text(perm.explanation, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
