package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.navigation.Screen
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.RoyalBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeTopBar(
    title: String,
    onMenuClick: () -> Unit,
    onQuickAction: (() -> Unit)? = null,
    quickActionIcon: ImageVector? = null
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "VISHAL CYBER CAFE & CSC • KOCHAS, BIHAR",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyanGlow,
                        letterSpacing = 0.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("nav_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open Navigation Menu",
                    tint = Color.White
                )
            }
        },
        actions = {
            if (onQuickAction != null && quickActionIcon != null) {
                IconButton(
                    onClick = onQuickAction,
                    modifier = Modifier.testTag("topbar_quick_action")
                ) {
                    Icon(
                        imageVector = quickActionIcon,
                        contentDescription = "Quick Action",
                        tint = GoldAccent
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = NavyDark,
            titleContentColor = Color.White
        )
    )
}

data class DrawerItem(
    val screen: Screen,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun CafeDrawerContent(
    currentScreen: Screen,
    pendingAppsCount: Int,
    onNavigate: (Screen) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val items = listOf(
        DrawerItem(Screen.Dashboard, Icons.Default.Dashboard),
        DrawerItem(Screen.Customers, Icons.Default.People),
        DrawerItem(Screen.Applications, Icons.Default.Description, pendingAppsCount),
        DrawerItem(Screen.Services, Icons.Default.Public),
        DrawerItem(Screen.PhotoStudio, Icons.Default.CameraAlt),
        DrawerItem(Screen.PdfTools, Icons.Default.PictureAsPdf),
        DrawerItem(Screen.Documents, Icons.Default.Description),
        DrawerItem(Screen.Accounting, Icons.Default.Assessment),
        DrawerItem(Screen.PrintCalculator, Icons.Default.Calculate),
        DrawerItem(Screen.Settings, Icons.Default.Settings)
    )

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp),
        color = NavyDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with Cafe Logo & Address
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(NavySurface, NavyDark)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_cafe_logo),
                            contentDescription = "Vishal Cyber Cafe Logo",
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "VISHAL CYBER CAFE",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "CSC DIGITAL SEVA KENDRA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = GoldAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Purana Bazar, Ward No. 16, Kochas, Rohtas, Bihar – 821112",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFBAC7D5),
                            lineHeight = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Counter Station: ACTIVE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Divider(color = Color(0xFF1F3152))

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Items
            items.forEach { item ->
                val isSelected = currentScreen.route == item.screen.route
                val bgColor = if (isSelected) RoyalBlue.copy(alpha = 0.28f) else Color.Transparent
                val contentColor = if (isSelected) CyanGlow else Color(0xFFD3E0EA)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable {
                            onNavigate(item.screen)
                            onCloseDrawer()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.screen.title,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = item.screen.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = contentColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    if (item.badgeCount > 0) {
                        Badge(
                            containerColor = GoldAccent,
                            contentColor = NavyDark
                        ) {
                            Text(
                                text = item.badgeCount.toString(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
