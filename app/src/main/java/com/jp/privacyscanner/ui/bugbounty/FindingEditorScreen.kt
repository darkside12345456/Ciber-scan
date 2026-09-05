package com.jp.privacyscanner.ui.bugbounty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import com.jp.privacyscanner.data.bugbounty.BountyFinding
import com.jp.privacyscanner.data.bugbounty.BountyProgram
import com.jp.privacyscanner.data.ai.AiSettings
import com.jp.privacyscanner.data.ai.ClaudeClient
import com.jp.privacyscanner.data.bugbounty.CvssV31
import com.jp.privacyscanner.data.bugbounty.FindingStatus
import com.jp.privacyscanner.data.bugbounty.ReportGenerator
import com.jp.privacyscanner.data.bugbounty.Severity
import com.jp.privacyscanner.util.PdfExporter
import kotlinx.coroutines.launch

@Composable
fun FindingEditorScreen(
    programId: Long,
    findingId: Long,
    viewModel: BountyViewModel,
    onBack: () -> Unit
) {
    val program by viewModel.program(programId).collectAsStateWithLifecycle(initialValue = null)
    val existing by if (findingId > 0) {
        viewModel.finding(findingId).collectAsStateWithLifecycle(initialValue = null)
    } else {
        remember { mutableStateOf<BountyFinding?>(null) }
    }

    // Espera o carregamento de um achado existente antes de mostrar o formulário.
    if (findingId > 0 && existing == null) {
        Box(Modifier.fillMaxSize()) {}
        return
    }

    FindingEditorContent(
        program = program,
        programId = programId,
        existing = existing,
        onBack = onBack,
        onSave = { finding -> viewModel.saveFinding(finding) { onBack() } }
    )
}

