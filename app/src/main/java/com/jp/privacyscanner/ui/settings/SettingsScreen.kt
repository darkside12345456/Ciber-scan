package com.jp.privacyscanner.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jp.privacyscanner.data.ai.AiSettings

/**
 * Ecrã de Definições. Neste momento centra-se na gestão do assistente de IA —
 * o único ponto onde a app pode enviar dados para fora. Dá ao utilizador
 * controlo total: ver estado, definir/trocar e apagar a chave.
 */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val aiSettings = remember { AiSettings(context) }

    var key by remember { mutableStateOf(aiSettings.apiKey) }
    var configured by remember { mutableStateOf(aiSettings.isConfigured) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Definições") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Assistente de IA",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        if (configured) "● Ativo — chave configurada" else "○ Inativo — sem chave",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "O assistente melhora os rascunhos de relatório de bug bounty via API " +
                            "do Claude, com a tua chave. É a ÚNICA funcionalidade da app que usa " +
                            "a internet: o texto do relatório é enviado para a Anthropic. Tudo o " +
                            "resto — scanner, scoring, monitorização — é 100% local. A chave fica " +
                            "guardada cifrada no dispositivo e nunca vai para backups.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = key,
                        onValueChange = { key = it },
                        label = { Text("Chave da API (sk-ant-…)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            enabled = key.isNotBlank(),
                            onClick = {
                                aiSettings.apiKey = key
                                configured = aiSettings.isConfigured
                                Toast.makeText(context, "Chave guardada", Toast.LENGTH_SHORT).show()
                            }
                        ) { Text("Guardar") }
                        OutlinedButton(
                            enabled = configured,
                            onClick = {
                                aiSettings.clear()
                                key = ""
                                configured = false
                                Toast.makeText(context, "Chave removida", Toast.LENGTH_SHORT).show()
                            }
                        ) { Text("Apagar chave") }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "Privacidade",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Card(Modifier.fillMaxWidth()) {
                Text(
                    "Esta app foi desenhada segundo o princípio privacy by design: a análise " +
                        "das tuas apps e permissões é feita inteiramente no dispositivo e nunca " +
                        "é enviada para lado nenhum.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}
