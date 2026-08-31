package com.jp.privacyscanner.util

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File

/**
 * Exporta texto (o rascunho de relatório em Markdown) para um ficheiro PDF
 * usando a API nativa [PdfDocument] — sem bibliotecas externas.
 *
 * O texto é desenhado linha a linha com quebra simples por largura e paginação
 * automática. Linhas iniciadas por '#'/'##' são realçadas como títulos.
 */
object PdfExporter {

    private const val PAGE_WIDTH = 595   // A4 a 72dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f
    private const val LINE_HEIGHT = 18f

    /** Gera o PDF em cache e devolve o ficheiro criado. */
    fun writeReport(context: Context, fileName: String, content: String): File {
        val doc = PdfDocument()
        val body = Paint().apply { textSize = 11f; color = 0xFF000000.toInt() }
        val h1 = Paint().apply {
            textSize = 16f; color = 0xFF000000.toInt()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val h2 = Paint().apply {
            textSize = 13f; color = 0xFF000000.toInt()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val maxWidth = PAGE_WIDTH - 2 * MARGIN

        var pageNumber = 1
        var page = doc.startPage(pageInfo(pageNumber))
        var canvas = page.canvas
        var y = MARGIN

        for (rawLine in content.lines()) {
            val paint = when {
                rawLine.startsWith("## ") -> h2
                rawLine.startsWith("# ") -> h1
                else -> body
            }
            val text = rawLine.removePrefix("## ").removePrefix("# ")
            val wrapped = wrap(text, paint, maxWidth)
            for (line in wrapped) {
                if (y + LINE_HEIGHT > PAGE_HEIGHT - MARGIN) {
                    doc.finishPage(page)
                    pageNumber++
                    page = doc.startPage(pageInfo(pageNumber))
                    canvas = page.canvas
                    y = MARGIN
                }
                canvas.drawText(line, MARGIN, y, paint)
                y += LINE_HEIGHT
            }
        }
        doc.finishPage(page)

        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, fileName)
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        return file
    }

    /** Gera o PDF e abre o seletor de partilha do sistema. */
    fun shareReport(context: Context, fileName: String, content: String) {
        val file = writeReport(context, fileName, content)
        val uri = FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(intent, "Partilhar relatório")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun pageInfo(number: Int) =
        PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, number).create()

    /** Quebra uma linha em várias conforme a largura disponível. */
    private fun wrap(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isEmpty()) return listOf("")
        val words = text.split(' ')
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        for (word in words) {
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (paint.measureText(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                if (current.isNotEmpty()) lines.add(current.toString())
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines.add(current.toString())
        return lines
    }
}
