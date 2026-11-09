package com.example.ui.applications

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ApplicationRecordEntity
import com.example.data.repository.CafeRepository
import com.example.ui.pdf.PdfGeneratorUtils
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.NavyDark
import com.example.ui.theme.RoyalBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ApplicationViewModel(private val repository: CafeRepository) : ViewModel() {
    val selectedFilter = MutableStateFlow("ALL")

    val filteredApplications: StateFlow<List<ApplicationRecordEntity>> = combine(
        repository.getAllApplications(),
        selectedFilter
    ) { apps, filter ->
        when (filter) {
            "ALL" -> apps
            "PENDING" -> apps.filter { it.status == "PENDING" || it.status == "IN_PROGRESS" }
            "SUBMITTED" -> apps.filter { it.status == "SUBMITTED" }
            "COMPLETED" -> apps.filter { it.status == "COMPLETED" }
            else -> apps
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addApplication(app: ApplicationRecordEntity) {
        viewModelScope.launch {
            repository.insertApplication(app)
        }
    }

    fun updateStatus(app: ApplicationRecordEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateApplication(app.copy(status = newStatus, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteApplication(app: ApplicationRecordEntity) {
        viewModelScope.launch {
            repository.deleteApplication(app)
        }
    }
}

@Composable
fun ApplicationListScreen(
    viewModel: ApplicationViewModel,
    repository: CafeRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val applications by viewModel.filteredApplications.collectAsStateWithLifecycle()
    val filter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = RoyalBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Application")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Status Filter Chips
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyDark)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "All Applications",
                        "PENDING" to "Pending / In-Progress",
                        "SUBMITTED" to "Submitted",
                        "COMPLETED" to "Completed"
                    )

                    filters.forEach { (key, label) ->
                        FilterChip(
                            selected = filter == key,
                            onClick = { viewModel.selectedFilter.value = key },
                            label = {
                                Text(
                                    label,
                                    color = if (filter == key) NavyDark else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = if (filter == key) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            if (applications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Assignment,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No applications found in this category.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Text("+ Register New Application")
                        }
                    }
                }
            } else {
                val dateFormat = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault())
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        top = 12.dp,
                        bottom = 80.dp
                    )
                ) {
                    items(applications, key = { it.id }) { app ->
                        ApplicationCardItem(
                            app = app,
                            formattedDate = dateFormat.format(Date(app.createdAt)),
                            onStatusChange = { newStatus -> viewModel.updateStatus(app, newStatus) },
                            onDelete = { viewModel.deleteApplication(app) },
                            onPrintReceipt = {
                                scope.launch(Dispatchers.Default) {
                                    val settings = repository.getSettingsSnapshot()
                                    val receiptBitmap = PdfGeneratorUtils.createCustomerReceiptBitmap(
                                        settings = settings,
                                        customerName = app.customerName,
                                        customerMobile = app.customerMobile,
                                        serviceName = app.serviceName,
                                        category = app.serviceCategory,
                                        referenceNo = app.referenceNumber,
                                        amountCharged = app.serviceCharge,
                                        amountPaid = app.paidAmount,
                                        paymentStatus = app.paymentStatus,
                                        paymentMethod = app.paymentMethod
                                    )
                                    withContext(Dispatchers.Main) {
                                        PdfGeneratorUtils.printBitmap(context, receiptBitmap, "Receipt_${app.customerName}")
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddApplicationDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { app ->
                viewModel.addApplication(app)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ApplicationCardItem(
    app: ApplicationRecordEntity,
    formattedDate: String,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit,
    onPrintReceipt: () -> Unit
) {
    val statusColor = when (app.status) {
        "COMPLETED" -> Color(0xFF10B981)
        "SUBMITTED" -> Color(0xFF3B82F6)
        "IN_PROGRESS" -> Color(0xFFF59E0B)
        "REJECTED" -> Color(0xFFEF4444)
        else -> Color(0xFF6B7280)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.customerName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = app.customerMobile, style = MaterialTheme.typography.bodySmall)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.16f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = app.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(10.dp)
            ) {
                Column {
                    Text(
                        text = "${app.serviceCategory} • ${app.serviceName}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    if (app.referenceNumber.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Reference / Ack: ${app.referenceNumber}",
                            style = MaterialTheme.typography.bodySmall.copy(color = RoyalBlue, fontWeight = FontWeight.Bold)
                        )
                    }
                    if (app.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Remarks: ${app.notes}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fee: ₹${"%.0f".format(app.serviceCharge)} (Paid: ₹${"%.0f".format(app.paidAmount)})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onPrintReceipt,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Receipt", fontSize = 11.sp)
                    }

                    if (app.status != "COMPLETED") {
                        Button(
                            onClick = { onStatusChange("COMPLETED") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Done", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddApplicationDialog(
    onDismiss: () -> Unit,
    onConfirm: (ApplicationRecordEntity) -> Unit
) {
    var customerName by remember { mutableStateOf("") }
    var customerMobile by remember { mutableStateOf("") }
    var serviceCategory by remember { mutableStateOf("Govt Jobs") }
    var serviceName by remember { mutableStateOf("SSC CGL 2026 Online Form") }
    var referenceNo by remember { mutableStateOf("APP" + System.currentTimeMillis().toString().takeLast(6)) }
    var feeText by remember { mutableStateOf("150") }
    var paidText by remember { mutableStateOf("150") }
    var status by remember { mutableStateOf("SUBMITTED") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Govt Jobs", "Bihar RTPS", "PAN Card", "Aadhaar", "Voter ID", "Scholarship", "Admission", "Photo Print")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Cyber / CSC Application", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("Customer Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customerMobile,
                    onValueChange = { customerMobile = it },
                    label = { Text("Customer Mobile Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text("Category:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        SuggestionChip(
                            onClick = { serviceCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (serviceCategory == cat) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = serviceName,
                    onValueChange = { serviceName = it },
                    label = { Text("Service / Form Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = referenceNo,
                    onValueChange = { referenceNo = it },
                    label = { Text("Reference / Ack / Reg Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = feeText,
                        onValueChange = { feeText = it },
                        label = { Text("Service Fee (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = paidText,
                        onValueChange = { paidText = it },
                        label = { Text("Paid Amount (₹)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Document status") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customerName.isNotBlank() && customerMobile.isNotBlank()) {
                        val fee = feeText.toDoubleOrNull() ?: 150.0
                        val paid = paidText.toDoubleOrNull() ?: 150.0
                        val pStatus = if (paid >= fee) "PAID" else "DUE"
                        onConfirm(
                            ApplicationRecordEntity(
                                customerId = 0,
                                customerName = customerName.trim(),
                                customerMobile = customerMobile.trim(),
                                serviceCategory = serviceCategory,
                                serviceName = serviceName.trim(),
                                referenceNumber = referenceNo.trim(),
                                status = status,
                                serviceCharge = fee,
                                paidAmount = paid,
                                paymentStatus = pStatus,
                                paymentMethod = "CASH",
                                notes = notes.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Register Application")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
