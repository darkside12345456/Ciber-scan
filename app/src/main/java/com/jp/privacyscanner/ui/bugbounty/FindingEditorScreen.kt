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
import com.jp.privacyscanner.data.bugbounty.FindingStatus
import com.jp.privacyscanner.data.bugbounty.ReportGenerator
import com.jp.privacyscanner.data.bugbounty.Severity
import com.jp.privacyscanner.util.PdfExporter

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
    var showReport by remember { mutableStateOf(false) }

    fun build(): BountyFinding = (existing ?: BountyFinding(programId = programId, title = "")).copy(
        programId = programId,
        title = title,
        severity = severity.name,
        status = status.name,
        affectedAsset = asset,
        description = description,
        steps = steps,
        impact = impact,
        remediation = remediation
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
private fun ReportDialog(markdown: String, fileName: String, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rascunho de relatório") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(markdown, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = {
                    clipboard.setText(AnnotatedString(markdown))
                    Toast.makeText(context, "Relatório copiado", Toast.LENGTH_SHORT).show()
                }) { Text("Copiar") }
                TextButton(onClick = {
                    runCatching { PdfExporter.shareReport(context, fileName, markdown) }
                        .onFailure {
                            Toast.makeText(context, "Falha ao gerar PDF", Toast.LENGTH_SHORT).show()
                        }
                    onDismiss()
                }) { Text("Exportar PDF") }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Fechar") } }
    )
}
