package com.chordio.utils


import android.content.ContentValues
import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.chordio.models.song.SongLine
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

fun saveCardContentAsPdf(context: Context, title: String, lyrics: List<SongLine>) {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    paint.textSize = 20f
    paint.isFakeBoldText = true
    canvas.drawText(title, 50f, 50f, paint)

    paint.textSize = 16f
    paint.isFakeBoldText = false
    var yPosition = 100f
    lyrics.forEach { line ->
        canvas.drawText(line.text, 50f, yPosition, paint)
        yPosition += 20f
        line.chords.forEach { chord ->
            canvas.drawText(" - ${chord.chord} at ${chord.position}", 70f, yPosition, paint)
            yPosition += 20f
        }
        yPosition += 10f
    }

    pdfDocument.finishPage(page)

    val fileName = "$title.pdf"

    try {
        val outputStream: OutputStream?

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // ✅ Για Android 10+ χρησιμοποιούμε MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
            }
            val uri = context.contentResolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            outputStream = uri?.let { context.contentResolver.openOutputStream(it) }
        } else {
            // Για παλαιότερα Android αποθηκεύουμε στο Downloads
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            outputStream = FileOutputStream(file)
        }

        outputStream?.use {
            pdfDocument.writeTo(it)
            Toast.makeText(context, "PDF saved: $fileName", Toast.LENGTH_LONG).show()
        } ?: run {
            Toast.makeText(context, "Error saving PDF", Toast.LENGTH_LONG).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    } finally {
        pdfDocument.close()
    }
}



