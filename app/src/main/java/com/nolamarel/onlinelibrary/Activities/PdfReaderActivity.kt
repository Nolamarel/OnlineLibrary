package com.nolamarel.onlinelibrary.Activities

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
        Toast.makeText(this, "PdfReaderActivity opened", Toast.LENGTH_LONG).show()
        pdfView = findViewById(R.id.pdfView)
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        tvProgress = findViewById(R.id.tvProgress)
        tvPageIndicator = findViewById(R.id.tvPageIndicator)
        topBar = findViewById(R.id.topBar)

        bookId = intent.getLongExtra("book_id", -1L)
        filePath = intent.getStringExtra("file_path")
        fileUri = intent.getStringExtra("file_uri")
        startPage = intent.getIntExtra("current_page", 0)
        bookTitle = intent.getStringExtra("book_title")

        tvTitle.text = bookTitle ?: "Чтение PDF"

        btnBack.setOnClickListener {
            finish()
        }

        if (bookId == -1L) {
            Toast.makeText(this, "Нет bookId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        Log.d("PdfReader", "bookId=$bookId")
        Log.d("PdfReader", "filePath=$filePath")
        Log.d("PdfReader", "fileUri=$fileUri")
        Log.d("PdfReader", "bookTitle=$bookTitle")
        Log.d("PdfReader", "startPage=$startPage")

        when {
            !filePath.isNullOrBlank() -> openPdfFromPath(filePath!!)
            !fileUri.isNullOrBlank() -> openPdfFromUri(fileUri!!)
            else -> {
                Toast.makeText(this, "Не передан путь к PDF", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        showReaderHint()
    }

    private fun openPdfFromPath(path: String) {
        Log.d("PdfReader", "openPdfFromPath path=$path startPage=$startPage")

        val file = File(path)
        Log.d("PdfReader", "exists=${file.exists()} canRead=${file.canRead()} length=${file.length()}")

        if (!file.exists()) {
            Toast.makeText(this, "Файл не найден: $path", Toast.LENGTH_LONG).show()
            return
        }

        pdfView.fromFile(file)
            .defaultPage(startPage)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .onLoad { pageCount ->
                Log.d("PdfReader", "PDF loaded from file, pageCount=$pageCount")
                totalPages = pageCount
                updatePageUi()
            }
            .onPageChange { page, pageCount ->
                Log.d("PdfReader", "onPageChange page=$page pageCount=$pageCount")
                currentPage = page
                totalPages = pageCount
                updatePageUi()
            }
            .onError { t ->
                Log.e("PdfReader", "PDF file error", t)
                Toast.makeText(this, "Ошибка PDF: ${t.message}", Toast.LENGTH_LONG).show()
            }
            .load()
    }

    private fun openPdfFromUri(uriString: String) {
        Log.d("PdfReader", "openPdfFromUri uriString=$uriString startPage=$startPage")

        try {
            val uri = Uri.parse(uriString)

            contentResolver.openInputStream(uri)?.use {
                Log.d("PdfReader", "URI input stream opened successfully")
            } ?: Log.d("PdfReader", "URI input stream is null")

            pdfView.fromUri(uri)
                .defaultPage(startPage)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .onLoad { pageCount ->
                    Log.d("PdfReader", "PDF loaded from uri, pageCount=$pageCount")
                    totalPages = pageCount
                    updatePageUi()
                }
                .onPageChange { page, pageCount ->
                    Log.d("PdfReader", "onPageChange page=$page pageCount=$pageCount")
                    currentPage = page
                    totalPages = pageCount
                    updatePageUi()
                }
                .onError { t ->
                    Log.e("PdfReader", "PDF uri error", t)
                    Toast.makeText(this, "Ошибка PDF URI: ${t.message}", Toast.LENGTH_LONG).show()
                }
                .load()
        } catch (e: Exception) {
            Log.e("PdfReader", "openPdfFromUri exception", e)
            Toast.makeText(this, "Ошибка открытия PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updatePageUi() {
        val shownPage = currentPage + 1
        tvPageIndicator.text = "$shownPage / $totalPages"
        tvProgress.text = "${calculateProgress()}%"
    }

    private fun showReaderHint() {
        Toast.makeText(
            this,
            "Свайпайте вверх и вниз для чтения PDF",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPause() {
        super.onPause()
        sendProgress()
    }

    private fun sendProgress() {
        val token = SessionManager(this).getToken() ?: return
        val bearer = if (token.startsWith("Bearer ")) token else "Bearer $token"

        val progress = calculateProgress()
        val effectivePath = fileUri ?: filePath

        if (!effectivePath.isNullOrBlank()) {
            SessionManager(this).saveLastOpenedBook(
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