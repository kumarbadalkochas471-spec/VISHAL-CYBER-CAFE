package com.example.ui.accounting

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.CafeRepository
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
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AccountingViewModel(private val repository: CafeRepository) : ViewModel() {
    val selectedFilter = MutableStateFlow("ALL") // "ALL", "INCOME", "EXPENSE"

    val transactions: StateFlow<List<TransactionEntity>> = combine(
        repository.getAllTransactions(),
        selectedFilter
    ) { list, filter ->
        when (filter) {
            "ALL" -> list
            "INCOME" -> list.filter { it.type == "INCOME" }
            "EXPENSE" -> list.filter { it.type == "EXPENSE" }
            else -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalIncome: StateFlow<Double> = repository.getTotalIncome()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = repository.getTotalExpense()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addTransaction(type: String, category: String, amount: Double, desc: String, method: String, customer: String) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
                    type = type,
                    category = category,
                    amount = amount,
                    description = desc,
                    paymentMethod = method,
                    customerName = customer.ifBlank { null },
                    dateMillis = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }
}

@Composable
fun AccountingScreen(
    viewModel: AccountingViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val totalIncome by viewModel.totalIncome.collectAsStateWithLifecycle()
    val totalExpense by viewModel.totalExpense.collectAsStateWithLifecycle()
    val filter by viewModel.selectedFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var dialogInitialType by remember { mutableStateOf("INCOME") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    dialogInitialType = "INCOME"
                    showAddDialog = true
                },
                containerColor = RoyalBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Stats Banner (Income, Expense, Net Profit)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = NavyDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ACCOUNTING & BALANCE SHEET",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CyanGlow,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Income", style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray))
                            Text(
                                "₹${"%.0f".format(totalIncome)}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            )
                        }

                        Column {
                            Text("Total Expenses", style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray))
                            Text(
                                "₹${"%.0f".format(totalExpense)}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEF4444)
                                )
                            )
                        }

                        Column {
                            val net = totalIncome - totalExpense
                            Text("Net Profit", style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray))
                            Text(
                                "₹${"%.0f".format(net)}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (net >= 0) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                dialogInitialType = "INCOME"
                                showAddDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Income", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                dialogInitialType = "EXPENSE"
                                showAddDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.MoneyOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("+ Expense", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    val csvContent = buildString {
                                        append("ID,Type,Category,Amount,Description,PaymentMethod,Customer,Date\n")
                                        transactions.forEach { t ->
                                            val d = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(t.dateMillis))
                                            append("${t.id},${t.type},\"${t.category}\",${t.amount},\"${t.description}\",${t.paymentMethod},\"${t.customerName ?: ""}\",$d\n")
                                        }
                                    }
                                    val file = File(context.cacheDir, "accounting_report_${System.currentTimeMillis()}.csv")
                                    FileOutputStream(file).use { it.write(csvContent.toByteArray()) }
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Exported CSV: ${file.name}", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("ALL" to "All Records", "INCOME" to "Income Only", "EXPENSE" to "Expenses Only").forEach { (key, label) ->
                    FilterChip(
                        selected = filter == key,
                        onClick = { viewModel.selectedFilter.value = key },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            // Transactions Feed
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No transactions logged in this filter.", color = Color.Gray)
                }
            } else {
                val dateFormat = SimpleDateFormat("dd MMM hh:mm a", Locale.getDefault())
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 80.dp
                    )
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionRowItem(
                            tx = tx,
                            formattedDate = dateFormat.format(Date(tx.dateMillis)),
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTransactionDialog(
            initialType = dialogInitialType,
            onDismiss = { showAddDialog = false },
            onConfirm = { type, cat, amt, desc, method, cust ->
                viewModel.addTransaction(type, cat, amt, desc, method, cust)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun TransactionRowItem(
    tx: TransactionEntity,
    formattedDate: String,
    onDelete: () -> Unit
) {
    val isIncome = tx.type == "INCOME"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isIncome) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isIncome) Icons.Default.Payments else Icons.Default.MoneyOff,
                    contentDescription = null,
                    tint = if (isIncome) Color(0xFF059669) else Color(0xFFDC2626),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.description.ifBlank { tx.category },
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${tx.category} • ${tx.paymentMethod} • $formattedDate",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                )
                if (!tx.customerName.isNullOrBlank()) {
                    Text(
                        text = "Customer: ${tx.customerName}",
                        style = MaterialTheme.typography.labelSmall.copy(color = RoyalBlue)
                    )
                }
            }

            Text(
                text = "${if (isIncome) "+" else "-"}₹${"%.0f".format(tx.amount)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) Color(0xFF059669) else Color(0xFFDC2626)
                )
            )

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.LightGray, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun AddTransactionDialog(
    initialType: String,
    onDismiss: () -> Unit,
    onConfirm: (type: String, category: String, amount: Double, desc: String, method: String, customer: String) -> Unit
) {
    var type by remember { mutableStateOf(initialType) }
    var category by remember { mutableStateOf(if (initialType == "INCOME") "Form Application" else "Paper Ream A4") }
    var amountText by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("CASH") }
    var customerName by remember { mutableStateOf("") }

    val incomeCategories = listOf("Form Application", "Photo Print", "Xerox / Print", "Color Print", "Lamination", "Online Service", "Scanning")
    val expenseCategories = listOf("Paper Ream A4", "Ink / Toner", "Electricity", "Internet Wifi", "Shop Rent", "Tea / Snacks", "Maintenance")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (type == "INCOME") "Record Cash/UPI Income" else "Record Shop Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == "INCOME",
                        onClick = {
                            type = "INCOME"
                            category = "Form Application"
                        },
                        label = { Text("Income (+)") }
                    )
                    FilterChip(
                        selected = type == "EXPENSE",
                        onClick = {
                            type = "EXPENSE"
                            category = "Paper Ream A4"
                        },
                        label = { Text("Expense (-)") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Category:", style = MaterialTheme.typography.labelSmall)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val catList = if (type == "INCOME") incomeCategories else expenseCategories
                    catList.forEach { cat ->
                        SuggestionChip(
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (category == cat) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description / Item details") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (type == "INCOME") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Customer Name (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Payment Method:", style = MaterialTheme.typography.labelSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SuggestionChip(
                        onClick = { method = "CASH" },
                        label = { Text("Cash") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (method == "CASH") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    )
                    SuggestionChip(
                        onClick = { method = "UPI" },
                        label = { Text("UPI (GPay/PhonePe)") },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = if (method == "UPI") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onConfirm(type, category, amt, desc, method, customerName)
                    }
                }
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
