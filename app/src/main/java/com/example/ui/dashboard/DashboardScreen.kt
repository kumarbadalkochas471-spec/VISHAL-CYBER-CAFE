package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entity.ApplicationRecordEntity
import com.example.data.local.entity.TransactionEntity
import com.example.ui.components.StatCard
import com.example.ui.navigation.Screen
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusPending
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom Status Colors
private val ColorIncomeGrad = listOf(Color(0xFF0F9D58), Color(0xFF0B6623))
private val ColorExpenseGrad = listOf(Color(0xFFE53935), Color(0xFFB71C1C))
private val ColorNetProfitGrad = listOf(Color(0xFF1E56A0), Color(0xFF0B192C))
private val ColorMonthGrad = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
private val ColorCustomersGrad = listOf(Color(0xFF4F46E5), Color(0xFF312E81))
private val ColorAppsGrad = listOf(Color(0xFF8B5CF6), Color(0xFF5B21B6))

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showExpenseDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Hero Banner Card with CSC Branding
        item {
            HeroBannerSection(
                onNavigate = onNavigate
            )
        }

        // Quick Action Bar
        item {
            QuickActionsBar(
                onNewCustomer = { onNavigate(Screen.Customers) },
                onNewApplication = { onNavigate(Screen.Applications) },
                onPhotoStudio = { onNavigate(Screen.PhotoStudio) },
                onPdfTools = { onNavigate(Screen.PdfTools) },
                onAddExpense = { showExpenseDialog = true }
            )
        }

        // Key Performance Indicators (6 Cards in 3 Rows)
        item {
            Text(
                text = "TODAY'S OPERATIONS OVERVIEW",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Today's Income",
                    value = "₹${"%.0f".format(state.todayIncome)}",
                    subtitle = "All cash & UPI payments",
                    icon = Icons.Default.Payments,
                    iconColor = Color(0xFF86EFAC),
                    gradientColors = ColorIncomeGrad,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Today's Expenses",
                    value = "₹${"%.0f".format(state.todayExpense)}",
                    subtitle = "Paper, ink & shop costs",
                    icon = Icons.Default.MoneyOff,
                    iconColor = Color(0xFFFCA5A5),
                    gradientColors = ColorExpenseGrad,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Today's Net Profit",
                    value = "₹${"%.0f".format(state.netProfit)}",
                    subtitle = "Income minus expenses",
                    icon = Icons.Default.TrendingUp,
                    iconColor = CyanGlow,
                    gradientColors = ColorNetProfitGrad,
                    modifier = Modifier.weight(1f)
                )

                StatCard(
                    title = "Monthly Income",
                    value = "₹${"%.0f".format(state.monthIncome)}",
                    subtitle = "Total revenue this month",
                    icon = Icons.Default.Assessment,
                    iconColor = Color(0xFFFEF08A),
                    gradientColors = ColorMonthGrad,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Customers",
                    value = "${state.totalCustomers}",
                    subtitle = "Registered accounts",
                    icon = Icons.Default.People,
                    iconColor = Color(0xFFC7D2FE),
                    gradientColors = ColorCustomersGrad,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Customers) }
                )

                StatCard(
                    title = "Applications",
                    value = "${state.totalCompletedApps}/${state.totalCompletedApps + state.totalPendingApps}",
                    subtitle = "${state.totalPendingApps} pending approval",
                    icon = Icons.Default.Assignment,
                    iconColor = Color(0xFFDDD6FE),
                    gradientColors = ColorAppsGrad,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigate(Screen.Applications) }
                )
            }
        }

        // Live Income vs Expense Visual Bar
        item {
            IncomeExpenseVisualCard(
                income = state.todayIncome,
                expense = state.todayExpense,
                netProfit = state.netProfit
            )
        }

        // Frequently Used Tools
        item {
            FrequentlyUsedToolsSection(onNavigate = onNavigate)
        }

        // Recent Applications Section
        item {
            RecentApplicationsSection(
                applications = state.recentApplications,
                onViewAll = { onNavigate(Screen.Applications) },
                onStatusChange = { app, newStatus ->
                    viewModel.updateApplicationStatus(app, newStatus)
                }
            )
        }

        // Recent Transactions Feed
        item {
            RecentTransactionsSection(
                transactions = state.recentTransactions,
                onViewAll = { onNavigate(Screen.Accounting) }
            )
        }
    }

    if (showExpenseDialog) {
        QuickAddExpenseDialog(
            onDismiss = { showExpenseDialog = false },
            onConfirm = { cat, amt, desc, method ->
                viewModel.quickAddExpense(cat, amt, desc, method)
                showExpenseDialog = false
            }
        )
    }
}

