package com.example.ui.pdf

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.photo.ImageProcessingUtils
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.NavyDark
import com.example.ui.theme.RoyalBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfToolsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Images to PDF", "Text to PDF", "PDF Utilities Guide")

    // Images to PDF State
    val imageList = remember { mutableStateListOf<Bitmap>() }
    var watermarkText by remember { mutableStateOf("VISHAL CYBER CAFE") }
    var includePageNumbers by remember { mutableStateOf(true) }
    var generatedPdfFile by remember { mutableStateOf<File?>(null) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    // Text to PDF State
    var docTitle by remember { mutableStateOf("AFFIDAVIT / DECLARATION APPLICATION") }
    var docBody by remember {
        mutableStateOf(
            "To,\nThe Sub-Divisional Magistrate,\nKochas, Rohtas, Bihar\n\nSubject: Application for Verification and Issuance of Official Certificate.\n\nRespected Sir/Madam,\n\nI hereby declare that all the information and supporting identification documents furnished by me are genuine and verified as per my knowledge.\n\nApplicant Name: Ramesh Kumar Sharma\nFather's Name: Dinanath Sharma\nAddress: Village Parsathua, Ward No. 16, Kochas, Rohtas – 821112\nMobile: 9871234560\n\nThanking you.\nYours faithfully,\n\n(Signature of Applicant)"
        )
    }

    // Photo picker for PDF
    val multiPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(10)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            scope.launch(Dispatchers.IO) {
                uris.forEach { uri ->
                    try {
                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val bmp = BitmapFactory.decodeStream(stream)
                            withContext(Dispatchers.Main) {
                                imageList.add(bmp)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = NavyDark,
            contentColor = Color.White
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
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
            when (selectedTab) {
                0 -> {
                    // TAB 0: IMAGES TO PDF
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Convert Multiple Images to A4 PDF",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Combine Aadhaar, Marksheets, and Certificates into a single official PDF",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        multiPhotoPicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Documents", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        // Load 2 sample document pages for instant testing
                                        val p1 = ImageProcessingUtils.createSamplePortrait()
                                        val p2 = ImageProcessingUtils.createSampleSignature()
                                        imageList.add(p1)
                                        imageList.add(p2)
                                        Toast.makeText(context, "Added 2 sample pages", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Add Sample Pages", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Selected Images Horizontal List
                            if (imageList.isNotEmpty()) {
                                Text(
                                    text = "Selected Pages (${imageList.size}):",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    itemsIndexed(imageList) { index, bmp ->
                                        Box(
                                            modifier = Modifier
                                                .size(100.dp, 130.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                                        ) {
                                            Image(
                                                bitmap = bmp.asImageBitmap(),
                                                contentDescription = "Page ${index + 1}",
                                                modifier = Modifier.fillMaxSize()
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(alpha = 0.6f))
                                                    .padding(2.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("Page ${index + 1}", color = Color.White, fontSize = 10.sp)
                                            }

                                            IconButton(
                                                onClick = { imageList.removeAt(index) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(24.dp)
                                                    .background(Color.Red.copy(alpha = 0.8f), CircleShape)
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Options: Watermark & Page Numbering
                            OutlinedTextField(
                                value = watermarkText,
                                onValueChange = { watermarkText = it },
                                label = { Text("Custom Diagonal Watermark Text") },
                                placeholder = { Text("e.g., VISHAL CYBER CAFE / VERIFIED COPY") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { includePageNumbers = !includePageNumbers }
                            ) {
                                Checkbox(
                                    checked = includePageNumbers,
                                    onCheckedChange = { includePageNumbers = it }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add page numbering footer (e.g. Page 1 of 3)", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (imageList.isEmpty()) {
                                        Toast.makeText(context, "Please add at least 1 image or sample page", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    isGeneratingPdf = true
                                    scope.launch(Dispatchers.Default) {
                                        val file = PdfGeneratorUtils.createPdfFromImages(
                                            context = context,
                                            images = imageList.toList(),
                                            watermarkText = watermarkText,
                                            includePageNumbers = includePageNumbers
                                        )
                                        withContext(Dispatchers.Main) {
                                            generatedPdfFile = file
                                            isGeneratingPdf = false
                                            Toast.makeText(context, "Generated PDF: ${file.name} (${file.length() / 1024} KB)", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate A4 PDF Now")
                            }

                            if (generatedPdfFile != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFD1FAE5))
                                        .padding(14.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF059669))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("PDF Generated Successfully!", fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                                            Text("${generatedPdfFile!!.name} • ${generatedPdfFile!!.length() / 1024} KB", fontSize = 11.sp, color = Color(0xFF065F46))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: TEXT TO PDF
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Text to Printable A4 Document / PDF",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Draft instant applications, affidavits, notices, and print-ready letters",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = docTitle,
                                onValueChange = { docTitle = it },
                                label = { Text("Document Header / Title") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = docBody,
                                onValueChange = { docBody = it },
                                label = { Text("Document Body (Hindi or English text)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.Default) {
                                        val file = PdfGeneratorUtils.createPdfFromText(
                                            context = context,
                                            title = docTitle,
                                            bodyText = docBody
                                        )
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Saved Text PDF: ${file.name}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export Printable A4 PDF")
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: PDF UTILITIES & CSC REFERENCE GUIDE
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Cyber Cafe PDF Operations Guide",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            PdfGuideItem(
                                title = "1. Aadhaar PDF Password Format",
                                desc = "First 4 letters of customer name in CAPITAL letters + 4-digit Year of Birth (e.g., RAMESH born in 1998 -> RAME1998)."
                            )

                            PdfGuideItem(
                                title = "2. SSC & BPSC PDF Size Rules",
                                desc = "Candidate certificates must be under 300KB or 500KB. Use Black & White / Grayscale scanning at 150-200 DPI for sharp compact PDFs."
                            )

                            PdfGuideItem(
                                title = "3. Bank & Income Tax Form 16 / e-PAN",
                                desc = "Password is PAN in lower-case or Date of Birth DDMMYYYY. Avoid sharing passwords with third parties."
                            )

                            PdfGuideItem(
                                title = "4. Direct Thermal & A4 Printing",
                                desc = "Android PrintHelper automatically scales and sends pages to any WiFi / OTG connected laser or inkjet printer."
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfGuideItem(
    title: String,
    desc: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = RoyalBlue))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}
