package com.jp.privacyscanner

import com.jp.privacyscanner.data.model.AppInfo
import com.jp.privacyscanner.data.model.PermissionCategory
import com.jp.privacyscanner.data.model.PermissionInfo
import com.jp.privacyscanner.data.model.RiskLevel
import com.jp.privacyscanner.data.monitoring.MonitoringDiff
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MonitoringDiffTest {

    private fun perm(raw: String, risk: RiskLevel, granted: Boolean) =
        PermissionInfo(raw, granted, PermissionCategory.OTHER, risk, "")

    private fun app(pkg: String, vararg perms: PermissionInfo) = AppInfo(
        packageName = pkg,
        appName = pkg,
        isSystemApp = false,
        installedAt = 0,
        versionName = null,
        permissions = perms.toList(),
        privacyScore = 50
    )

    @Test
    fun `snapshot inclui so sensiveis concedidas`() {
        val snap = MonitoringDiff.buildSnapshot(
            listOf(
                app(
                    "com.a",
                    perm("android.permission.CAMERA", RiskLevel.HIGH, granted = true),
                    perm("android.permission.INTERNET", RiskLevel.LOW, granted = true),
                    perm("android.permission.RECORD_AUDIO", RiskLevel.HIGH, granted = false)
                )
            )
        )
        assertEquals(setOf("com.a|android.permission.CAMERA"), snap)
    }

    @Test
    fun `deteta permissao recem concedida`() {
        val old = setOf("com.a|android.permission.CAMERA")
        val current = setOf(
            "com.a|android.permission.CAMERA",
            "com.a|android.permission.RECORD_AUDIO"
        )
        val changes = MonitoringDiff.newlyGranted(old, current)
        assertEquals(1, changes.size)
        assertEquals("com.a", changes[0].packageName)
        assertEquals(listOf("android.permission.RECORD_AUDIO"), changes[0].newPermissions)
    }

    @Test
    fun `sem alteracoes devolve lista vazia`() {
        val snap = setOf("com.a|android.permission.CAMERA")
        assertTrue(MonitoringDiff.newlyGranted(snap, snap).isEmpty())
    }

    @Test
    fun `app nova conta como totalmente nova`() {
        val changes = MonitoringDiff.newlyGranted(
            emptySet(),
            setOf("com.nova|android.permission.ACCESS_FINE_LOCATION")
        )
        assertEquals(1, changes.size)
        assertEquals("com.nova", changes[0].packageName)
    }
}
