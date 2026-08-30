package com.jp.privacyscanner.data.recommendations

import com.jp.privacyscanner.data.model.PermissionCategory
import com.jp.privacyscanner.data.model.RiskLevel

/**
 * Uma recomendação concreta e acionável para o utilizador, gerada a partir das
 * permissões de uma app. É o passo que transforma "aqui estão os riscos" em
 * "faz isto a seguir".
 *
 * @param category  Categoria de permissão a que a recomendação diz respeito.
 * @param severity  Urgência da ação (herda o risco da permissão).
 * @param title     Frase curta e direta, ex.: "Desliga o microfone".
 * @param rationale Porquê — o contexto que dá credibilidade à recomendação.
 */
data class Recommendation(
    val category: PermissionCategory,
    val severity: RiskLevel,
    val title: String,
    val rationale: String
)
