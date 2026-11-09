package com.example.ui.photo

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import java.io.ByteArrayOutputStream
import kotlin.math.sqrt

object ImageProcessingUtils {

    /**
     * Remove or Replace Background using Euclidean color distance & edge awareness.
     * @param source Source bitmap
     * @param targetBgColor Color.TRANSPARENT or solid replacement (White, Blue, Red, etc.)
     * @param tolerance Value 0..100 controlling color threshold
     */
    fun processBackgroundReplacement(
        source: Bitmap,
        targetBgColor: Int,
        tolerance: Float = 35f
    ): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        // Sample corner background pixels to identify dominant background color
        val corners = listOf(
            pixels[0],
            pixels[width - 1],
            pixels[(height - 1) * width],
            pixels[(height - 1) * width + width - 1],
            pixels[width / 2], // top center
            pixels[(height / 4) * width + 2], // upper left border
            pixels[(height / 4) * width + width - 3] // upper right border
        )

        // Average background color from corners
        var rSum = 0; var gSum = 0; var bSum = 0
        for (c in corners) {
            rSum += Color.red(c)
            gSum += Color.green(c)
            bSum += Color.blue(c)
        }
        val bgR = rSum / corners.size
        val bgG = gSum / corners.size
        val bgB = bSum / corners.size

        val threshold = (tolerance / 100f) * 255f

        for (i in pixels.indices) {
            val px = pixels[i]
            val r = Color.red(px)
            val g = Color.green(px)
            val b = Color.blue(px)

            // Euclidean color distance from background
            val dist = sqrt(
                ((r - bgR) * (r - bgR) +
                 (g - bgG) * (g - bgG) +
                 (b - bgB) * (b - bgB)).toDouble()
            ).toFloat()

            if (dist < threshold) {
                // Feather edge near threshold for smooth hair blending
                if (dist > threshold * 0.85f && targetBgColor == Color.TRANSPARENT) {
                    val alphaRatio = (dist - threshold * 0.85f) / (threshold * 0.15f)
                    val alpha = (alphaRatio * 255).toInt().coerceIn(0, 255)
                    pixels[i] = Color.argb(alpha, r, g, b)
                } else {
                    pixels[i] = targetBgColor
                }
            }
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Add Name & Date of Photo (DOP) imprint for SSC / UPSC / Army / Police applications.
     */
    fun addNameAndDateImprint(
        source: Bitmap,
        candidateName: String,
        dateOfPhoto: String
    ): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawBitmap(source, 0f, 0f, null)

        if (candidateName.isNotBlank() || dateOfPhoto.isNotBlank()) {
            val bannerHeight = (height * 0.18f).toInt()
            val bannerTop = height - bannerHeight

            // White banner background
            val bgPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, bannerTop.toFloat(), width.toFloat(), height.toFloat(), bgPaint)

            // Border line on top of banner
            val linePaint = Paint().apply {
                color = Color.BLACK
                strokeWidth = (width * 0.005f).coerceAtLeast(2f)
            }
            canvas.drawLine(0f, bannerTop.toFloat(), width.toFloat(), bannerTop.toFloat(), linePaint)

            val textPaint = Paint().apply {
                color = Color.BLACK
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }

            if (candidateName.isNotBlank() && dateOfPhoto.isNotBlank()) {
                textPaint.textSize = bannerHeight * 0.38f
                canvas.drawText(
                    candidateName.uppercase(),
                    width / 2f,
                    bannerTop + bannerHeight * 0.42f,
                    textPaint
                )
                textPaint.textSize = bannerHeight * 0.34f
                textPaint.isFakeBoldText = false
                canvas.drawText(
                    "DOB/DOP: $dateOfPhoto",
                    width / 2f,
                    bannerTop + bannerHeight * 0.84f,
                    textPaint
                )
            } else {
                val text = candidateName.ifBlank { "DOP: $dateOfPhoto" }
                textPaint.textSize = bannerHeight * 0.48f
                canvas.drawText(
                    text.uppercase(),
                    width / 2f,
                    bannerTop + bannerHeight * 0.65f,
                    textPaint
                )
            }
        }

