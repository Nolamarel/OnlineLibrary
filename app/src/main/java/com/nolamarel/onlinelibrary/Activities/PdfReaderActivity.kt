package com.nolamarel.onlinelibrary.Activities

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.alamin5g.pdf.PDFView
import com.nolamarel.onlinelibrary.ApiClient
import com.nolamarel.onlinelibrary.R
import com.nolamarel.onlinelibrary.auth.SessionManager
import com.nolamarel.onlinelibrary.network.UpdateProgressRequest
import kotlinx.coroutines.launch
import java.io.File

class PdfReaderActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView

    private var bookId: Long = -1L
    private var currentPage = 0
    private var totalPages = 0

    private var filePath: String? = null
    private var fileUri: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_reader)

        pdfView = findViewById(R.id.pdfView)

        bookId = intent.getLongExtra("book_id", -1L)
        filePath = intent.getStringExtra("file_path")
        fileUri = intent.getStringExtra("file_uri")

        if (bookId == -1L) {
            Toast.makeText(this, "Нет bookId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        when {
            !filePath.isNullOrBlank() -> openPdfFromPath(filePath!!)
            !fileUri.isNullOrBlank() -> openPdfFromUri(fileUri!!)
            else -> {
                Toast.makeText(this, "Не передан путь к PDF", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun openPdfFromPath(path: String) {
        val file = File(path)

        if (!file.exists()) {
            Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            pdfView.fromFile(file)
                .onPageChange { page, pageCount ->
                    currentPage = page
                    totalPages = pageCount
                }
                .load()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка открытия PDF", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun openPdfFromUri(uriString: String) {
        try {
            val uri = Uri.parse(uriString)

            pdfView.fromUri(uri)
                .onPageChange { page, pageCount ->
                    currentPage = page
                    totalPages = pageCount
                }
                .load()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка открытия PDF", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        sendProgress()
    }

    override fun onDestroy() {
        sendProgress()
        super.onDestroy()
    }

    private fun sendProgress() {
        val token = SessionManager(this).getToken() ?: return
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        val progress = calculateProgress()
        val effectivePath = fileUri ?: filePath

        val body = UpdateProgressRequest(
            progress = progress,
            currentPage = currentPage,
            locator = "page_$currentPage",
            localFilePath = effectivePath,
            fileFormat = "pdf"
        )

        lifecycleScope.launch {
            try {
                ApiClient.serverApi.updateReadingProgress(
                    token = bearer,
                    bookId = bookId.toString(),
                    body = body
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun calculateProgress(): String {
        if (totalPages == 0) return "0.00"

        val percent = ((currentPage + 1).toDouble() / totalPages.toDouble()) * 100
        return String.format("%.2f", percent)
    }
}