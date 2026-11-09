package com.example.ui.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import com.example.data.local.entity.ApplicationRecordEntity
import com.example.data.local.entity.CafeSettingsEntity
import com.example.data.local.entity.CustomerEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGeneratorUtils {

    // Standard A4 dimensions at 72 points per inch (Android PdfDocument standard: 595 x 842 points)
    const val PAGE_WIDTH = 595
    const val PAGE_HEIGHT = 842

    /**
     * Create PDF from a list of bitmaps (Images to PDF tool).
     */
    fun createPdfFromImages(
        context: Context,
        images: List<Bitmap>,
        watermarkText: String = "",
        includePageNumbers: Boolean = true
    ): File {
        val document = PdfDocument()

        images.forEachIndexed { index, bitmap ->
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, index + 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // White background
            canvas.drawColor(Color.WHITE)

            // Scale bitmap to fit page with margins
            val margin = 36f
            val maxW = PAGE_WIDTH - 2 * margin
            val maxH = PAGE_HEIGHT - 2 * margin - 20f

            val scale = (maxW / bitmap.width).coerceAtMost(maxH / bitmap.height)
            val scaledW = bitmap.width * scale
            val scaledH = bitmap.height * scale
            val left = (PAGE_WIDTH - scaledW) / 2f
            val top = margin + ((maxH - scaledH) / 2f)

            val destRect = Rect(left.toInt(), top.toInt(), (left + scaledW).toInt(), (top + scaledH).toInt())
            canvas.drawBitmap(bitmap, null, destRect, null)

            // Watermark if requested
            if (watermarkText.isNotBlank()) {
                val wmPaint = Paint().apply {
                    color = Color.argb(45, 100, 100, 100)
                    textSize = 42f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.save()
                canvas.rotate(-45f, PAGE_WIDTH / 2f, PAGE_HEIGHT / 2f)
                canvas.drawText(watermarkText, PAGE_WIDTH / 2f, PAGE_HEIGHT / 2f, wmPaint)
                canvas.restore()
            }

            // Page numbers
            if (includePageNumbers) {
                val numPaint = Paint().apply {
                    color = Color.GRAY
                    textSize = 12f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("Page ${index + 1} of ${images.size}", PAGE_WIDTH / 2f, PAGE_HEIGHT - 16f, numPaint)
            }

            document.finishPage(page)
        }

        val outputFile = File(context.cacheDir, "vishal_cyber_doc_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()
        return outputFile
    }

    /**
     * Create PDF from Text (Text to PDF tool / Application letters).
     */
    fun createPdfFromText(
        context: Context,
        title: String,
        bodyText: String,
        watermarkText: String = ""
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        canvas.drawColor(Color.WHITE)

        val headerPaint = Paint().apply {
            color = Color.rgb(11, 25, 44) // Navy
            textSize = 20f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 13f
            isAntiAlias = true
        }

        var y = 50f
        canvas.drawText(title, 40f, y, headerPaint)
        y += 24f

        val dividerPaint = Paint().apply {
            color = Color.rgb(30, 86, 160)
            strokeWidth = 2f
        }
        canvas.drawLine(40f, y, (PAGE_WIDTH - 40).toFloat(), y, dividerPaint)
        y += 30f

        // Wrap and draw body lines
        val lines = bodyText.split("\n")
        val maxLineWidth = PAGE_WIDTH - 80f

        for (rawLine in lines) {
            val words = rawLine.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (bodyPaint.measureText(testLine) > maxLineWidth) {
                    canvas.drawText(currentLine, 40f, y, bodyPaint)
                    y += 18f
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine, 40f, y, bodyPaint)
                y += 18f
            }
            y += 6f
            if (y > PAGE_HEIGHT - 60) break
        }

        // Footer
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 10f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Generated by VISHAL CYBER CAFE & CSC CENTRE, Kochas", PAGE_WIDTH / 2f, PAGE_HEIGHT - 20f, footerPaint)

        document.finishPage(page)

        val outputFile = File(context.cacheDir, "vishal_cyber_text_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()
        return outputFile
    }

    /**
     * Create Printable Customer Invoice / Receipt Bitmap.
     */
    fun createCustomerReceiptBitmap(
        settings: CafeSettingsEntity,
        customerName: String,
        customerMobile: String,
        serviceName: String,
        category: String,
        referenceNo: String,
        amountCharged: Double,
        amountPaid: Double,
        paymentStatus: String,
        paymentMethod: String
    ): Bitmap {
        val width = 750
        val height = 1050
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.WHITE)

        val borderPaint = Paint().apply {
            color = Color.rgb(30, 86, 160)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRect(20f, 20f, width - 20f, height - 20f, borderPaint)

        // Header Background
        val headerBg = Paint().apply {
            color = Color.rgb(11, 25, 44)
        }
        canvas.drawRect(20f, 20f, width - 20f, 150f, headerBg)

        // Business Title
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(settings.businessName, width / 2f, 70f, titlePaint)

        val subTitlePaint = Paint().apply {
            color = Color.rgb(245, 158, 11) // Gold
            textSize = 16f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(settings.tagline, width / 2f, 100f, subTitlePaint)

        val addressPaint = Paint().apply {
            color = Color.rgb(220, 225, 235)
            textSize = 14f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(settings.address, width / 2f, 130f, addressPaint)

        // Receipt Meta (No, Date)
        val metaPaint = Paint().apply {
            color = Color.rgb(60, 60, 60)
            textSize = 16f
            isAntiAlias = true
        }
        val dateStr = SimpleDateFormat("dd-MMM-yyyy hh:mm a", Locale.getDefault()).format(Date())
        val receiptNo = "VCC-" + System.currentTimeMillis().toString().takeLast(6)

        canvas.drawText("Receipt No: $receiptNo", 45f, 195f, metaPaint)
        canvas.drawText("Date: $dateStr", width - 320f, 195f, metaPaint)

        // Customer Details Box
        val boxPaint = Paint().apply {
            color = Color.rgb(244, 247, 252)
            style = Paint.Style.FILL
        }
        canvas.drawRect(40f, 220f, width - 40f, 320f, boxPaint)

        val boldPaint = Paint().apply {
            color = Color.rgb(11, 25, 44)
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("CUSTOMER DETAILS", 55f, 250f, boldPaint)
        canvas.drawText("Name: $customerName", 55f, 280f, metaPaint)
        canvas.drawText("Mobile: $customerMobile", 55f, 305f, metaPaint)

        // Table Header
        val tableHeaderPaint = Paint().apply {
            color = Color.rgb(30, 86, 160)
            style = Paint.Style.FILL
        }
        canvas.drawRect(40f, 350f, width - 40f, 395f, tableHeaderPaint)

        val thText = Paint().apply {
            color = Color.WHITE
            textSize = 16f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("Item / Description", 55f, 380f, thText)
        canvas.drawText("Category", 420f, 380f, thText)
        canvas.drawText("Amount", width - 150f, 380f, thText)

        // Item Rows
        val rowPaint = Paint().apply {
            color = Color.rgb(30, 30, 30)
            textSize = 16f
            isAntiAlias = true
        }
        canvas.drawText(serviceName.take(34), 55f, 435f, rowPaint)
        canvas.drawText(category.take(18), 420f, 435f, rowPaint)
        canvas.drawText("Rs. ${"%.2f".format(amountCharged)}", width - 150f, 435f, boldPaint)

        if (referenceNo.isNotBlank()) {
            val refPaint = Paint().apply {
                color = Color.rgb(100, 100, 100)
                textSize = 14f
            }
            canvas.drawText("Ref/Ack No: $referenceNo", 55f, 465f, refPaint)
        }

        // Totals & Due
        val divPaint = Paint().apply {
            color = Color.rgb(200, 210, 225)
            strokeWidth = 2f
        }
        canvas.drawLine(40f, 520f, width - 40f, 520f, divPaint)

        val dueAmount = (amountCharged - amountPaid).coerceAtLeast(0.0)
        canvas.drawText("Total Amount:", width - 350f, 560f, rowPaint)
        canvas.drawText("Rs. ${"%.2f".format(amountCharged)}", width - 150f, 560f, boldPaint)

        canvas.drawText("Amount Paid (${paymentMethod}):", width - 350f, 595f, rowPaint)
        val paidPaint = Paint().apply {
            color = Color.rgb(16, 185, 129) // Green
            textSize = 18f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("Rs. ${"%.2f".format(amountPaid)}", width - 150f, 595f, paidPaint)

        if (dueAmount > 0) {
            val duePaint = Paint().apply {
                color = Color.rgb(239, 68, 68) // Red
                textSize = 18f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("Balance Due:", width - 350f, 630f, rowPaint)
            canvas.drawText("Rs. ${"%.2f".format(dueAmount)}", width - 150f, 630f, duePaint)
        }

        // Payment status badge
        val badgeBg = Paint().apply {
            color = if (dueAmount <= 0) Color.rgb(209, 250, 229) else Color.rgb(254, 226, 226)
        }
        canvas.drawRoundRect(45f, 540f, 220f, 590f, 12f, 12f, badgeBg)
        val badgeText = Paint().apply {
            color = if (dueAmount <= 0) Color.rgb(6, 95, 70) else Color.rgb(153, 27, 27)
            textSize = 16f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(if (dueAmount <= 0) "PAID IN FULL" else "PAYMENT DUE", 132f, 572f, badgeText)

        // UPI Payment Info Box
        canvas.drawRect(40f, 700f, width - 40f, 820f, boxPaint)
        canvas.drawText("UPI DIGITAL PAYMENT SUPPORT", 55f, 735f, boldPaint)
        canvas.drawText("Pay directly to Shop UPI ID: ${settings.upiId}", 55f, 765f, metaPaint)
        canvas.drawText("PhonePe / GooglePay / Paytm accepted. Call: ${settings.mobile}", 55f, 795f, metaPaint)

        // Signatures
        canvas.drawLine(80f, 950f, 280f, 950f, divPaint)
        canvas.drawText("Customer Signature", 90f, 980f, metaPaint)

        canvas.drawLine(width - 280f, 950f, width - 80f, 950f, divPaint)
        canvas.drawText("Authorized Seal & Sign", width - 270f, 980f, metaPaint)
        canvas.drawText(settings.ownerName, width - 230f, 930f, boldPaint)

        return bitmap
    }

    /**
     * Send Bitmap to connected printer via Android PrintManager.
     */
     fun printBitmap(context: Context, bitmap: Bitmap, jobName: String = "Vishal_Cyber_Cafe_Print") {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
            val adapter = object : PrintDocumentAdapter() {
                private var printedPdfDocument: PrintedPdfDocument? = null

                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback,
                    extras: Bundle?
                ) {
                    printedPdfDocument = PrintedPdfDocument(context, newAttributes)
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder(jobName)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback
                ) {
                    val pdfDoc = printedPdfDocument ?: return
                    val page = pdfDoc.startPage(0)

                    if (cancellationSignal?.isCanceled == true) {
                        callback.onWriteCancelled()
                        pdfDoc.close()
                        return
                    }

                    val pageCanvas = page.canvas
                    val pw = page.info.pageWidth
                    val ph = page.info.pageHeight
                    val scale = (pw.toFloat() / bitmap.width).coerceAtMost(ph.toFloat() / bitmap.height)
                    val sw = bitmap.width * scale
                    val sh = bitmap.height * scale
                    val left = (pw - sw) / 2f
                    val top = (ph - sh) / 2f

                    pageCanvas.drawBitmap(bitmap, null, Rect(left.toInt(), top.toInt(), (left + sw).toInt(), (top + sh).toInt()), null)
                    pdfDoc.finishPage(page)

                    try {
                        FileOutputStream(destination.fileDescriptor).use { out ->
                            pdfDoc.writeTo(out)
                        }
                        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback.onWriteFailed(e.message)
                    } finally {
                        pdfDoc.close()
                    }
                }
            }
            printManager.print(jobName, adapter, PrintAttributes.Builder().build())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
