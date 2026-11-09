package com.example.ui.photo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.pdf.PdfGeneratorUtils
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.RoyalBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PhotoStudioScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("AI BG Remover", "Passport Sheet", "Signature Resizer", "Enhance & Compress")

    // Working Bitmaps
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showOriginalPreview by remember { mutableStateOf(false) }

    // BG Remover parameters
    var selectedBgOption by remember { mutableStateOf("BLUE") } // "TRANSPARENT", "WHITE", "BLUE", "RED", "GREY"
    var tolerance by remember { mutableFloatStateOf(35f) }

    // Passport Imprint parameters
    var candidateName by remember { mutableStateOf("") }
    var dateOfPhoto by remember {
        mutableStateOf(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()))
    }
    var sheetPhotoCount by remember { mutableIntStateOf(16) }
    var generatedSheetBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Signature parameters
    var signatureThreshold by remember { mutableFloatStateOf(180f) }

    // Adjustments
    var brightness by remember { mutableFloatStateOf(0f) }
    var contrast by remember { mutableFloatStateOf(0f) }
    var saturation by remember { mutableFloatStateOf(1f) }
    var compressionQuality by remember { mutableFloatStateOf(85f) }
    var compressedSizeKb by remember { mutableIntStateOf(0) }

    // Initialize with sample portrait if null
    LaunchedEffect(Unit) {
        if (originalBitmap == null) {
            val sample = ImageProcessingUtils.createSamplePortrait()
            originalBitmap = sample
            processedBitmap = sample
        }
    }

    // Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(it)?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        withContext(Dispatchers.Main) {
                            originalBitmap = bmp
                            processedBitmap = bmp
                            generatedSheetBitmap = null
                            Toast.makeText(context, "Photo loaded: ${bmp.width}x${bmp.height}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to load photo", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Process BG removal
    fun applyBackgroundRemoval() {
        val src = originalBitmap ?: return
        isProcessing = true
        scope.launch(Dispatchers.Default) {
            val targetColor = when (selectedBgOption) {
                "TRANSPARENT" -> android.graphics.Color.TRANSPARENT
                "WHITE" -> android.graphics.Color.WHITE
                "BLUE" -> android.graphics.Color.rgb(43, 108, 176) // Standard Indian Passport Sky Blue
                "RED" -> android.graphics.Color.rgb(190, 30, 30)
                "GREY" -> android.graphics.Color.rgb(220, 225, 235)
                else -> android.graphics.Color.WHITE
            }

            var result = ImageProcessingUtils.processBackgroundReplacement(src, targetColor, tolerance)
            if (candidateName.isNotBlank() || dateOfPhoto.isNotBlank()) {
                result = ImageProcessingUtils.addNameAndDateImprint(result, candidateName, dateOfPhoto)
            }

            val (_, kb) = ImageProcessingUtils.compressBitmap(
                result,
                if (selectedBgOption == "TRANSPARENT") Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG,
                compressionQuality.toInt()
            )

            withContext(Dispatchers.Main) {
                processedBitmap = result
                compressedSizeKb = kb
                isProcessing = false
            }
        }
    }

    // Apply adjustments
    fun applyAdjustments() {
        val src = originalBitmap ?: return
        isProcessing = true
        scope.launch(Dispatchers.Default) {
            val adjusted = ImageProcessingUtils.adjustImage(src, brightness, contrast, saturation)
            val (_, kb) = ImageProcessingUtils.compressBitmap(adjusted, Bitmap.CompressFormat.JPEG, compressionQuality.toInt())
            withContext(Dispatchers.Main) {
                processedBitmap = adjusted
                compressedSizeKb = kb
                isProcessing = false
            }
        }
    }

    // Generate sheet
    fun makePassportSheet() {
        val currentPhoto = processedBitmap ?: originalBitmap ?: return
        isProcessing = true
        scope.launch(Dispatchers.Default) {
            val sheet = ImageProcessingUtils.generateMultiPhotoSheet(currentPhoto, sheetPhotoCount)
            withContext(Dispatchers.Main) {
                generatedSheetBitmap = sheet
                isProcessing = false
            }
        }
    }

    // Clean signature
    fun applySignatureClean() {
        val src = originalBitmap ?: return
        isProcessing = true
        scope.launch(Dispatchers.Default) {
            val cleaned = ImageProcessingUtils.cleanSignature(src, signatureThreshold.toInt())
            val (_, kb) = ImageProcessingUtils.compressBitmap(cleaned, Bitmap.CompressFormat.JPEG, 75)
            withContext(Dispatchers.Main) {
                processedBitmap = cleaned
                compressedSizeKb = kb
                isProcessing = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Header
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = NavyDark,
            contentColor = Color.White
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        selectedTab = index
                        if (index == 2 && originalBitmap == null) {
                            val sig = ImageProcessingUtils.createSampleSignature()
                            originalBitmap = sig
                            processedBitmap = sig
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) CyanGlow else Color(0xFFBAC7D5)
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Source Image Control Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Photo Studio Station",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = if (compressedSizeKb > 0) "Estimated Size: ~$compressedSizeKb KB" else "Ready to process",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (compressedSizeKb in 10..50) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val sample = if (selectedTab == 2) ImageProcessingUtils.createSampleSignature() else ImageProcessingUtils.createSamplePortrait()
                                originalBitmap = sample
                                processedBitmap = sample
                                generatedSheetBitmap = null
                                Toast.makeText(context, "Loaded demo image", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Sample", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload", fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Photo Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val displayBitmap = if (showOriginalPreview) originalBitmap else (processedBitmap ?: originalBitmap)

                    if (displayBitmap != null) {
                        Image(
                            bitmap = displayBitmap.asImageBitmap(),
                            contentDescription = "Photo Preview",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        )
                    }

                    if (isProcessing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = CyanGlow)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Processing Image...", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }

                    // Before/After comparison button
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .clickable { showOriginalPreview = !showOriginalPreview }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Compare,
                                    contentDescription = "Compare",
                                    tint = CyanGlow,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (showOriginalPreview) "Viewing ORIGINAL" else "Compare (Tap to hold)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab-Specific Controls
            when (selectedTab) {
                0 -> {
                    // TAB 0: AI BG REMOVER & COLOR REPLACER
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Select Background Color",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BgColorChip(
                                    name = "Passport Blue",
                                    color = Color(0xFF2B6CB0),
                                    isSelected = selectedBgOption == "BLUE",
                                    onClick = { selectedBgOption = "BLUE" }
                                )
                                BgColorChip(
                                    name = "Pure White",
                                    color = Color.White,
                                    isSelected = selectedBgOption == "WHITE",
                                    onClick = { selectedBgOption = "WHITE" },
                                    hasBorder = true
                                )
                                BgColorChip(
                                    name = "Transparent",
                                    color = Color.Gray,
                                    isSelected = selectedBgOption == "TRANSPARENT",
                                    onClick = { selectedBgOption = "TRANSPARENT" }
                                )
                                BgColorChip(
                                    name = "Red",
                                    color = Color(0xFFBE1E2D),
                                    isSelected = selectedBgOption == "RED",
                                    onClick = { selectedBgOption = "RED" }
                                )
                                BgColorChip(
                                    name = "Light Grey",
                                    color = Color(0xFFDCE1EB),
                                    isSelected = selectedBgOption == "GREY",
                                    onClick = { selectedBgOption = "GREY" }
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Edge & Color Tolerance (${tolerance.toInt()})",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Slider(
                                value = tolerance,
                                onValueChange = { tolerance = it },
                                valueRange = 10f..80f,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "SSC / UPSC Name & Date of Photo Imprint (Optional)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = candidateName,
                                    onValueChange = { candidateName = it },
                                    label = { Text("Candidate Name") },
                                    modifier = Modifier.weight(1.3f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = dateOfPhoto,
                                    onValueChange = { dateOfPhoto = it },
                                    label = { Text("DOP (dd/mm/yyyy)") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { applyBackgroundRemoval() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Remove & Apply Background Now")
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: PASSPORT MULTI-PHOTO SHEET (A4)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "A4 Sheet Passport Photo Layout",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Arrange multiple 3.5cm x 4.5cm photos with cutting guide borders",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(6, 8, 12, 16, 32).forEach { count ->
                                    FilterChip(
                                        selected = sheetPhotoCount == count,
                                        onClick = { sheetPhotoCount = count },
                                        label = { Text("$count Photos", fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { makePassportSheet() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PhotoAlbum, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate $sheetPhotoCount-Photo A4 Sheet")
                            }

                            if (generatedSheetBitmap != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Divider()
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Generated A4 Printable Preview:",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Image(
                                    bitmap = generatedSheetBitmap!!.asImageBitmap(),
                                    contentDescription = "A4 Sheet",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(280.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        generatedSheetBitmap?.let {
                                            PdfGeneratorUtils.printBitmap(context, it, "Passport_Photo_Sheet_A4")
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Print A4 Sheet to Connected Printer")
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: SIGNATURE RESIZER & CLEANER
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Signature Cleaner & Size Compressor",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Cleans mobile paper shadows and optimizes to 10KB - 20KB limit for SSC/BPSC/UPSC",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Background Paper Whiteness Threshold (${signatureThreshold.toInt()})",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Slider(
                                value = signatureThreshold,
                                onValueChange = { signatureThreshold = it },
                                valueRange = 100f..230f,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = { applySignatureClean() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Draw, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clean Paper & Sharpen Ink (10-20KB)")
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: ENHANCE & COMPRESS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Brightness, Contrast & File Compression",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Brightness (${"%.2f".format(brightness)})", style = MaterialTheme.typography.labelSmall)
                            Slider(value = brightness, onValueChange = { brightness = it; applyAdjustments() }, valueRange = -0.5f..0.5f)

                            Text("Contrast (${"%.2f".format(contrast)})", style = MaterialTheme.typography.labelSmall)
                            Slider(value = contrast, onValueChange = { contrast = it; applyAdjustments() }, valueRange = -0.5f..0.8f)

                            Text("Saturation (${"%.2f".format(saturation)})", style = MaterialTheme.typography.labelSmall)
                            Slider(value = saturation, onValueChange = { saturation = it; applyAdjustments() }, valueRange = 0f..2f)

                            Text("JPG Quality (${compressionQuality.toInt()}%)", style = MaterialTheme.typography.labelSmall)
                            Slider(value = compressionQuality, onValueChange = { compressionQuality = it; applyAdjustments() }, valueRange = 20f..100f)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Save & Print
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val bmpToSave = processedBitmap ?: originalBitmap ?: return@OutlinedButton
                        scope.launch(Dispatchers.IO) {
                            try {
                                val file = File(context.cacheDir, "vishal_cyber_photo_${System.currentTimeMillis()}.jpg")
                                FileOutputStream(file).use { out ->
                                    bmpToSave.compress(Bitmap.CompressFormat.JPEG, compressionQuality.toInt(), out)
                                }
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Saved image to: ${file.name} (${file.length() / 1024} KB)", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save JPG/PNG")
                }

                Button(
                    onClick = {
                        val bmpToPrint = processedBitmap ?: originalBitmap ?: return@Button
                        PdfGeneratorUtils.printBitmap(context, bmpToPrint, "Customer_Passport_Photo")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Direct Print")
                }
            }
        }
    }
}

@Composable
private fun BgColorChip(
    name: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    hasBorder: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) RoyalBlue.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (isSelected) 2.dp else if (hasBorder) 1.dp else 0.dp,
                color = if (isSelected) RoyalBlue else Color.LightGray,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(if (hasBorder) Modifier.border(1.dp, Color.Gray, CircleShape) else Modifier)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            )
        }
    }
}
