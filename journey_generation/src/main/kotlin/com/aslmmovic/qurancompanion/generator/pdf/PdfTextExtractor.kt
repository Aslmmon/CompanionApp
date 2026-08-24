package com.aslmmovic.qurancompanion.generator.pdf

import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.ByteArrayOutputStream
import java.io.File

object PdfTextExtractor {

    // Extract complete text from a PDF file using Apache PDFBox
    fun extractAllText(pdfFile: File): String {
        require(pdfFile.exists()) { "PDF file not found: ${pdfFile.absolutePath}" }
        Loader.loadPDF(pdfFile).use { document ->
            val stripper = PDFTextStripper()
            return stripper.getText(document).trim()
        }
    }

    // Extract text from a specified page range (1-indexed inclusive)
    fun extractPageRange(pdfFile: File, startPage: Int, endPage: Int): String {
        require(pdfFile.exists()) { "PDF file not found: ${pdfFile.absolutePath}" }
        Loader.loadPDF(pdfFile).use { document ->
            val stripper = PDFTextStripper().apply {
                this.startPage = startPage
                this.endPage = endPage
            }
            return stripper.getText(document).trim()
        }
    }

    // Extract a subset of pages into a new standalone PDF byte array for Gemini multimodal vision
    fun extractPageRangeToBytes(pdfFile: File, startPage: Int = 1, endPage: Int? = null): ByteArray {
        require(pdfFile.exists()) { "PDF file not found: ${pdfFile.absolutePath}" }
        Loader.loadPDF(pdfFile).use { document ->
            val totalPages = document.numberOfPages
            val sPage = startPage.coerceIn(1, totalPages)
            val ePage = (endPage ?: totalPages).coerceIn(sPage, totalPages)

            PDDocument().use { slicedDoc ->
                for (i in (sPage - 1) until ePage) {
                    slicedDoc.addPage(document.getPage(i))
                }
                val out = ByteArrayOutputStream()
                slicedDoc.save(out)
                return out.toByteArray()
            }
        }
    }

    // Get total number of pages in the PDF file
    fun getPageCount(pdfFile: File): Int {
        require(pdfFile.exists()) { "PDF file not found: ${pdfFile.absolutePath}" }
        Loader.loadPDF(pdfFile).use { document ->
            return document.numberOfPages
        }
    }
}
