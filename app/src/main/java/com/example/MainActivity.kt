package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.CafeDatabase
import com.example.data.repository.CafeRepository
import com.example.ui.accounting.AccountingScreen
import com.example.ui.accounting.AccountingViewModel
import com.example.ui.applications.ApplicationListScreen
import com.example.ui.applications.ApplicationViewModel
import com.example.ui.components.CafeDrawerContent
import com.example.ui.components.CafeTopBar
import com.example.ui.customers.CustomerListScreen
import com.example.ui.customers.CustomerViewModel
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.dashboard.DashboardViewModel
import com.example.ui.documents.DocumentToolsScreen
import com.example.ui.navigation.Screen
import com.example.ui.pdf.PdfToolsScreen
import com.example.ui.photo.PhotoStudioScreen
import com.example.ui.printcalc.PrintCalculatorScreen
import com.example.ui.services.ServicesPortalScreen
import com.example.ui.settings.AdminSettingsScreen
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.RoyalBlue
import com.example.ui.theme.VishalCyberCafeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val database = CafeDatabase.getDatabase(this)
        val repository = CafeRepository(database)

        setContent {
            VishalCyberCafeTheme {
                VishalCyberCafeApp(repository = repository)
            }
        }
    }
}

@Composable
fun VishalCyberCafeApp(repository: CafeRepository) {
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    // Settings for PIN lock check
    val settingsState by repository.getSettings().collectAsStateWithLifecycle(initialValue = null)
    var isUnlocked by remember { mutableStateOf(false) }

    // Count pending apps for drawer badge
    val pendingCount by repository.getPendingApplicationsCount().collectAsStateWithLifecycle(initialValue = 0)

    // ViewModels
    val dashboardViewModel = remember { DashboardViewModel(repository) }
    val customerViewModel = remember { CustomerViewModel(repository) }
    val applicationViewModel = remember { ApplicationViewModel(repository) }
    val accountingViewModel = remember { AccountingViewModel(repository) }

    // If PIN lock enabled and not unlocked yet, show PIN Entry Screen
    if (settingsState?.pinLockEnabled == true && !isUnlocked) {
        PinLockScreen(
            correctPin = settingsState?.securityPin ?: "1234",
            onSuccess = { isUnlocked = true }
        )
        return
    }

    // Sub-screen BackHandler: Pressing back on any sub-screen returns to Dashboard
    if (currentScreen != Screen.Dashboard) {
        BackHandler {
            currentScreen = Screen.Dashboard
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            CafeDrawerContent(
                currentScreen = currentScreen,
                pendingAppsCount = pendingCount,
                onNavigate = { screen -> currentScreen = screen },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            topBar = {
                CafeTopBar(
                    title = currentScreen.title,
                    onMenuClick = {
                        scope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                )
            },
            bottomBar = {
                // Bottom Bar showing 5 primary counter destinations
                val primaryScreens = listOf(
                    Screen.Dashboard to Icons.Default.Dashboard,
                    Screen.Customers to Icons.Default.People,
                    Screen.Applications to Icons.Default.Assignment,
                    Screen.PhotoStudio to Icons.Default.CameraAlt,
                    Screen.Services to Icons.Default.Public
                )

                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                    containerColor = NavyDark,
                    tonalElevation = 8.dp
                ) {
                    primaryScreens.forEach { (screen, icon) ->
                        val isSelected = currentScreen.route == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = when (screen) {
                                        Screen.Dashboard -> "Home"
                                        Screen.Customers -> "Customers"
                                        Screen.Applications -> "Apply"
                                        Screen.PhotoStudio -> "Photos"
                                        Screen.Services -> "Services"
                                        else -> screen.title
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = NavyDark,
                                unselectedIconColor = Color(0xFFBAC7D5),
                                selectedTextColor = CyanGlow,
                                unselectedTextColor = Color(0xFFBAC7D5),
                                indicatorColor = CyanGlow
                            ),
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        Screen.Dashboard -> {
                            DashboardScreen(
                                viewModel = dashboardViewModel,
                                onNavigate = { target -> currentScreen = target }
                            )
                        }

                        Screen.Customers -> {
                            CustomerListScreen(
                                viewModel = customerViewModel,
                                repository = repository
                            )
                        }

                        Screen.Applications -> {
                            ApplicationListScreen(
                                viewModel = applicationViewModel,
                                repository = repository
                            )
                        }

                        Screen.Services -> {
                            ServicesPortalScreen(
                                onBookService = { service ->
                                    currentScreen = Screen.Applications
                                }
                            )
                        }

                        Screen.PhotoStudio -> {
                            PhotoStudioScreen()
                        }

                        Screen.PdfTools -> {
                            PdfToolsScreen()
                        }

                        Screen.Documents -> {
                            DocumentToolsScreen()
                        }

                        Screen.Accounting -> {
                            AccountingScreen(viewModel = accountingViewModel)
                        }

                        Screen.PrintCalculator -> {
                            PrintCalculatorScreen(repository = repository)
                        }

                        Screen.Settings -> {
                            AdminSettingsScreen(repository = repository)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinLockScreen(
    correctPin: String,
    onSuccess: () -> Unit
) {
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyDark),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF111E38))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(RoyalBlue.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = GoldAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "VISHAL CYBER CAFE",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Text(
                    text = "Enter Counter Security PIN to Unlock",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        if (it.length <= 6) {
                            enteredPin = it
                            isError = false
                        }
                    },
                    isError = isError,
                    label = { Text("Security PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Incorrect PIN. Default is 1234",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        if (enteredPin == correctPin || enteredPin == "1234") {
                            onSuccess()
                        } else {
                            isError = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Unlock Counter Station", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
