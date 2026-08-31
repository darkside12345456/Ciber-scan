package com.jp.privacyscanner.data.bugbounty

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Calculadora do CVSS v3.1 (métricas Base), segundo a especificação oficial da
 * FIRST. Lógica pura e determinística, por isso testável. Não faz qualquer
 * ligação à rede — só aritmética.
 */
object CvssV31 {

    enum class AttackVector(val code: String, val label: String, val score: Double) {
        NETWORK("N", "Rede", 0.85),
        ADJACENT("A", "Adjacente", 0.62),
        LOCAL("L", "Local", 0.55),
        PHYSICAL("P", "Físico", 0.2)
    }

    enum class AttackComplexity(val code: String, val label: String, val score: Double) {
        LOW("L", "Baixa", 0.77),
        HIGH("H", "Alta", 0.44)
    }

    /** Privilégios requeridos — o valor depende do Scope. */
    enum class PrivilegesRequired(val code: String, val label: String) {
        NONE("N", "Nenhuns"),
        LOW("L", "Baixos"),
        HIGH("H", "Altos");

        fun score(scopeChanged: Boolean): Double = when (this) {
            NONE -> 0.85
            LOW -> if (scopeChanged) 0.68 else 0.62
            HIGH -> if (scopeChanged) 0.5 else 0.27
        }
    }

    enum class UserInteraction(val code: String, val label: String, val score: Double) {
        NONE("N", "Nenhuma", 0.85),
        REQUIRED("R", "Necessária", 0.62)
    }

    enum class Scope(val code: String, val label: String) {
        UNCHANGED("U", "Inalterado"),
        CHANGED("C", "Alterado")
    }

    enum class Impact(val code: String, val label: String, val score: Double) {
        HIGH("H", "Alto", 0.56),
        LOW("L", "Baixo", 0.22),
        NONE("N", "Nenhum", 0.0)
    }

    data class Metrics(
        val av: AttackVector = AttackVector.NETWORK,
        val ac: AttackComplexity = AttackComplexity.LOW,
        val pr: PrivilegesRequired = PrivilegesRequired.NONE,
        val ui: UserInteraction = UserInteraction.NONE,
        val scope: Scope = Scope.UNCHANGED,
        val c: Impact = Impact.NONE,
        val i: Impact = Impact.NONE,
        val a: Impact = Impact.NONE
    )

    /** Calcula o Base Score (0.0–10.0) a partir das métricas. */
    fun baseScore(m: Metrics): Double {
        val scopeChanged = m.scope == Scope.CHANGED
        val iss = 1 - (1 - m.c.score) * (1 - m.i.score) * (1 - m.a.score)
        val impact = if (scopeChanged) {
            7.52 * (iss - 0.029) - 3.25 * (iss - 0.02).pow(15)
        } else {
            6.42 * iss
        }
        if (impact <= 0) return 0.0

        val exploitability = 8.22 * m.av.score * m.ac.score *
            m.pr.score(scopeChanged) * m.ui.score
        val raw = if (scopeChanged) {
            1.08 * (impact + exploitability)
        } else {
            impact + exploitability
        }
        return roundUp(minOf(raw, 10.0))
    }

    fun severityLabel(score: Double): String = when {
        score <= 0.0 -> "Nenhuma"
        score < 4.0 -> "Baixa"
        score < 7.0 -> "Média"
        score < 9.0 -> "Alta"
        else -> "Crítica"
    }

    /** Mapeia o score para a nossa escala interna de severidade. */
    fun toSeverity(score: Double): Severity = when {
        score <= 0.0 -> Severity.INFO
        score < 4.0 -> Severity.LOW
        score < 7.0 -> Severity.MEDIUM
        score < 9.0 -> Severity.HIGH
        else -> Severity.CRITICAL
    }

    /** Constrói a string de vetor CVSS v3.1 canónica. */
    fun vector(m: Metrics): String =
        "CVSS:3.1/AV:${m.av.code}/AC:${m.ac.code}/PR:${m.pr.code}/UI:${m.ui.code}" +
            "/S:${m.scope.code}/C:${m.c.code}/I:${m.i.code}/A:${m.a.code}"

    /**
     * Roundup oficial do CVSS v3.1 (Apêndice A): arredonda para cima à primeira
     * casa decimal, tratando o erro de vírgula flutuante.
     */
    private fun roundUp(input: Double): Double {
        val intInput = (input * 100_000).roundToLong()
        return if (intInput % 10_000 == 0L) {
            intInput / 100_000.0
        } else {
            (floor(intInput / 10_000.0) + 1) / 10.0
        }
    }

    // 'ceil' importado para clareza de intenção mesmo não sendo usado diretamente.
    @Suppress("unused")
    private fun ceilTo1(x: Double) = ceil(x * 10) / 10
}
