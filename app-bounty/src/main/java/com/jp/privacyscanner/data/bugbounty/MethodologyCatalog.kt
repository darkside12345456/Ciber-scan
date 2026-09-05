package com.jp.privacyscanner.data.bugbounty

/**
 * Checklist metodológica para testes de segurança em alvos AUTORIZADOS.
 *
 * É um guia organizacional e educativo — ajuda o investigador a estruturar o
 * trabalho e a não esquecer classes de teste comuns. Não executa qualquer
 * ataque; apenas lembra o que verificar manualmente, sempre dentro do âmbito
 * autorizado do programa.
 */
object MethodologyCatalog {

    enum class Phase(val label: String) {
        RECON("Reconhecimento"),
        AUTH("Autenticação"),
        ACCESS("Autorização / Controlo de acesso"),
        INPUT("Validação de entrada"),
        LOGIC("Lógica de negócio"),
        CONFIG("Configuração e exposição"),
        REPORT("Relatório")
    }

    data class ChecklistItem(
        val id: String,
        val phase: Phase,
        val title: String,
        val hint: String
    )

    val items: List<ChecklistItem> = listOf(
        // Reconhecimento
        ChecklistItem("recon_scope", Phase.RECON,
            "Confirmar o âmbito autorizado",
            "Lê a política do programa. Testa apenas os alvos in-scope; anota o que está out-of-scope."),
        ChecklistItem("recon_assets", Phase.RECON,
            "Mapear a superfície",
            "Domínios, subdomínios, APIs e funcionalidades. Regista tudo o que vais cobrir."),
        ChecklistItem("recon_tech", Phase.RECON,
            "Identificar tecnologias",
            "Framework, servidor, versões visíveis — orienta que classes de teste fazem sentido."),

        // Autenticação
        ChecklistItem("auth_flows", Phase.AUTH,
            "Rever fluxos de autenticação",
            "Registo, login, recuperação de password, MFA. Procura lógica fraca ou passos que se possam saltar."),
        ChecklistItem("auth_session", Phase.AUTH,
            "Gestão de sessão",
            "Expiração, invalidação no logout, atributos dos cookies (HttpOnly, Secure, SameSite)."),

        // Autorização
        ChecklistItem("access_idor", Phase.ACCESS,
            "Referências diretas a objetos (IDOR)",
            "Consegues aceder a recursos de outro utilizador trocando um identificador?"),
        ChecklistItem("access_privesc", Phase.ACCESS,
            "Escalonamento de privilégios",
            "Um utilizador normal consegue ações de administrador? Verifica endpoints e campos."),

        // Validação de entrada
        ChecklistItem("input_injection", Phase.INPUT,
            "Pontos de injeção",
            "Onde entra input do utilizador em consultas, comandos ou HTML? Testa dentro do âmbito."),
        ChecklistItem("input_xss", Phase.INPUT,
            "Cross-site scripting",
            "Refletido, armazenado e baseado em DOM nos campos que aceitam texto."),
        ChecklistItem("input_upload", Phase.INPUT,
            "Upload de ficheiros",
            "Validação de tipo/tamanho e onde o ficheiro fica acessível."),

        // Lógica de negócio
        ChecklistItem("logic_flows", Phase.LOGIC,
            "Abuso de lógica de negócio",
            "Preços, quantidades, passos fora de ordem, condições de corrida em ações sensíveis."),
        ChecklistItem("logic_rate", Phase.LOGIC,
            "Limites e rate limiting",
            "Ações sensíveis (login, OTP) têm limitação suficiente?"),

        // Configuração
        ChecklistItem("config_headers", Phase.CONFIG,
            "Cabeçalhos de segurança",
            "HSTS, CSP, X-Content-Type-Options e afins."),
        ChecklistItem("config_exposure", Phase.CONFIG,
            "Exposição de informação",
            "Mensagens de erro verbosas, ficheiros de debug, endpoints esquecidos."),

        // Relatório
        ChecklistItem("report_repro", Phase.REPORT,
            "Passos de reprodução claros",
            "Um triager deve conseguir reproduzir sem adivinhar."),
        ChecklistItem("report_impact", Phase.REPORT,
            "Impacto e remediação",
            "Explica o risco real para o negócio e sugere como corrigir.")
    )

    val allIds: Set<String> get() = items.map { it.id }.toSet()

    fun byPhase(): Map<Phase, List<ChecklistItem>> = items.groupBy { it.phase }
}