@Composable
private fun HeroBannerSection(
    onNavigate: (Screen) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = NavyDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_cafe_banner),
                    contentDescription = "CSC Workstation Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, NavyDark.copy(alpha = 0.85f), NavyDark)
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CSC ID: CSC-BR-821112",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "VISHAL CYBER CAFE & CSC CENTRE",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Purana Bazar, Ward No. 16, Kochas, Rohtas, Bihar – 821112",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFBAC7D5)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onNavigate(Screen.Services) },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Online Services", fontSize = 12.sp, color = Color.White)
                    }

                    Button(
                        onClick = { onNavigate(Screen.PhotoStudio) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF233863)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = GoldAccent
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Photo Studio", fontSize = 12.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionsBar(
    onNewCustomer: () -> Unit,
    onNewApplication: () -> Unit,
    onPhotoStudio: () -> Unit,
    onPdfTools: () -> Unit,
    onAddExpense: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionButton(
                    label = "+ Customer",
                    icon = Icons.Default.People,
                    color = Color(0xFF3B82F6),
                    onClick = onNewCustomer
                )
                QuickActionButton(
                    label = "+ Apply",
                    icon = Icons.Default.Assignment,
                    color = Color(0xFF8B5CF6),
                    onClick = onNewApplication
                )
                QuickActionButton(
                    label = "Passport",
                    icon = Icons.Default.CameraAlt,
                    color = Color(0xFFF59E0B),
                    onClick = onPhotoStudio
                )
                QuickActionButton(
                    label = "PDF/Print",
                    icon = Icons.Default.Print,
                    color = Color(0xFF10B981),
                    onClick = onPdfTools
                )
                QuickActionButton(
                    label = "+ Expense",
                    icon = Icons.Default.MoneyOff,
                    color = Color(0xFFEF4444),
                    onClick = onAddExpense
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun IncomeExpenseVisualCard(
    income: Double,
    expense: Double,
    netProfit: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Profit & Loss Analysis",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (netProfit >= 0) "Net Profit: ₹${"%.0f".format(netProfit)}" else "Net Loss: ₹${"%.0f".format(-netProfit)}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (netProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val total = (income + expense).coerceAtLeast(1.0)
            val incomeRatio = (income / total).toFloat().coerceIn(0.05f, 0.95f)

            // Visual comparative progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(Color(0xFFEF4444))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(incomeRatio)
                        .height(18.dp)
                        .background(Color(0xFF10B981))
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Income: ₹${"%.0f".format(income)} (${(incomeRatio * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Expense: ₹${"%.0f".format(expense)} (${((1f - incomeRatio) * 100).toInt()}%)",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }
    }
}

@Composable
private fun FrequentlyUsedToolsSection(
    onNavigate: (Screen) -> Unit
) {
    Column(modifier = Modifier.padding(top = 10.dp)) {
        Text(
            text = "FREQUENTLY USED CYBER TOOLS",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            ),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ToolChipCard(
                    title = "AI BG Remover",
                    desc = "White, Blue & Transparent",
                    icon = Icons.Default.CameraAlt,
                    iconBg = Color(0xFF3B82F6),
                    onClick = { onNavigate(Screen.PhotoStudio) }
                )
            }
            item {
                ToolChipCard(
                    title = "16 Photo A4 Sheet",
                    desc = "With Name & Date imprint",
                    icon = Icons.Default.Print,
                    iconBg = Color(0xFFF59E0B),
                    onClick = { onNavigate(Screen.PhotoStudio) }
                )
            }
            item {
                ToolChipCard(
                    title = "Signature Resizer",
                    desc = "10-20KB SSC/UPSC crisp",
                    icon = Icons.Default.Description,
                    iconBg = Color(0xFF10B981),
                    onClick = { onNavigate(Screen.PhotoStudio) }
                )
            }
            item {
                ToolChipCard(
                    title = "Images to PDF",
                    desc = "Merge with Watermark",
                    icon = Icons.Default.PictureAsPdf,
                    iconBg = Color(0xFFEF4444),
                    onClick = { onNavigate(Screen.PdfTools) }
                )
            }
            item {
                ToolChipCard(
                    title = "Resume / Biodata",
                    desc = "Print-ready A4 maker",
                    icon = Icons.Default.Description,
                    iconBg = Color(0xFF8B5CF6),
                    onClick = { onNavigate(Screen.Documents) }
                )
            }
            item {
                ToolChipCard(
                    title = "Print Calculator",
                    desc = "B&W, Color, Lamination",
                    icon = Icons.Default.Calculate,
                    iconBg = Color(0xFF00D2D3),
                    onClick = { onNavigate(Screen.PrintCalculator) }
                )
            }
        }
    }
}

@Composable
private fun ToolChipCard(
    title: String,
    desc: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBg.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconBg, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp))
                Text(text = desc, style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun RecentApplicationsSection(
    applications: List<ApplicationRecordEntity>,
    onViewAll: () -> Unit,
    onStatusChange: (ApplicationRecordEntity, String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Applications",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onViewAll) {
                    Text("View All (${applications.size})", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (applications.isEmpty()) {
                Text(
                    text = "No applications registered yet. Click '+ Apply' above to create one.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                applications.forEach { app ->
                    RecentAppItem(
                        app = app,
                        onStatusChange = { newStatus -> onStatusChange(app, newStatus) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun RecentAppItem(
    app: ApplicationRecordEntity,
    onStatusChange: (String) -> Unit
) {
    val statusColor = when (app.status) {
        "COMPLETED" -> Color(0xFF10B981)
        "SUBMITTED" -> Color(0xFF3B82F6)
        "IN_PROGRESS" -> Color(0xFFF59E0B)
        "REJECTED" -> Color(0xFFEF4444)
        else -> Color(0xFF6B7280)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = app.customerName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                // Status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = app.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${app.serviceCategory} • ${app.serviceName}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            if (app.referenceNumber.isNotBlank()) {
                Text(
                    text = "Ref: ${app.referenceNumber}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fee: ₹${"%.0f".format(app.serviceCharge)} (Paid: ₹${"%.0f".format(app.paidAmount)})",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (app.status != "COMPLETED") {
                        SuggestionChip(
                            onClick = { onStatusChange("COMPLETED") },
                            label = { Text("Complete", fontSize = 10.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFFD1FAE5))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionsSection(
    transactions: List<TransactionEntity>,
    onViewAll: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onViewAll) {
                    Text("Accounting", color = MaterialTheme.colorScheme.primary)
                }
            }

            if (transactions.isEmpty()) {
                Text(
                    text = "No transactions recorded yet.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                transactions.take(5).forEach { tx ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isIncome = tx.type == "INCOME"
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isIncome) Color(0xFFD1FAE5) else Color(0xFFFEE2E2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isIncome) Icons.Default.Payments else Icons.Default.MoneyOff,
                                contentDescription = null,
                                tint = if (isIncome) Color(0xFF059669) else Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = tx.description.ifBlank { tx.category },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${tx.category} • ${tx.paymentMethod} • ${timeFormat.format(Date(tx.dateMillis))}",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            )
                        }

                        Text(
                            text = "${if (isIncome) "+" else "-"}₹${"%.0f".format(tx.amount)}",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isIncome) Color(0xFF059669) else Color(0xFFDC2626)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAddExpenseDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, amount: Double, desc: String, method: String) -> Unit
) {
    var category by remember { mutableStateOf("Paper Ream A4") }
    var amountText by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("CASH") }

    val categories = listOf(
        "Paper Ream A4",
        "Ink / Toner",
        "Electricity",
        "Internet Wifi",
        "Shop Rent",
        "Tea / Snacks",
        "Machine Maintenance",
        "Other Expense"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Daily Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Category:", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(categories) { cat ->
                        SuggestionChip(
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (category == cat) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description / Remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

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
                        label = { Text("UPI (PhonePe/GPay)") },
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
                        onConfirm(category, amt, desc, method)
                    }
                }
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
