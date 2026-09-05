package com.jp.privacyscanner.data.monitoring

import com.jp.privacyscanner.data.model.AppInfo
import com.jp.privacyscanner.data.model.RiskLevel

/**
 * Logica pura (sem dependencias Android) que compara duas analises e deteta
 * permissoes sensiveis recem-concedidas. Isolada aqui para ser testavel e para
 * manter o [MonitoringWorker] fino.
 *
 * Uma "entrada de snapshot" e a string `packageName|permissaoRaw`, o formato
 * guardado entre execucoes. O separador '|' e seguro porque nem os nomes de
 * pacote nem os nomes de permissao o contem.
 */
object MonitoringDiff {

    private const val SEP = "|"

    /** Constroi o snapshot das permissoes sensiveis concedidas de um scan. */
    fun buildSnapshot(apps: List<AppInfo>): Set<String> = buildSet {
        for (app in apps) {
            for (perm in app.permissions) {
                if (perm.granted && perm.riskLevel != RiskLevel.LOW) {
                    add(app.packageName + SEP + perm.rawName)
                }
            }
        }
    }

    /** Uma alteracao detetada: uma app passou a ter permissoes sensiveis novas. */
    data class Change(val packageName: String, val newPermissions: List<String>)

    /**
     * Devolve, por app, as permissoes sensiveis que existem no snapshot novo mas
     * nao existiam no anterior. Apps ausentes do snapshot antigo (recem-
     * instaladas) contam como totalmente novas.
     */
    fun newlyGranted(old: Set<String>, current: Set<String>): List<Change> {
        val added = current - old
        if (added.isEmpty()) return emptyList()
        return added
            .map { it.substringBefore(SEP) to it.substringAfter(SEP) }
            .groupBy({ it.first }, { it.second })
            .map { (pkg, perms) -> Change(pkg, perms.sorted()) }
            .sortedBy { it.packageName }
    }
}
