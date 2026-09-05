package com.jp.privacyscanner.data.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Cliente mínimo da Messages API do Claude, via HTTP direto.
 *
 * Numa app Android que faz uma única chamada com a chave do próprio utilizador,
 * o HTTP direto é a abordagem idiomática e leve — evita trazer um SDK de
 * servidor. A chamada corre em Dispatchers.IO e devolve o texto da resposta.
 */
object ClaudeClient {

    private const val ENDPOINT = "https://api.anthropic.com/v1/messages"
    private const val ANTHROPIC_VERSION = "2023-06-01"
    private const val MODEL = "claude-opus-5"
    private const val MAX_TOKENS = 4000

    private val SYSTEM_PROMPT = """
        És um editor técnico de relatórios de segurança para bug bounty. Recebes
        um rascunho em Markdown e devolves uma versão melhorada.

        Regras estritas:
        - NÃO inventes vulnerabilidades, passos, impactos, ativos ou dados que não
          estejam no rascunho. Trabalhas apenas com o que te é dado.
        - NÃO exageres a severidade nem o impacto.
        - Melhora clareza, estrutura, tom profissional e correção do Markdown.
        - Se uma secção estiver vazia, deixa uma sugestão curta entre parênteses
          do que o autor deve preencher (não a inventes).
        - Mantém o idioma do rascunho.
        - Responde APENAS com o relatório melhorado, sem comentários teus.
    """.trimIndent()

    /** Resultado de uma tentativa de melhoria. */
    sealed interface Result {
        data class Success(val text: String) : Result
        data class Error(val message: String) : Result
    }

    suspend fun improveReport(apiKey: String, draftMarkdown: String): Result =
        withContext(Dispatchers.IO) {
            runCatching { request(apiKey, draftMarkdown) }
                .getOrElse { Result.Error(it.message ?: "Erro de rede.") }
        }

    private fun request(apiKey: String, draft: String): Result {
        val body = JSONObject()
            .put("model", MODEL)
            .put("max_tokens", MAX_TOKENS)
            .put("system", SYSTEM_PROMPT)
            .put(
                "messages",
                JSONArray().put(
                    JSONObject()
                        .put("role", "user")
                        .put("content", "Melhora este rascunho de relatório:\n\n$draft")
                )
            )
            .toString()

        val conn = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-api-key", apiKey)
            setRequestProperty("anthropic-version", ANTHROPIC_VERSION)
        }

        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val response = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
        conn.disconnect()

        return if (code in 200..299) {
            parseSuccess(response)
        } else {
            Result.Error(parseError(response, code))
        }
    }

    private fun parseSuccess(json: String): Result {
        val content = JSONObject(json).optJSONArray("content") ?: return Result.Error("Resposta vazia.")
        val sb = StringBuilder()
        for (i in 0 until content.length()) {
            val block = content.optJSONObject(i) ?: continue
            if (block.optString("type") == "text") sb.append(block.optString("text"))
        }
        val text = sb.toString().trim()
        return if (text.isEmpty()) Result.Error("Resposta sem texto.") else Result.Success(text)
    }

    private fun parseError(json: String, code: Int): String {
        val message = runCatching {
            JSONObject(json).optJSONObject("error")?.optString("message")
        }.getOrNull()
        return when {
            !message.isNullOrBlank() -> message
            code == 401 -> "Chave da API inválida (401)."
            code == 429 -> "Limite de pedidos atingido (429). Tenta mais tarde."
            else -> "Erro da API ($code)."
        }
    }
}
