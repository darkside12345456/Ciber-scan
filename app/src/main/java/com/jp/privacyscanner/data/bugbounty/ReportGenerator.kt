package com.jp.privacyscanner.data.bugbounty

/**
 * Gera um rascunho de relatório em Markdown a partir de um achado. Lógica pura
 * (sem Android), por isso testável. O resultado é um ponto de partida que o
 * investigador revê antes de submeter na plataforma.
 */
object ReportGenerator {

    fun forFinding(program: BountyProgram, finding: BountyFinding): String {
        val sb = StringBuilder()
        sb.appendLine("# ${finding.title.ifBlank { "(sem título)" }}")
        sb.appendLine()
        sb.appendLine("**Programa:** ${program.name.ifBlank { "—" }}")
        if (program.platform.isNotBlank()) sb.appendLine("**Plataforma:** ${program.platform}")
        sb.appendLine("**Severidade:** ${finding.severityEnum.label}")
        sb.appendLine("**Estado:** ${finding.statusEnum.label}")
        if (finding.affectedAsset.isNotBlank()) {
            sb.appendLine("**Ativo afetado:** ${finding.affectedAsset}")
        }
        sb.appendLine()

        section(sb, "Descrição", finding.description)
        section(sb, "Passos para reproduzir", finding.steps)
        section(sb, "Impacto", finding.impact)
        section(sb, "Remediação sugerida", finding.remediation)

        sb.appendLine("---")
        sb.appendLine("_Rascunho gerado localmente. Revê e confirma antes de submeter. " +
            "Testa apenas alvos dentro do âmbito autorizado do programa._")
        return sb.toString().trimEnd() + "\n"
    }

    private fun section(sb: StringBuilder, title: String, body: String) {
        sb.appendLine("## $title")
        sb.appendLine(body.ifBlank { "_(por preencher)_" })
        sb.appendLine()
    }
}
