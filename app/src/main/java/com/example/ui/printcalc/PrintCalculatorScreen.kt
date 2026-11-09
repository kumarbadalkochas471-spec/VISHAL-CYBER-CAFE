package com.example.ui.printcalc

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.CafeRepository
import com.example.ui.pdf.PdfGeneratorUtils
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.RoyalBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun PrintCalculatorScreen(
    repository: CafeRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Printing Types
    var printType by remember { mutableStateOf("BW_SINGLE") } // "BW_SINGLE", "BW_DOUBLE", "COLOR", "GLOSSY_PHOTO"
    var pageCount by remember { mutableIntStateOf(10) }
    var copiesCount by remember { mutableIntStateOf(1) }
    var includeLamination by remember { mutableStateOf(false) }
    var laminationSheets by remember { mutableIntStateOf(1) }
    var includeSpiralBinding by remember { mutableStateOf(false) }

    // Rates (Standard default or customizable)
    var rateBwSingle by remember { mutableDoubleStateOf(5.0) }
    var rateBwDouble by remember { mutableDoubleStateOf(8.0) }
    var rateColor by remember { mutableDoubleStateOf(15.0) }
    var rateGlossyPhoto by remember { mutableDoubleStateOf(50.0) }
    var rateLamination by remember { mutableDoubleStateOf(20.0) }
    var rateSpiral by remember { mutableDoubleStateOf(40.0) }

    // Payment method
    var paymentMethod by remember { mutableStateOf("CASH") }
    var customerName by remember { mutableStateOf("") }

    val baseRatePerPage = when (printType) {
        "BW_SINGLE" -> rateBwSingle
        "BW_DOUBLE" -> rateBwDouble
        "COLOR" -> rateColor
        "GLOSSY_PHOTO" -> rateGlossyPhoto
        else -> rateBwSingle
    }

    val printTotal = (baseRatePerPage * pageCount * copiesCount)
    val laminationTotal = if (includeLamination) (rateLamination * laminationSheets) else 0.0
    val spiralTotal = if (includeSpiralBinding) rateSpiral else 0.0
    val grandTotal = printTotal + laminationTotal + spiralTotal

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Grand Total Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = NavyDark)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PRINT & PHOTO BILL CALCULATOR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyanGlow,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "₹${"%.0f".format(grandTotal)}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 38.sp
                    )
                )

                Text(
                    text = "$pageCount Pages × $copiesCount Copies = ${pageCount * copiesCount} Total Prints",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val desc = "${when(printType) {
                                    "BW_SINGLE" -> "B&W Single Side"
                                    "BW_DOUBLE" -> "B&W Both Sides"
                                    "COLOR" -> "Color Print"
                                    "GLOSSY_PHOTO" -> "Glossy Photo Print"
                                    else -> "Print"
                                }} ($pageCount pgs × $copiesCount)"

                                repository.insertTransaction(
                                    TransactionEntity(
                                        type = "INCOME",
                                        category = if (printType == "GLOSSY_PHOTO") "Photo Print" else "Xerox / Print",
                                        amount = grandTotal,
                                        description = desc,
                                        paymentMethod = paymentMethod,
                                        customerName = customerName.ifBlank { null },
                                        dateMillis = System.currentTimeMillis()
                                    )
                                )
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Added ₹${"%.0f".format(grandTotal)} to Today's Income!", Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Income", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch(Dispatchers.Default) {
                                val settings = repository.getSettingsSnapshot()
                                val receipt = PdfGeneratorUtils.createCustomerReceiptBitmap(
                                    settings = settings,
                                    customerName = customerName.ifBlank { "Counter Customer" },
                                    customerMobile = "N/A",
                                    serviceName = "A4 Printing & Xerox (${pageCount * copiesCount} pgs)",
                                    category = "Printing",
                                    referenceNo = "PRN" + System.currentTimeMillis().toString().takeLast(6),
                                    amountCharged = grandTotal,
                                    amountPaid = grandTotal,
                                    paymentStatus = "PAID",
                                    paymentMethod = paymentMethod
                                )
                                withContext(Dispatchers.Main) {
                                    PdfGeneratorUtils.printBitmap(context, receipt, "Print_Bill")
                                }
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print Bill", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Print Type Selection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "1. Select Print Mode",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PrintModeOption(
                        title = "Black & White (Single Side)",
                        rateDesc = "₹${"%.0f".format(rateBwSingle)} / page",
                        isSelected = printType == "BW_SINGLE",
                        onClick = { printType = "BW_SINGLE" }
                    )
                    PrintModeOption(
                        title = "Black & White (Back to Back / Both Sides)",
                        rateDesc = "₹${"%.0f".format(rateBwDouble)} / sheet",
                        isSelected = printType == "BW_DOUBLE",
                        onClick = { printType = "BW_DOUBLE" }
                    )
                    PrintModeOption(
                        title = "Full Color Laser / Inkjet Print",
                        rateDesc = "₹${"%.0f".format(rateColor)} / page",
                        isSelected = printType == "COLOR",
                        onClick = { printType = "COLOR" }
                    )
                    PrintModeOption(
                        title = "High Quality Glossy Photo Print (4x6 / A4)",
                        rateDesc = "₹${"%.0f".format(rateGlossyPhoto)} / sheet",
                        isSelected = printType == "GLOSSY_PHOTO",
                        onClick = { printType = "GLOSSY_PHOTO" }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Quantity Selectors
                Text(
                    text = "2. Page Count & Copies",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Number of Pages / Sheets:", style = MaterialTheme.typography.bodyMedium)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (pageCount > 1) pageCount-- },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = "$pageCount",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )

                        IconButton(
                            onClick = { pageCount++ },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Copies / Sets:", style = MaterialTheme.typography.bodyMedium)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (copiesCount > 1) copiesCount-- },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                        }

                        Text(
                            text = "$copiesCount",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )

                        IconButton(
                            onClick = { copiesCount++ },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider()
                Spacer(modifier = Modifier.height(16.dp))

                // Add-on Services: Lamination & Spiral Binding
                Text(
                    text = "3. Add-on Services",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeLamination,
                        onCheckedChange = { includeLamination = it }
                    )
                    Text("A4 Thermal Pouch Lamination (+₹${"%.0f".format(rateLamination)} / sheet)", style = MaterialTheme.typography.bodyMedium)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeSpiralBinding,
                        onCheckedChange = { includeSpiralBinding = it }
                    )
                    Text("Spiral Book Binding with Cover (+₹${"%.0f".format(rateSpiral)})", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Customer & Payment Info
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paymentMethod == "CASH",
                        onClick = { paymentMethod = "CASH" },
                        label = { Text("Cash Payment") }
                    )
                    FilterChip(
                        selected = paymentMethod == "UPI",
                        onClick = { paymentMethod = "UPI" },
                        label = { Text("UPI (GPay/PhonePe)") }
                    )
                }
            }
        }
    }
}

@Composable
private fun PrintModeOption(
    title: String,
    rateDesc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) RoyalBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .then(
                if (isSelected) Modifier.background(RoyalBlue.copy(alpha = 0.1f)) else Modifier
            )
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) RoyalBlue else Color.LightGray.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = rateDesc,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isSelected) Color.White else Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}