@Composable
private fun FindingEditorContent(
    program: BountyProgram?,
    programId: Long,
    existing: BountyFinding?,
    onBack: () -> Unit,
    onSave: (BountyFinding) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var severity by remember { mutableStateOf(existing?.severityEnum ?: Severity.MEDIUM) }
    var status by remember { mutableStateOf(existing?.statusEnum ?: FindingStatus.DRAFT) }
    var asset by remember { mutableStateOf(existing?.affectedAsset ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var steps by remember { mutableStateOf(existing?.steps ?: "") }
    var impact by remember { mutableStateOf(existing?.impact ?: "") }
    var remediation by remember { mutableStateOf(existing?.remediation ?: "") }
    var cvssVector by remember { mutableStateOf(existing?.cvssVector ?: "") }
    var cvssScore by remember { mutableStateOf(existing?.cvssScore ?: 0.0) }
    var showReport by remember { mutableStateOf(false) }
    var showCvss by remember { mutableStateOf(false) }

    fun build(): BountyFinding = (existing ?: BountyFinding(programId = programId, title = "")).copy(
        programId = programId,
        title = title,
        severity = severity.name,
        status = status.name,
        affectedAsset = asset,
        description = description,
        steps = steps,
        impact = impact,
        remediation = remediation,
        cvssVector = cvssVector,
        cvssScore = cvssScore
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Novo achado" else "Editar achado") },
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
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("Título") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                EnumDropdown(
                    label = "Severidade",
                    current = severity.label,
                    options = Severity.entries.map { it.label to it },
                    onSelected = { severity = it },
                    modifier = Modifier.weight(1f)
                )
                EnumDropdown(
                    label = "Estado",
                    current = status.label,
                    options = FindingStatus.entries.map { it.label to it },
                    onSelected = { status = it },
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = asset, onValueChange = { asset = it },
                label = { Text("Ativo afetado (URL/endpoint)") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedButton(onClick = { showCvss = true }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (cvssScore > 0.0) {
                        "CVSS v3.1: $cvssScore (${CvssV31.severityLabel(cvssScore)})"
                    } else {
                        "Calcular CVSS v3.1"
                    }
                )
            }

            MultiField("Descrição", description) { description = it }
            MultiField("Passos para reproduzir", steps) { steps = it }
            MultiField("Impacto", impact) { impact = it }
            MultiField("Remediação sugerida", remediation) { remediation = it }

            Button(
                onClick = { onSave(build()) },
                enabled = title.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar") }

            OutlinedButton(
                onClick = { showReport = true },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ver rascunho de relatório") }

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showCvss) {
        CvssDialog(
            onDismiss = { showCvss = false },
            onApply = { metrics ->
                cvssScore = CvssV31.baseScore(metrics)
                cvssVector = CvssV31.vector(metrics)
                severity = CvssV31.toSeverity(cvssScore)
                showCvss = false
            }
        )
    }

    if (showReport && program != null) {
        val current = program
        ReportDialog(
            markdown = ReportGenerator.forFinding(current, build()),
            fileName = reportFileName(current.name, title),
            onDismiss = { showReport = false }
        )
    }
}

/** Nome de ficheiro seguro para o PDF, a partir do programa e do título. */
private fun reportFileName(programName: String, title: String): String {
    val base = "${programName}_${title}".ifBlank { "relatorio" }
        .lowercase()
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .take(60)
    return (base.ifBlank { "relatorio" }) + ".pdf"
}

@Composable
private fun MultiField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().height(120.dp)
    )
}

@Composable
private fun <T> EnumDropdown(
    label: String,
    current: String,
    options: List<Pair<String, T>>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(current, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { (text, value) ->
                    DropdownMenuItem(
                        text = { Text(text) },
                        onClick = { onSelected(value); expanded = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun CvssDialog(
    onDismiss: () -> Unit,
    onApply: (CvssV31.Metrics) -> Unit
) {
    var av by remember { mutableStateOf(CvssV31.AttackVector.NETWORK) }
    var ac by remember { mutableStateOf(CvssV31.AttackComplexity.LOW) }
    var pr by remember { mutableStateOf(CvssV31.PrivilegesRequired.NONE) }
    var ui by remember { mutableStateOf(CvssV31.UserInteraction.NONE) }
    var scope by remember { mutableStateOf(CvssV31.Scope.UNCHANGED) }
    var c by remember { mutableStateOf(CvssV31.Impact.NONE) }
    var i by remember { mutableStateOf(CvssV31.Impact.NONE) }
    var a by remember { mutableStateOf(CvssV31.Impact.NONE) }

    val metrics = CvssV31.Metrics(av, ac, pr, ui, scope, c, i, a)
    val score = CvssV31.baseScore(metrics)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("CVSS v3.1 — Base") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Score: $score  ·  ${CvssV31.severityLabel(score)}",
                    style = MaterialTheme.typography.titleMedium
                )
                EnumDropdown("Attack Vector (AV)", av.label,
                    CvssV31.AttackVector.entries.map { it.label to it }, { av = it })
                EnumDropdown("Attack Complexity (AC)", ac.label,
                    CvssV31.AttackComplexity.entries.map { it.label to it }, { ac = it })
                EnumDropdown("Privileges Required (PR)", pr.label,
                    CvssV31.PrivilegesRequired.entries.map { it.label to it }, { pr = it })
                EnumDropdown("User Interaction (UI)", ui.label,
                    CvssV31.UserInteraction.entries.map { it.label to it }, { ui = it })
                EnumDropdown("Scope (S)", scope.label,
                    CvssV31.Scope.entries.map { it.label to it }, { scope = it })
                EnumDropdown("Confidentiality (C)", c.label,
                    CvssV31.Impact.entries.map { it.label to it }, { c = it })
                EnumDropdown("Integrity (I)", i.label,
                    CvssV31.Impact.entries.map { it.label to it }, { i = it })
                EnumDropdown("Availability (A)", a.label,
                    CvssV31.Impact.entries.map { it.label to it }, { a = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { onApply(metrics) }) { Text("Aplicar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ReportDialog(markdown: String, fileName: String, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val aiSettings = remember { AiSettings(context) }

    var text by remember { mutableStateOf(markdown) }
    var loading by remember { mutableStateOf(false) }
    var showKeyDialog by remember { mutableStateOf(false) }

    fun improveWithAi() {
        loading = true
        scope.launch {
            when (val result = ClaudeClient.improveReport(aiSettings.apiKey, text)) {
                is ClaudeClient.Result.Success -> text = result.text
                is ClaudeClient.Result.Error ->
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
            }
            loading = false
        }
    }

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        title = { Text("Rascunho de relatório") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                if (loading) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.height(18.dp).width(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("A melhorar com IA…", style = MaterialTheme.typography.bodySmall)
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Text(text, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Column {
                Row {
                    TextButton(enabled = !loading, onClick = {
                        clipboard.setText(AnnotatedString(text))
                        Toast.makeText(context, "Relatório copiado", Toast.LENGTH_SHORT).show()
                    }) { Text("Copiar") }
                    TextButton(enabled = !loading, onClick = {
                        runCatching { PdfExporter.shareReport(context, fileName, text) }
                            .onFailure {
                                Toast.makeText(context, "Falha ao gerar PDF", Toast.LENGTH_SHORT).show()
                            }
                        onDismiss()
                    }) { Text("Exportar PDF") }
                }
                TextButton(enabled = !loading, onClick = {
                    if (aiSettings.isConfigured) improveWithAi() else showKeyDialog = true
                }) { Text("Melhorar com IA") }
            }
        },
        dismissButton = {
            TextButton(enabled = !loading, onClick = onDismiss) { Text("Fechar") }
        }
    )

    if (showKeyDialog) {
        ApiKeyDialog(
            aiSettings = aiSettings,
            onDismiss = { showKeyDialog = false },
            onSaved = {
                showKeyDialog = false
                improveWithAi()
            }
        )
    }
}

@Composable
private fun ApiKeyDialog(
    aiSettings: AiSettings,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    var key by remember { mutableStateOf(aiSettings.apiKey) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assistente de IA (opcional)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Ao ativar, o texto do relatório é enviado para a API do Claude com a " +
                        "tua própria chave. É a única funcionalidade que usa a internet — todo " +
                        "o resto da app é local.",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    if (aiSettings.isEncrypted) {
                        "🔒 A chave é guardada cifrada no dispositivo."
                    } else {
                        "⚠ A cifra não está disponível neste dispositivo: a chave ficaria em " +
                            "texto simples."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (aiSettings.isEncrypted) MaterialTheme.typography.bodySmall.color
                    else MaterialTheme.colorScheme.error
                )
                Text(
                    "Confirma as regras de confidencialidade (NDA) do programa antes de enviar " +
                        "detalhes de um achado para fora.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = key, onValueChange = { key = it },
                    label = { Text("Chave da API (sk-ant-…)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = key.isNotBlank(),
                onClick = { aiSettings.apiKey = key; onSaved() }
            ) { Text("Guardar e usar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
