package com.example.ui.documents

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.pdf.PdfGeneratorUtils
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.NavyDark
import com.example.ui.theme.RoyalBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DocumentToolsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Resume / Biodata", "Application Letters", "Hindi Typing Helper")

    // Resume / Biodata State
    var bioName by remember { mutableStateOf("Rahul Kumar") }
    var bioFatherName by remember { mutableStateOf("Satendra Singh") }
    var bioDob by remember { mutableStateOf("15/08/2000") }
    var bioMobile by remember { mutableStateOf("9871234560") }
    var bioAddress by remember { mutableStateOf("Vill: Kochas, Ward No. 16, Dist: Rohtas, Bihar - 821112") }
    var bioCategory by remember { mutableStateOf("OBC (BC-II)") }
    var bioEducation by remember {
        mutableStateOf("• Matriculation (10th): BSEB Patna - 72%\n• Intermediate (12th Science): BSEB Patna - 68%\n• B.Sc (Mathematics): Veer Kunwar Singh University - 65%")
    }
    var bioSkills by remember {
        mutableStateOf("• DCA Computer Course\n• Hindi & English Typing (35 WPM)\n• MS Office, Excel & Internet operations")
    }

    // Letter Generator State
    var selectedTemplateIndex by remember { mutableIntStateOf(0) }
    val letterTemplates = listOf(
        "Bank KYC & Mobile Update",
        "School/College Leave Application",
        "Police FIR Missing Document",
        "College Character Certificate (CLC)"
    )

    var letterTitle by remember { mutableStateOf("Application for Updating Mobile Number in Bank Account") }
    var letterBody by remember {
        mutableStateOf(
            "To,\nThe Branch Manager,\nState Bank of India (SBI),\nKochas Branch, Rohtas, Bihar\n\nSubject: Request to update mobile number and KYC in Savings Account.\n\nRespected Sir/Madam,\n\nI hold a Savings Bank Account in your branch with Account Number: 382910XXXXX.\nMy previous mobile number is no longer active. Therefore, I request you to kindly update my new mobile number in my account records so that I can receive transaction SMS and OTPs.\n\nAccount Details:\nAccount Holder Name: Rahul Kumar\nAccount Number: 382910XXXXX\nNew Mobile Number: 9871234560\nAadhaar Number: XXXX-XXXX-4560\n\nI have attached self-attested copies of my Aadhaar Card and Bank Passbook front page for your verification.\n\nThanking you.\nYours faithfully,\n\nRahul Kumar\nMobile: 9871234560"
        )
    }

    // Hindi Unicode Typing State
    var hindiInputText by remember { mutableStateOf("विशाल साइबर कैफे एवं सीएससी सेंटर, कोचस, रोहतास, बिहार") }

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
                    // TAB 0: RESUME / BIODATA MAKER
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "A4 Biodata & Resume Maker",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Generate print-ready marriage biodata or job resume in seconds",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = bioName,
                                onValueChange = { bioName = it },
                                label = { Text("Candidate Full Name") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = bioFatherName,
                                    onValueChange = { bioFatherName = it },
                                    label = { Text("Father's Name") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = bioDob,
                                    onValueChange = { bioDob = it },
                                    label = { Text("Date of Birth") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = bioMobile,
                                    onValueChange = { bioMobile = it },
                                    label = { Text("Mobile Number") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = bioCategory,
                                    onValueChange = { bioCategory = it },
                                    label = { Text("Category / Caste") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bioAddress,
                                onValueChange = { bioAddress = it },
                                label = { Text("Permanent Address") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bioEducation,
                                onValueChange = { bioEducation = it },
                                label = { Text("Educational Qualifications") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = bioSkills,
                                onValueChange = { bioSkills = it },
                                label = { Text("Technical Skills & Typing") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.Default) {
                                        val fullBiodataText = "CURRICULUM VITAE / BIODATA\n\n" +
                                                "Candidate Name: $bioName\n" +
                                                "Father's Name: $bioFatherName\n" +
                                                "Date of Birth: $bioDob\n" +
                                                "Mobile: $bioMobile\n" +
                                                "Category: $bioCategory\n" +
                                                "Address: $bioAddress\n\n" +
                                                "EDUCATIONAL QUALIFICATIONS:\n$bioEducation\n\n" +
                                                "TECHNICAL SKILLS & EXPERIENCE:\n$bioSkills\n\n" +
                                                "DECLARATION:\nI hereby declare that all the information provided above is true and correct to the best of my knowledge.\n\n" +
                                                "Place: Kochas, Rohtas\nDate: " +
                                                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date()) +
                                                "\n\n\n(Signature of Candidate)"

                                        val file = PdfGeneratorUtils.createPdfFromText(
                                            context = context,
                                            title = "RESUME - $bioName",
                                            bodyText = fullBiodataText
                                        )

                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Exported Resume PDF: ${file.name}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Printable A4 Resume PDF")
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: APPLICATION LETTER GENERATOR
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Cyber Cafe Formal Letter Templates",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Select ready-made templates for official government, police & bank works",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Template selection chips
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                letterTemplates.forEachIndexed { idx, templateName ->
                                    FilterChip(
                                        selected = selectedTemplateIndex == idx,
                                        onClick = {
                                            selectedTemplateIndex = idx
                                            when (idx) {
                                                0 -> {
                                                    letterTitle = "Application for Updating Mobile Number in Bank Account"
                                                    letterBody = "To,\nThe Branch Manager,\nState Bank of India,\nKochas, Rohtas, Bihar\n\nSubject: Request to update mobile number and KYC in Savings Account.\n\nRespected Sir/Madam,\n\nI hold a Savings Bank Account in your branch with Account Number: 382910XXXXX.\nMy registered mobile number is no longer active. Kindly update my new mobile number in my account records.\n\nAccount Details:\nAccount Holder Name: Ramesh Kumar\nAccount Number: 382910XXXXX\nNew Mobile: 9871234560\n\nAttached: Copy of Aadhaar Card and Passbook.\n\nYours faithfully,\nRamesh Kumar"
                                                }
                                                1 -> {
                                                    letterTitle = "Application for Leave of Absence"
                                                    letterBody = "To,\nThe Principal / Headmaster,\nHigh School, Kochas, Rohtas, Bihar\n\nSubject: Application for 3 days leave due to fever.\n\nRespected Sir,\n\nI am a student of Class X, Roll No: 24. I have been suffering from viral fever and doctor has advised complete rest for 3 days.\nKindly grant me leave of absence from 10/11/2026 to 12/11/2026.\n\nThanking you.\nYours obediently,\n\nStudent Name: Rahul Kumar\nClass: X, Roll: 24"
                                                }
                                                2 -> {
                                                    letterTitle = "Information / Application for Lost Documents (Station Diary / FIR)"
                                                    letterBody = "To,\nThe Station House Officer (SHO),\nKochas Police Station, Rohtas, Bihar\n\nSubject: Report regarding loss of Original Matriculation Certificate & Aadhaar Card.\n\nRespected Sir,\n\nI wish to report that on 08/11/2026, while traveling from Kochas Bazar to Sasaram, my file folder containing my Original Matriculation Certificate (Roll Code: 71024, Roll No: 20012) and Aadhaar Card was accidentally lost.\nDespite diligent search, it could not be recovered.\nKindly register a Station Diary (Sanha) entry and issue an acknowledgment copy for obtaining duplicate certificates.\n\nYours faithfully,\n\nApplicant Name: Manoj Singh\nFather's Name: Harishankar Singh\nAddress: Village Baligaon, Kochas, Rohtas\nMobile: 9934125678"
                                                }
                                                3 -> {
                                                    letterTitle = "Application for College Leaving & Character Certificate (CLC)"
                                                    letterBody = "To,\nThe Principal,\nSher Shah College, Sasaram, Rohtas\n\nSubject: Request for issuance of College Leaving Certificate (CLC) and Character Certificate.\n\nRespected Sir,\n\nI was a regular student of B.Sc. (Session 2022-2025) with Roll No: 182 and Registration No: BR229102.\nI have successfully passed the graduation degree examination without any pending dues.\nI need my College Leaving Certificate and Character Certificate for taking admission in Post Graduation.\n\nKindly issue the certificates at your earliest convenience.\n\nYours obediently,\n\nStudent: Priya Kumari\nMobile: 9123456789"
                                                }
                                            }
                                        },
                                        label = { Text(templateName, fontSize = 11.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = letterTitle,
                                onValueChange = { letterTitle = it },
                                label = { Text("Letter Heading") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = letterBody,
                                onValueChange = { letterBody = it },
                                label = { Text("Editable Letter Content") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.Default) {
                                        val file = PdfGeneratorUtils.createPdfFromText(
                                            context = context,
                                            title = letterTitle,
                                            bodyText = letterBody
                                        )
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Printed/Exported Letter: ${file.name}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Export & Print A4 Official Letter")
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: HINDI TYPING & CONVERTER HELPER
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Hindi Unicode & Cyber Cafe Typing Tool",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Standard Mangal / Unicode Hindi text for Bihar RTPS, Land mutation and online forms",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = hindiInputText,
                                onValueChange = { hindiInputText = it },
                                label = { Text("Hindi Text Box (यूनिकोड हिंदी टेक्स्ट)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text("Quick Hindi Phrase Shortcuts for Forms:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Spacer(modifier = Modifier.height(6.dp))

                            val phrases = listOf(
                                "निवासी: ग्राम+पोस्ट कोचस, रोहतास",
                                "जाति: अन्य पिछड़ा वर्ग (BC-II)",
                                "पेशा: कृषि / छात्र",
                                "वार्षिक आय: ₹ 90,000/- मात्र",
                                "शपथ पत्र: सभी विवरण सत्य एवं सही हैं।"
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                phrases.forEach { phrase ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                            .clickable {
                                                hindiInputText = "$hindiInputText\n$phrase"
                                                Toast.makeText(context, "Appended phrase", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = "+ $phrase", fontSize = 12.sp, color = RoyalBlue)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
