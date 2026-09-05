package com.jp.privacyscanner

import com.jp.privacyscanner.data.model.PermissionCategory
import com.jp.privacyscanner.data.permissions.PermissionCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Rede de segurança contra o catálogo "envelhecer em silêncio" (revisão 3.4).
 * Se uma permissão perigosa conhecida deixar de estar no catálogo, este teste
 * falha — obrigando a manter o catálogo atualizado em vez de ela cair
 * silenciosamente no caso por omissão e parar de penalizar o score.
 */
class PermissionCatalogTest {

    /** Permissões sensíveis que a app tem obrigação de reconhecer. */
    private val mustKnow = listOf(
        "android.permission.ACCESS_FINE_LOCATION",
        "android.permission.ACCESS_COARSE_LOCATION",
        "android.permission.ACCESS_BACKGROUND_LOCATION",
        "android.permission.RECORD_AUDIO",
        "android.permission.CAMERA",
        "android.permission.READ_CONTACTS",
        "android.permission.READ_SMS",
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_CALL_LOG",
        "android.permission.READ_PHONE_STATE",
        "android.permission.READ_CALENDAR",
        "android.permission.BODY_SENSORS",
        "android.permission.READ_MEDIA_IMAGES",
        "android.permission.READ_MEDIA_VIDEO",
        "android.permission.READ_MEDIA_AUDIO",
        "android.permission.ACTIVITY_RECOGNITION"
    )

    @Test
    fun `todas as permissoes perigosas conhecidas estao no catalogo`() {
        val faltam = mustKnow.filterNot { PermissionCatalog.isKnown(it) }
        assertTrue("Permissões em falta no catálogo: $faltam", faltam.isEmpty())
    }

    @Test
    fun `permissao desconhecida cai no default seguro`() {
        val e = PermissionCatalog.lookup("android.permission.INVENTADA_XYZ")
        assertEquals(PermissionCategory.OTHER, e.category)
    }
}