        return output
    }

    /**
     * Generate A4 printable sheet with multiple passport photos & cutting guidelines.
     */
    fun generateMultiPhotoSheet(
        photo: Bitmap,
        photoCount: Int = 16
    ): Bitmap {
        // Standard A4 aspect ratio: 2480 x 3508 at 300 DPI (we use 1240 x 1754 for fast rendering)
        val sheetWidth = 1240
        val sheetHeight = 1754
        val sheet = Bitmap.createBitmap(sheetWidth, sheetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(sheet)

        // White sheet background
        canvas.drawColor(Color.WHITE)

        val (columns, rows) = when (photoCount) {
            6 -> Pair(2, 3)
            8 -> Pair(2, 4)
            12 -> Pair(3, 4)
            16 -> Pair(4, 4)
            32 -> Pair(4, 8)
            else -> Pair(4, 4)
        }

        val marginX = 80f
        val marginY = 120f
        val spacingX = 35f
        val spacingY = 40f

        val availableWidth = sheetWidth - (2 * marginX) - ((columns - 1) * spacingX)
        val photoW = availableWidth / columns
        val photoH = photoW * (4.5f / 3.5f) // 3.5cm x 4.5cm ratio

        val scaledPhoto = Bitmap.createScaledBitmap(photo, photoW.toInt(), photoH.toInt(), true)

        val borderPaint = Paint().apply {
            color = Color.rgb(200, 200, 200)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            pathEffect = DashPathEffect(floatArrayOf(6f, 6f), 0f)
        }

        var count = 0
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                if (count >= photoCount) break
                val left = marginX + c * (photoW + spacingX)
                val top = marginY + r * (photoH + spacingY)

                canvas.drawBitmap(scaledPhoto, left, top, null)
                // Draw cutting guide rectangle
                canvas.drawRect(left, top, left + photoW, top + photoH, borderPaint)
                count++
            }
        }

        // Header watermark text
        val titlePaint = Paint().apply {
            color = Color.rgb(120, 130, 145)
            textSize = 24f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("VISHAL CYBER CAFE & CSC CENTRE - PASSPORT PHOTO PRINT SHEET (A4)", sheetWidth / 2f, 60f, titlePaint)

        return sheet
    }

    /**
     * Clean and enhance signature: converts paper shadows into pure white and ink into solid dark.
     */
    fun cleanSignature(source: Bitmap, threshold: Int = 180): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val pixels = IntArray(width * height)
        source.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val px = pixels[i]
            val r = Color.red(px)
            val g = Color.green(px)
            val b = Color.blue(px)
            val luma = (0.299 * r + 0.587 * g + 0.114 * b).toInt()

            if (luma > threshold) {
                pixels[i] = Color.WHITE // Clean background
            } else {
                pixels[i] = Color.rgb(10, 25, 47) // Crisp dark signature ink
            }
        }

        output.setPixels(pixels, 0, width, 0, 0, width, height)
        return output
    }

    /**
     * Adjust brightness, contrast, and saturation.
     */
    fun adjustImage(source: Bitmap, brightness: Float, contrast: Float, saturation: Float): Bitmap {
        val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val cm = ColorMatrix()

        // Contrast
        val scale = contrast + 1f
        val translate = (-0.5f * scale + 0.5f) * 255f
        val cmContrast = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )

        // Brightness
        val bShift = brightness * 255f
        val cmBrightness = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, bShift,
                0f, 1f, 0f, 0f, bShift,
                0f, 0f, 1f, 0f, bShift,
                0f, 0f, 0f, 1f, 0f
            )
        )

        // Saturation
        val cmSat = ColorMatrix().apply { setSaturation(saturation) }

        cm.postConcat(cmContrast)
        cm.postConcat(cmBrightness)
        cm.postConcat(cmSat)

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(cm)
        }
        canvas.drawBitmap(source, 0f, 0f, paint)
        return output
    }

    /**
     * Compress bitmap to target format and return byte array with estimated KB size.
     */
    fun compressBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): Pair<ByteArray, Int> {
        val stream = ByteArrayOutputStream()
        bitmap.compress(format, quality, stream)
        val bytes = stream.toByteArray()
        val kbSize = bytes.size / 1024
        return Pair(bytes, kbSize)
    }

    /**
     * Creates a demo portrait photo for instant testing when no gallery photo is picked.
     */
    fun createSamplePortrait(): Bitmap {
        val width = 420
        val height = 540
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Light gradient background
        val bgPaint = Paint().apply { color = Color.rgb(228, 233, 242) }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Shoulders/Coat
        val coatPaint = Paint().apply {
            color = Color.rgb(24, 43, 73)
            isAntiAlias = true
        }
        canvas.drawOval(RectF(60f, 320f, 360f, 620f), coatPaint)

        // Shirt collar
        val shirtPaint = Paint().apply {
            color = Color.WHITE
            isAntiAlias = true
        }
        canvas.drawRect(180f, 300f, 240f, 360f, shirtPaint)

        // Neck
        val skinPaint = Paint().apply {
            color = Color.rgb(224, 172, 105)
            isAntiAlias = true
        }
        canvas.drawRect(185f, 240f, 235f, 320f, skinPaint)

        // Head/Face
        canvas.drawOval(RectF(130f, 110f, 290f, 290f), skinPaint)

        // Hair
        val hairPaint = Paint().apply {
            color = Color.rgb(35, 25, 20)
            isAntiAlias = true
        }
        canvas.drawOval(RectF(125f, 90f, 295f, 180f), hairPaint)

        // Eyes
        val eyePaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            isAntiAlias = true
        }
        canvas.drawCircle(175f, 195f, 8f, eyePaint)
        canvas.drawCircle(245f, 195f, 8f, eyePaint)

        // Smile
        val mouthPaint = Paint().apply {
            color = Color.rgb(160, 80, 80)
            style = Paint.Style.STROKE
            strokeWidth = 4f
            isAntiAlias = true
        }
        canvas.drawArc(RectF(185f, 230f, 235f, 255f), 0f, 180f, false, mouthPaint)

        return bitmap
    }

    /**
     * Creates a demo signature image for testing signature crop & resizer.
     */
    fun createSampleSignature(): Bitmap {
        val width = 400
        val height = 160
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Slightly yellowish paper background with noise
        canvas.drawColor(Color.rgb(248, 246, 238))

        val penPaint = Paint().apply {
            color = Color.rgb(18, 45, 115)
            style = Paint.Style.STROKE
            strokeWidth = 6f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
            isAntiAlias = true
        }

        // Draw cursive signature lines
        canvas.drawLine(50f, 90f, 110f, 50f, penPaint)
        canvas.drawLine(110f, 50f, 140f, 110f, penPaint)
        canvas.drawLine(140f, 110f, 180f, 70f, penPaint)
        canvas.drawLine(180f, 70f, 220f, 100f, penPaint)
        canvas.drawLine(220f, 100f, 270f, 60f, penPaint)
        canvas.drawLine(270f, 60f, 320f, 120f, penPaint)
        canvas.drawLine(70f, 130f, 340f, 125f, penPaint)

        return bitmap
    }
}
