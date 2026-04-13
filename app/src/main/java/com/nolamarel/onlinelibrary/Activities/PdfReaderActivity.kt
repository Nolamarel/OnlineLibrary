package com.nolamarel.onlinelibrary.Activities

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alamin5g.pdf.PDFView
import com.nolamarel.onlinelibrary.R
import java.io.File

class PdfReaderActivity : AppCompatActivity() {

    private lateinit var pdfView: PDFView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_reader)

        pdfView = findViewById(R.id.pdfView)

        val filePath = intent.getStringExtra("file_path")
        val fileUri = intent.getStringExtra("file_uri")

        when {
            !filePath.isNullOrBlank() -> openPdfFromPath(filePath)
            !fileUri.isNullOrBlank() -> openPdfFromUri(fileUri)
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
            pdfView.fromFile(file).load()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка открытия PDF", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun openPdfFromUri(uriString: String) {
        try {
            val uri = Uri.parse(uriString)
            pdfView.fromUri(uri).load()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Ошибка открытия PDF", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}