package com.jp.privacyscanner

import com.jp.privacyscanner.data.bugbounty.BountyFinding
import com.jp.privacyscanner.data.bugbounty.BountyProgram
import com.jp.privacyscanner.data.bugbounty.FindingStatus
import com.jp.privacyscanner.data.bugbounty.ReportGenerator
import com.jp.privacyscanner.data.bugbounty.Severity
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportGeneratorTest {

    private val program = BountyProgram(
        id = 1, name = "Acme VDP", platform = "HackerOne", inScope = "*.acme.com"
    )

    @Test
    fun `relatorio inclui titulo severidade e programa`() {
        val finding = BountyFinding(
            id = 1, programId = 1,
            title = "IDOR no endpoint de faturas",
            severity = Severity.HIGH.name,
            status = FindingStatus.DRAFT.name,
            affectedAsset = "api.acme.com/invoices",
            description = "É possível ler faturas de outro utilizador.",
            steps = "1. Autenticar\n2. Trocar o id",
            impact = "Exposição de dados de faturação.",
            remediation = "Validar a posse do recurso."
        )
        val md = ReportGenerator.forFinding(program, finding)
        assertTrue(md.contains("# IDOR no endpoint de faturas"))
        assertTrue(md.contains("**Severidade:** Alta"))
        assertTrue(md.contains("**Programa:** Acme VDP"))
        assertTrue(md.contains("## Passos para reproduzir"))
        assertTrue(md.contains("api.acme.com/invoices"))
    }

    @Test
    fun `seccoes vazias mostram marcador por preencher`() {
        val finding = BountyFinding(id = 2, programId = 1, title = "Teste")
        val md = ReportGenerator.forFinding(program, finding)
        assertTrue(md.contains("_(por preencher)_"))
    }

    @Test
    fun `relatorio termina com aviso de ambito autorizado`() {
        val finding = BountyFinding(id = 3, programId = 1, title = "Teste")
        val md = ReportGenerator.forFinding(program, finding)
        assertTrue(md.contains("âmbito autorizado"))
    }
}
