package com.jp.privacyscanner.ui.bugbounty

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jp.privacyscanner.data.bugbounty.BountyFinding
import com.jp.privacyscanner.data.bugbounty.BountyProgram
import com.jp.privacyscanner.data.bugbounty.MethodologyCatalog

@Composable
fun BountyProgramScreen(
    programId: Long,
    viewModel: BountyViewModel,
    onBack: () -> Unit,
    onAddFinding: (Long) -> Unit,
    onFindingClick: (Long) -> Unit
) {
    val program by viewModel.program(programId).collectAsStateWithLifecycle(initialValue = null)
    val findings by viewModel.findings(programId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var confirmDelete by remember { mutableStateOf(false) }
    var editScope by remember { mutableStateOf(false) }

    val current = program

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current?.name ?: "Programa") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (current != null) {
                        IconButton(onClick = { editScope = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar âmbito")
                        }
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Apagar programa")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (current != null) {
                FloatingActionButton(onClick = { onAddFinding(programId) }) {
                    Icon(Icons.Default.Add, contentDescription = "Novo achado")
                }
            }
        }
    ) { padding ->
        if (current == null) {
            Column(Modifier.fillMaxSize().padding(padding), Arrangement.Center, Alignment.CenterHorizontally) {
                Text("Programa não encontrado.")
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (current.inScope.isNotBlank() || current.outOfScope.isNotBlank()) {
                item { ScopeCard(current) }
            }

            item { SectionTitle("Achados (${findings.size})") }
            if (findings.isEmpty()) {
                item {
                    Text(
                        "Ainda sem achados. Usa o + para registar o primeiro.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                items(findings, key = { it.id }) { f ->
                    FindingRow(f, onClick = { onFindingClick(f.id) })
                }
            }

            item { SectionTitle("Checklist metodológica") }
            item { ChecklistProgress(current) }
            MethodologyCatalog.byPhase().forEach { (phase, phaseItems) ->
                item { PhaseHeader(phase.label) }
                items(phaseItems, key = { it.id }) { item ->
                    ChecklistRow(
                        item = item,
                        checked = current.completedIds.contains(item.id),
                        onToggle = { viewModel.toggleChecklist(current, item.id) }
                    )
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Apagar programa?") },
            text = { Text("Isto remove o programa e todos os seus achados. Não é reversível.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.deleteProgram(current)
                    onBack()
                }) { Text("Apagar") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } }
        )
    }

    if (editScope && current != null) {
        EditScopeDialog(
            program = current,
            onDismiss = { editScope = false },
            onSave = { updated ->
                viewModel.updateProgram(updated)
                editScope = false
            }
        )
    }
}

@Composable
private fun EditScopeDialog(
    program: BountyProgram,
    onDismiss: () -> Unit,
    onSave: (BountyProgram) -> Unit
) {
    var platform by remember { mutableStateOf(program.platform) }
    var inScope by remember { mutableStateOf(program.inScope) }
    var outOfScope by remember { mutableStateOf(program.outOfScope) }
    var policyUrl by remember { mutableStateOf(program.policyUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar âmbito") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = platform, onValueChange = { platform = it },
                    label = { Text("Plataforma") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = inScope, onValueChange = { inScope = it },
                    label = { Text("In-scope (um por linha)") },
                    modifier = Modifier.fillMaxWidth().height(110.dp)
                )
                OutlinedTextField(
                    value = outOfScope, onValueChange = { outOfScope = it },
                    label = { Text("Out-of-scope (um por linha)") },
                    modifier = Modifier.fillMaxWidth().height(110.dp)
                )
                OutlinedTextField(
                    value = policyUrl, onValueChange = { policyUrl = it },
                    label = { Text("URL da política") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    program.copy(
                        platform = platform.trim(),
                        inScope = inScope.trim(),
                        outOfScope = outOfScope.trim(),
                        policyUrl = policyUrl.trim()
                    )
                )
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ScopeCard(program: BountyProgram) {
    Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Column(Modifier.padding(16.dp)) {
            if (program.inScope.isNotBlank()) {
                Text("In-scope", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(program.inScope, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
            }
            if (program.outOfScope.isNotBlank()) {
                Text("Out-of-scope", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(program.outOfScope, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun FindingRow(finding: BountyFinding, onClick: () -> Unit) {
    val color = severityColor(finding.severityEnum)
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(
                Modifier.width(6.dp).height(56.dp)
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                    .background(color)
            )
            Column(Modifier.padding(12.dp).weight(1f)) {
                Text(
                    finding.title.ifBlank { "(sem título)" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                val cvss = if (finding.cvssScore > 0.0) "CVSS ${finding.cvssScore} · " else ""
                Text(
                    "$cvss${finding.severityEnum.label} · ${finding.statusEnum.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun ChecklistProgress(program: BountyProgram) {
    val total = MethodologyCatalog.allIds.size
    val done = program.completedIds.count { it in MethodologyCatalog.allIds }
    Text(
        "$done de $total itens concluídos",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun PhaseHeader(label: String) {
    Text(
        label,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun ChecklistRow(
    item: MethodologyCatalog.ChecklistItem,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onToggle)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Column {
                Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(item.hint, style = MaterialTheme.typography.bodySmall)
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
        modifier = Modifier.padding(top = 12.dp)
    )
}
