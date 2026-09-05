package com.jp.privacyscanner.data.scanner

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.jp.privacyscanner.data.model.AppInfo
import com.jp.privacyscanner.data.model.PermissionInfo
import com.jp.privacyscanner.data.permissions.PermissionCatalog
import com.jp.privacyscanner.data.scoring.ScoringEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lê as apps instaladas e as respetivas permissões através do [PackageManager].
 *
 * Limites confirmados no relatório (secção 2): só conseguimos as permissões
 * *declaradas* e se estão *concedidas*. Não conseguimos ver uso em tempo real
 * nem dados recolhidos por outras apps. Tudo é processado localmente.
 */
class AppScanner(private val context: Context) {

    private val pm: PackageManager get() = context.packageManager

    /**
     * Devolve todas as apps analisadas, ordenadas da mais arriscada (score
     * mais baixo) para a mais segura.
     *
     * @param includeSystemApps se false, filtra apps de sistema — normalmente
     *        o que o utilizador quer ver primeiro são as que instalou.
     */
    suspend fun scanInstalledApps(includeSystemApps: Boolean = false): List<AppInfo> =
        withContext(Dispatchers.Default) {
            // 1) Lista leve de pacotes (sem permissões) — uma resposta pequena.
            // 2) Permissões pedidas pacote a pacote, para nunca ultrapassar o
            //    limite de ~1 MB por transação do Binder. Pedir tudo de uma vez
            //    (getInstalledPackages(GET_PERMISSIONS)) lança
            //    TransactionTooLargeException em telemóveis com muitas apps.
            queryInstalledPackages().asSequence()
                .filter { it.packageName != context.packageName } // não nos analisamos a nós próprios
                .mapNotNull { base -> packageInfoWithPermissions(base.packageName) }
                .filter { includeSystemApps || !it.isSystemApp() }
                .map { it.toAppInfo() }
                .sortedBy { it.privacyScore }
                .toList()
        }

    @Suppress("DEPRECATION")
    private fun queryInstalledPackages(): List<PackageInfo> {
        // Sem GET_PERMISSIONS: a resposta é pequena e cabe folgadamente no Binder.
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0L))
        } else {
            pm.getInstalledPackages(0)
        }
    }

    /** Obtém as permissões de um único pacote, tolerando pacotes entretanto removidos. */
    @Suppress("DEPRECATION")
    private fun packageInfoWithPermissions(packageName: String): PackageInfo? = try {
        val flags = PackageManager.GET_PERMISSIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            pm.getPackageInfo(packageName, flags)
        }
    } catch (e: Exception) {
        // PackageManager.NameNotFoundException ou, raramente, ainda
        // TransactionTooLargeException num único pacote patológico: ignoramos
        // esse pacote em vez de deixar cair o scan inteiro.
        null
    }

    private fun PackageInfo.toAppInfo(): AppInfo {
        val perms = buildPermissionList(this)
        return AppInfo(
            packageName = packageName,
            appName = applicationInfo?.let { pm.getApplicationLabel(it).toString() } ?: packageName,
            isSystemApp = isSystemApp(),
            installedAt = firstInstallTime,
            versionName = versionName,
            permissions = perms,
            privacyScore = ScoringEngine.scoreForApp(perms)
        )
    }

    private fun buildPermissionList(pkg: PackageInfo): List<PermissionInfo> {
        val requested = pkg.requestedPermissions ?: return emptyList()
        val flags = pkg.requestedPermissionsFlags
        return requested.mapIndexed { index, rawName ->
            val granted = flags != null &&
                index < flags.size &&
                (flags[index] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
            val entry = PermissionCatalog.lookup(rawName)
            PermissionInfo(
                rawName = rawName,
                granted = granted,
                category = entry.category,
                riskLevel = entry.riskLevel,
                explanation = entry.explanation
            )
        }
    }

    private fun PackageInfo.isSystemApp(): Boolean {
        val ai = applicationInfo ?: return false
        return (ai.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
    }
}
