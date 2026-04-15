package com.nolamarel.onlinelibrary.Activities

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
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
    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvPageIndicator: TextView
    private lateinit var topBar: View

    private var bookId: Long = -1L
    private var currentPage = 0
    private var totalPages = 0
    private var startPage = 0

    private var filePath: String? = null
    private var fileUri: String? = null
    private var bookTitle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_reader)

        initViews()
        readArguments()
        setupClicks()

        if (!validateInput()) return

        tvTitle.text = bookTitle?.takeIf { it.isNotBlank() } ?: "Чтение PDF"

        openPdf()
        showReaderHint()
    }

    private fun initViews() {
        pdfView = findViewById(R.id.pdfView)
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        tvProgress = findViewById(R.id.tvProgress)
        tvPageIndicator = findViewById(R.id.tvPageIndicator)
        topBar = findViewById(R.id.topBar)
    }

    private fun readArguments() {
        bookId = intent.getLongExtra("book_id", -1L)
        filePath = intent.getStringExtra("file_path")
        fileUri = intent.getStringExtra("file_uri")
        startPage = intent.getIntExtra("current_page", 0)
        bookTitle = intent.getStringExtra("book_title")
    }

    private fun setupClicks() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun validateInput(): Boolean {
        if (bookId == -1L) {
            Toast.makeText(this, "Не удалось открыть книгу", Toast.LENGTH_SHORT).show()
            finish()
            return false
        }

        if (filePath.isNullOrBlank() && fileUri.isNullOrBlank()) {
            Toast.makeText(this, "Не передан PDF-файл", Toast.LENGTH_SHORT).show()
            finish()
            return false
        }

        return true
    }

    private fun openPdf() {
        when {
            !filePath.isNullOrBlank() -> openPdfFromPath(filePath!!)
            !fileUri.isNullOrBlank() -> openPdfFromUri(fileUri!!)
            else -> {
                Toast.makeText(this, "Не удалось открыть PDF", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun openPdfFromPath(path: String) {
        val file = File(path)

        if (!file.exists() || !file.canRead()) {
            Toast.makeText(this, "Файл PDF не найден или недоступен", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        pdfView.fromFile(file)
            .defaultPage(startPage.coerceAtLeast(0))
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onLoad { pageCount ->
                totalPages = pageCount
                currentPage = startPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
                updatePageUi()
            }
            .onPageChange { page, pageCount ->
                currentPage = page
                totalPages = pageCount
                updatePageUi()
            }
            .onError {
                Toast.makeText(
                    this,
                    "Ошибка при открытии PDF-файла",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
            .load()
    }

    private fun openPdfFromUri(uriString: String) {
        try {
            val uri = Uri.parse(uriString)

            pdfView.fromUri(uri)
                .defaultPage(startPage.coerceAtLeast(0))
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .onLoad { pageCount ->
                    totalPages = pageCount
                    currentPage = startPage.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
                    updatePageUi()
                }
                .onPageChange { page, pageCount ->
                    currentPage = page
                    totalPages = pageCount
                    updatePageUi()
                }
                .onError {
                    Toast.makeText(
                        this,
                        "Ошибка при открытии PDF по ссылке",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .load()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Не удалось открыть PDF",
                Toast.LENGTH_LONG
            ).show()
            finish()
        }
    }

    private fun updatePageUi() {
        val shownPage = if (totalPages == 0) 0 else currentPage + 1
        tvPageIndicator.text = "$shownPage / $totalPages"
        tvProgress.text = "${calculateProgress()}%"
    }

    private fun showReaderHint() {
        Toast.makeText(
            this,
            "Свайпайте вверх и вниз для чтения",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onPause() {
        super.onPause()
        saveProgress()
    }

    override fun onDestroy() {
        saveProgress()
        super.onDestroy()
    }

    private fun saveProgress() {
        if (bookId == -1L) return

        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken() ?: return
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        val effectivePath = fileUri ?: filePath
        val progress = calculateProgress()

        if (!effectivePath.isNullOrBlank()) {
            sessionManager.saveLastOpenedBook(
                bookId = bookId,
                title = bookTitle,
                currentPage = currentPage,
                localPath = effectivePath
            )
        }

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
            } catch (_: Exception) {
            }
        }
    }

    private fun calculateProgress(): String {
        if (totalPages <= 0) return "0.00"

        val percent = ((currentPage + 1).toDouble() / totalPages.toDouble()) * 100.0
        return String.format("%.2f", percent)
    }
}