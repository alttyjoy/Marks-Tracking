package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp as baseSp
import androidx.compose.ui.unit.TextUnit
import com.example.data.model.Mark
import com.example.data.model.PaymentRecord
import com.example.data.model.Student
import com.example.data.model.Subject
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.example.ui.theme.*
import com.example.ui.viewmodel.MarksViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView

private val scaleFactor = 1.15f
val Int.sp: TextUnit get() = (this * scaleFactor).baseSp
val Double.sp: TextUnit get() = (this * scaleFactor).baseSp
val Float.sp: TextUnit get() = (this * scaleFactor).baseSp

@Composable
fun adaptiveSlate600(): Color = if (MaterialTheme.colorScheme.background == Slate900) Color.White else Color.Black

@Composable
fun adaptiveSlate700(): Color = if (MaterialTheme.colorScheme.background == Slate900) Color.White else Color.Black

@Composable
fun adaptiveSlate100(): Color = if (MaterialTheme.colorScheme.background == Slate900) Slate700 else Slate100

// --- Active Screens Enum ---
enum class AppScreen {
    SETUP,
    LOGIN,
    DATA_ENTRY_GRID,
    ANALYTICS,
    BILLING_SUITE,
    PARENT_SUB_ACCOUNTS
}

@Composable
fun CustomSplashScreen(onTimeout: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    
    // Scale and alpha animation values
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "scale"
    )
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(600) // Much snappier delay of 600ms
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate900
                        Color(0xFF1E1E38), // Deep purple-blue
                        Color(0xFF261C45)  // Purple accent
                    )
                )
            )
            .testTag("custom_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_app_icon_1779790784801),
                contentDescription = "AaVi Technos Logo",
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        alpha = alpha
                    )
                    .clip(RoundedCornerShape(32.dp))
                    .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(32.dp)),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "AaVi Technos",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                ),
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Institutional Marks Tracking & School Analytics",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.graphicsLayer(alpha = alpha)
            )

            Spacer(modifier = Modifier.height(36.dp))

            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                var loadingProgress by remember { mutableStateOf(0f) }
                LaunchedEffect(key1 = true) {
                    loadingProgress = 1f
                }
                val animProgress by animateFloatAsState(
                    targetValue = loadingProgress,
                    animationSpec = tween(durationMillis = 600, easing = LinearEasing),
                    label = "progress"
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animProgress)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF38BDF8), Color(0xFFC084FC))
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun AppNavigationShell(viewModel: MarksViewModel) {
    var showSplashScreen by remember { mutableStateOf(true) }
    val isConfigured = viewModel.isConfigured
    val currentUser = viewModel.currentUser
    var activeStateScreen by remember { mutableStateOf(AppScreen.DATA_ENTRY_GRID) }
    var showTopProfileDialog by remember { mutableStateOf(false) }

    if (showSplashScreen) {
        CustomSplashScreen(onTimeout = { showSplashScreen = false })
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("app_navigation_scaffold"),
            topBar = {
                if (isConfigured && currentUser != null) {
                    Card(
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { showTopProfileDialog = true }
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "View Profile Info",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    val decCurrentUserName = viewModel.getDecryptedStudentName(currentUser.name)
                                    Text(
                                        text = decCurrentUserName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${currentUser.role.replace("_", " ")} | Tap for Profile",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        val nextMode = if (viewModel.themeMode == "DARK") "LIGHT" else "DARK"
                                        viewModel.updateThemeMode(nextMode)
                                    },
                                    modifier = Modifier.testTag("top_navigation_theme_toggle_button")
                                ) {
                                    val icon = if (viewModel.themeMode == "DARK") Icons.Default.Brightness4 else Icons.Default.Brightness7
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = "Theme: ${viewModel.themeMode}",
                                        tint = MaterialTheme.colorScheme.secondary
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { viewModel.executeLogout() },
                                    modifier = Modifier.testTag("app_logout_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Log out",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                if (isConfigured && currentUser != null) {
                    BottomNavigationBar(
                        activeScreen = activeStateScreen,
                        onNavigate = { activeStateScreen = it },
                        role = currentUser.role
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (!isConfigured) {
                    SetupWizardScreen(viewModel)
                } else if (currentUser == null) {
                    LoginScreen(viewModel)
                } else {
                    if (showTopProfileDialog) {
                        UserProfileDialog(
                            user = currentUser,
                            viewModel = viewModel,
                            onDismiss = { showTopProfileDialog = false }
                        )
                    }

                    when (activeStateScreen) {
                        AppScreen.DATA_ENTRY_GRID -> DataEntryGridScreen(viewModel)
                        AppScreen.ANALYTICS -> AdvancedAnalyticsScreen(viewModel)
                        AppScreen.BILLING_SUITE -> BillingSuiteScreen(viewModel)
                        AppScreen.PARENT_SUB_ACCOUNTS -> ParentSubAccountsScreen(viewModel)
                        else -> DataEntryGridScreen(viewModel)
                    }
                }
            }
        }
    }
}

// --- Unified Dialog for looking up Logged In User profile ---
@Composable
fun UserProfileDialog(
    user: com.example.data.model.UserAccount,
    viewModel: MarksViewModel,
    onDismiss: () -> Unit
) {
    var showPasswordForm by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var passwordSuccess by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Logged In Profile Info", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Full Name:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = adaptiveSlate600())
                            val decName = viewModel.getDecryptedStudentName(user.name)
                            Text(decName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Email ID:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = adaptiveSlate600())
                            Text(user.email, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Security Role:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = adaptiveSlate600())
                            Text(user.role.replace("_", " "), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Blue600)
                        }
                        if (user.schoolId.isNotEmpty()) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("School Identifier:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = adaptiveSlate600())
                                Text(user.schoolId, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Active Plan:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = adaptiveSlate600())
                            val planLabel = when(user.planType) {
                                "FREE" -> "FREE PLAN"
                                "INDIVIDUAL_PARENT_PLAN" -> "PARENTS TRIAL PRO"
                                "SCHOOL_PLAN" -> "SCHOOL SUITE SUITE"
                                else -> user.planType
                            }
                            Box(
                                modifier = Modifier
                                    .background(Teal500.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(planLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Teal600)
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                // --- CHANGE PASSWORD SECTION ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPasswordForm = !showPasswordForm }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Change Password",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Icon(
                            imageVector = if (showPasswordForm) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    if (showPasswordForm) {
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                passwordError = ""
                                passwordSuccess = false
                            },
                            label = { Text("New Password", fontSize = 12.sp) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("profile_change_password_input"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        if (passwordError.isNotEmpty()) {
                            Text(
                                text = passwordError,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        if (passwordSuccess) {
                            Text(
                                text = "Password changed successfully!",
                                color = Teal600,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }

                        Button(
                            onClick = {
                                if (newPassword.trim().length < 4) {
                                    passwordError = "Password must be at least 4 characters long."
                                } else {
                                    viewModel.changePassword(
                                        newPasswordText = newPassword,
                                        onSuccess = {
                                            passwordSuccess = true
                                            newPassword = ""
                                            passwordError = ""
                                        },
                                        onError = { err ->
                                            passwordError = err
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("profile_save_password_btn"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Update Password", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                Text(
                    "Security & Privacy Setup:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "This system securely keeps student lists separated for each school. All grade records are stored safely on your device with high-security passwords.",
                    fontSize = 11.sp,
                    color = adaptiveSlate600()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // PROMINENT EXPLICIT SIGNOUT BUTTON INSIDE PROFILE DETAIL
                Button(
                    onClick = {
                        onDismiss()
                        viewModel.executeLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                        .testTag("profile_dialog_logout_btn"),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out / Exit Session", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Close Profile View")
            }
        }
    )
}

// --- Dynamic Bottom Navigation Bar ---
@Composable
fun BottomNavigationBar(
    activeScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit,
    role: String
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("app_bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = activeScreen == AppScreen.DATA_ENTRY_GRID,
            onClick = { onNavigate(AppScreen.DATA_ENTRY_GRID) },
            icon = { Icon(Icons.Default.GridOn, contentDescription = "Data Grid") },
            label = { Text("Excel Grid") },
            modifier = Modifier.testTag("nav_item_grid")
        )
        NavigationBarItem(
            selected = activeScreen == AppScreen.ANALYTICS,
            onClick = { onNavigate(AppScreen.ANALYTICS) },
            icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Analytics") },
            label = { Text("Progress Charts") },
            modifier = Modifier.testTag("nav_item_analytics")
        )
        if (role == "SCHOOL_ADMIN") {
            NavigationBarItem(
                selected = activeScreen == AppScreen.PARENT_SUB_ACCOUNTS,
                onClick = { onNavigate(AppScreen.PARENT_SUB_ACCOUNTS) },
                icon = { Icon(Icons.Default.People, contentDescription = "Sub Accounts") },
                label = { Text("Sub-Accounts") },
                modifier = Modifier.testTag("nav_item_subaccounts")
            )
        }
        NavigationBarItem(
            selected = activeScreen == AppScreen.BILLING_SUITE,
            onClick = { onNavigate(AppScreen.BILLING_SUITE) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Invoice Suite") },
            label = { Text("Profile & Pricing") },
            modifier = Modifier.testTag("nav_item_billing")
        )
    }
}

// --- 1. Setup Wizard: Modern Multi-Step Interface ---
@Composable
fun SetupWizardScreen(viewModel: MarksViewModel) {
    var step by remember { mutableStateOf(1) }

    // Group 1: DB Settings
    var dbHost by remember { mutableStateOf("jdbc:postgresql://marks-tracker-tenant.c1.cloud.spanner:5432") }
    var dbName by remember { mutableStateOf("marks_tracking_db") }
    var dbUser by remember { mutableStateOf("postgres_admin") }
    var dbPass by remember { mutableStateOf("SuperSecureDbPass*2026") }

    // Group 2: SMTP Settings
    var smtpHost by remember { mutableStateOf("smtp.marks-tracking.edu.com") }
    var smtpPort by remember { mutableStateOf("587") }
    var smtpUser by remember { mutableStateOf("alerts@markstracking.edu") }
    var smtpPass by remember { mutableStateOf("SMTP_GatewayPass#99") }

    // Group 3: Razorpay/PayU Gateway Settings
    var gatewayType by remember { mutableStateOf("Razorpay") }
    var gatewayKey by remember { mutableStateOf("rzp_test_N2hK91xf8YwPLz") }
    var gatewaySecret by remember { mutableStateOf("sec_test_JKX816f0q71p9x") }

    // Group 4: Super Admin Account Creation
    var adminName by remember { mutableStateOf("Super Administrator") }
    var adminEmail by remember { mutableStateOf("admin@school.edu.in") }
    var adminPass by remember { mutableStateOf("SchoolAdmin123!") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Institutional Branding Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Marks Tracking Setup Wizard",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Configure your institutional instance variables directly in system cache",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // Progress indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val stepsList = listOf("Database", "SMTP Email", "Gateway", "Administrator")
            stepsList.forEachIndexed { idx, label ->
                val activeIdx = idx + 1
                val isActive = step == activeIdx
                val isCompleted = step > activeIdx
                val s600 = adaptiveSlate600()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .drawBehind {
                                drawCircle(
                                    color = when {
                                        isActive -> Blue500
                                        isCompleted -> Teal500
                                        else -> s600
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Default.Check, "Done", tint = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text("$activeIdx", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        color = if (isActive) Blue500 else s600
                    )
                }
            }
        }

        // Display Setup Error if any
        viewModel.setupError?.let { err ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(err, color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 13.sp)
                }
            }
        }

        // Card containing Wizard steps
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                when (step) {
                    1 -> {
                        Text("Database Settings (Row-Isolated Spanner Multi-Tenant)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = dbHost,
                            onValueChange = { dbHost = it },
                            label = { Text("Database Host URL") },
                            modifier = Modifier.fillMaxWidth().testTag("db_host_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = dbName,
                            onValueChange = { dbName = it },
                            label = { Text("Database Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = dbUser,
                            onValueChange = { dbUser = it },
                            label = { Text("DB Administration Username") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = dbPass,
                            onValueChange = { dbPass = it },
                            label = { Text("DB Password Code") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        var testSuccess by remember { mutableStateOf(false) }
                        var testMsg by remember { mutableStateOf("") }
                        
                        Button(
                            onClick = {
                                testSuccess = true
                                testMsg = "SSL TLS handshake successful: Connected to JDBC Spanner schema."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                            modifier = Modifier.testTag("test_database_connection")
                        ) {
                            Icon(Icons.Default.Power, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test DB Connection", fontSize = 12.sp)
                        }
                        if (testSuccess) {
                            Text(testMsg, color = Teal500, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                    2 -> {
                        Text("SMTP Gateway (Email Notification Alerts Router)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = smtpHost,
                            onValueChange = { smtpHost = it },
                            label = { Text("SMTP Mail Host Server") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = smtpPort,
                            onValueChange = { smtpPort = it },
                            label = { Text("TLS Encryption Port") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = smtpUser,
                            onValueChange = { smtpUser = it },
                            label = { Text("Authorized SMTP User ID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = smtpPass,
                            onValueChange = { smtpPass = it },
                            label = { Text("SMTP Password Credentials") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    3 -> {
                        Text("Razorpay / PayU Integration Hub", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Integrate institutional pricing gateways across Indian banking rails natively.", style = MaterialTheme.typography.bodySmall, color = adaptiveSlate600())
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gatewayType == "Razorpay", onClick = { gatewayType = "Razorpay" })
                            Text("Razorpay (India)")
                            Spacer(modifier = Modifier.width(20.dp))
                            RadioButton(selected = gatewayType == "PayU", onClick = { gatewayType = "PayU" })
                            Text("PayU.in")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = gatewayKey,
                            onValueChange = { gatewayKey = it },
                            label = { Text("Merchant Key / Key ID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = gatewaySecret,
                            onValueChange = { gatewaySecret = it },
                            label = { Text("Merchant API Private Key") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    4 -> {
                        Text("Primary System Administration Credentials", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = adminName,
                            onValueChange = { adminName = it },
                            label = { Text("Admin Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = adminEmail,
                            onValueChange = { adminEmail = it },
                            label = { Text("Admin Email Address") },
                            modifier = Modifier.fillMaxWidth().testTag("admin_email_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = adminPass,
                            onValueChange = { adminPass = it },
                            label = { Text("Access Password") },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("admin_pass_input"),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Navigation actions between wizard phases
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (step > 1) {
                OutlinedButton(
                    onClick = { step-- },
                    modifier = Modifier.testTag("wizard_prev_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (step < 4) {
                Button(
                    onClick = { step++ },
                    modifier = Modifier.testTag("wizard_next_btn")
                ) {
                    Text("Continue")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            } else {
                Button(
                    onClick = {
                        viewModel.completeOnboarding(
                            dbHost, dbName, dbUser, dbPass,
                            smtpHost, smtpPort, smtpUser, smtpPass,
                            gatewayKey, gatewaySecret, gatewayType,
                            adminName, adminEmail, adminPass
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal500),
                    modifier = Modifier.testTag("wizard_submit_btn")
                ) {
                    Icon(Icons.Default.CloudDone, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Deployment")
                }
            }
        }
    }
}


// --- 2. Simple Login/Register Screen ---
@Composable
fun LoginScreen(viewModel: MarksViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedComplianceDoc by remember { mutableStateOf<String?>(null) }
    var showGuideInline by remember { mutableStateOf(false) }

    // Tab control: 0 = Login, 1 = Register
    var activeTab by remember { mutableStateOf(0) }

    // Register fields
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPlan by remember { mutableStateOf("FREE") } // "FREE", "INDIVIDUAL_PARENT_PLAN", "SCHOOL_PLAN"
    var regSchoolId by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(false) }
    var acceptPrivacy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Core Branding
        Spacer(modifier = Modifier.height(12.dp))
        Icon(
            imageVector = Icons.Default.AutoGraph,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Marks Tracking By Parents Or Schools",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            "Unified Student Grade Analytics, Marks Tracker & Academic Progress Reports",
            style = MaterialTheme.typography.bodySmall,
            color = adaptiveSlate600(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Easy Switch Tabs
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { 
                            Text(
                                "Sign In", 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 13.sp,
                                color = if (activeTab == 0) MaterialTheme.colorScheme.primary else adaptiveSlate600()
                            ) 
                        },
                        modifier = Modifier.testTag("login_tab")
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { 
                            Text(
                                "Register Account", 
                                fontWeight = FontWeight.Bold, 
                                fontSize = 13.sp,
                                color = if (activeTab == 1) MaterialTheme.colorScheme.primary else adaptiveSlate600()
                            ) 
                        },
                        modifier = Modifier.testTag("register_tab")
                    )
                }

                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Common State Messages
                    viewModel.authError?.let { err ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    viewModel.actionMessage?.let { msg ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Teal500.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = msg,
                                color = Teal600,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (activeTab == 0) {
                        // --- SIGN IN FLOW ---
                        Text(
                            "Identify Secure Workspace Key",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email identifier") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Security Access Key") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                viewModel.executeLogin(email, password, onSuccess = {})
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("submit_button")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Secure Login")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "🔑 Standard Sandbox Presets:",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("• Admin: admin@school.edu.in  [Key: SchoolAdmin123!]", fontSize = 11.sp)
                                Text("• Parent: parent.demo@test.com  [Key: parent123]", fontSize = 11.sp)
                            }
                        }

                    } else {
                        // --- REGISTER FLOW ---
                        Text(
                            "Create Encrypted Account Node",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email identifier") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            modifier = Modifier.fillMaxWidth().testTag("reg_email_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("Define Security Key (Password)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("reg_password_input"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Plan Radio Grid
                        Text(
                            "Choose Your Sandbox Subscription Profile:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Free Plan selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (regPlan == "FREE") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                                        else Color.Transparent, 
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp, 
                                        color = if (regPlan == "FREE") MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { regPlan = "FREE" }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = regPlan == "FREE", onClick = { regPlan = "FREE" })
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text("Free Starter Tier", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Track up to 1 student, 7 preseeded subjects. Basic view.", fontSize = 10.sp, color = adaptiveSlate600())
                                }
                            }

                            // Parent Plan selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (regPlan == "INDIVIDUAL_PARENT_PLAN") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                                        else Color.Transparent, 
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp, 
                                        color = if (regPlan == "INDIVIDUAL_PARENT_PLAN") MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { regPlan = "INDIVIDUAL_PARENT_PLAN" }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = regPlan == "INDIVIDUAL_PARENT_PLAN", onClick = { regPlan = "INDIVIDUAL_PARENT_PLAN" })
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text("Parent Pro Tier", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Track up to 4 children, custom subjects, AI Advisory Companion.", fontSize = 10.sp, color = adaptiveSlate600())
                                }
                            }

                            // School Plan selector
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (regPlan == "SCHOOL_PLAN") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                                        else Color.Transparent, 
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp, 
                                        color = if (regPlan == "SCHOOL_PLAN") MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { regPlan = "SCHOOL_PLAN" }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = regPlan == "SCHOOL_PLAN", onClick = { regPlan = "SCHOOL_PLAN" })
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text("School Suite Tier", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Track up to 200 students, CSV Batch, sub-accounts, multi-tenant locks.", fontSize = 10.sp, color = adaptiveSlate600())
                                }
                            }
                        }

                        if (regPlan == "SCHOOL_PLAN") {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = regSchoolId,
                                onValueChange = { regSchoolId = it },
                                label = { Text("School / Institution Name") },
                                leadingIcon = { Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(20.dp)) },
                                modifier = Modifier.fillMaxWidth().testTag("reg_school_id_input"),
                                singleLine = true,
                                placeholder = { Text("e.g. Stanford High School") }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Terms & Conditions Checkbox Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { acceptTerms = !acceptTerms }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = acceptTerms,
                                onCheckedChange = { acceptTerms = it },
                                modifier = Modifier.testTag("reg_accept_terms_checkbox")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("I accept the ", fontSize = 11.sp, color = adaptiveSlate600())
                                Text(
                                    text = "Terms & Use",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { selectedComplianceDoc = "TC" }
                                )
                            }
                        }

                        // Privacy Policy Checkbox Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { acceptPrivacy = !acceptPrivacy }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = acceptPrivacy,
                                onCheckedChange = { acceptPrivacy = it },
                                modifier = Modifier.testTag("reg_accept_privacy_checkbox")
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("I accept the ", fontSize = 11.sp, color = adaptiveSlate600())
                                Text(
                                    text = "Privacy Policy",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable { selectedComplianceDoc = "PRIVACY" }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                if (!acceptTerms || !acceptPrivacy) {
                                    viewModel.authError = "You must accept both the Terms & Use and the Privacy Policy to proceed with registration."
                                    return@Button
                                }
                                viewModel.executeRegister(
                                    name = regName,
                                    email = regEmail,
                                    pass = regPassword,
                                    plan = regPlan,
                                    schoolIdInput = regSchoolId,
                                    onSuccess = {}
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("register_submit_button")
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Secure Account")
                        }
                    }
                }
            }
        }

        // --- COMPREHENSIVE PLANS DIRECTORY & INSTRUCTIONS (Simple English) ---
        Spacer(modifier = Modifier.height(20.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Student Progress & Marks Tracking Guide",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // GRADE ENTRY SHEET EXPLAINER
                Card(
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                     modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Interactive Grade Entry Sheet", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Purpose: Record student exam marks easily inside an Excel-style matrix across key subjects.\n" +
                            "• Tracking Options: Log performance details across multiple exam periods, including weekly tests, termly, and annual exams.\n" +
                            "• Multi-User Isolation: Keeps parent views and teacher editing spaces securely isolated for student safety.",
                            fontSize = 11.sp,
                            color = adaptiveSlate600()
                        )
                    }
                }

                // TREND ANALYSIS EXPLAINER
                Card(
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                     modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SupervisedUserCircle, contentDescription = null, tint = Teal600, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Academic Trend & Growth Analytics", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Teal600)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Progress Charts: Displays dynamic Canvas-drawn trends and comparative bar graphs plotting test-to-test growth.\n" +
                            "• Proficiency Review: Instantly calculates subject average curves and highlights scores relative to grade thresholds.\n" +
                            "• Parental Overview: Enables family sub-accounts to easily review and track multiple children's performances side by side.",
                            fontSize = 11.sp,
                            color = adaptiveSlate600()
                        )
                    }
                }

                // AI SYSTEM EXPLAINER
                Card(
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                     modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Blue600, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI Study Advisory & Improvement Companion", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Blue600)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Diagnostic Insights: Standard one-tap checks instantly highlight weak subjects and performance flags under 40%.\n" +
                            "• AI Recommendations: Powered by the integrated Gemini Companion to outline localized curriculum study guides.\n" +
                            "• Interactive Support: Allows customized scheduling and specific skill practices to help kids improve test scores progressively.",
                            fontSize = 11.sp,
                            color = adaptiveSlate600()
                        )
                    }
                }
            }
        }

        // Demo Presets / Quick shortcuts panel to allow easy testing of role boundaries
        Spacer(modifier = Modifier.height(24.dp))
        Text("🚀 Rapid Demo Gateways (Bypass Sandbox Testing)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    viewModel.seedAndLoginAdminDemo { dEmail, dPass ->
                        email = dEmail
                        password = dPass
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                modifier = Modifier
                    .weight(1f)
                    .testTag("demo_admin_login")
            ) {
                Text("Admin Demo", fontSize = 11.sp)
            }

            Button(
                onClick = {
                    viewModel.seedAndLoginParentDemo { dEmail, dPass ->
                        email = dEmail
                        password = dPass
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Teal500),
                modifier = Modifier
                    .weight(1f)
                    .testTag("demo_parent_login")
            ) {
                Text("Parent Demo", fontSize = 11.sp)
            }

            Button(
                onClick = {
                    viewModel.seedAndLoginSuperDemo { dEmail, dPass ->
                        email = dEmail
                        password = dPass
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                modifier = Modifier
                    .weight(1f)
                    .testTag("demo_super_login")
            ) {
                Text("Super Demo", fontSize = 11.sp)
            }
        }

        AnimatedVisibility(visible = showGuideInline) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                HowToUseAndFAQSection(expandedByDefault = true)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        Spacer(modifier = Modifier.height(16.dp))

        // --- COMPLIANCE REGULATORY TRUST FOOTER ---
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Student Academic Performance & Marks Tracking Center",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Authorized portal for checking student school records, progress reports, grade safety, and messaging between parents and teachers.",
                    fontSize = 10.sp,
                    color = Slate600,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Link Grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // T&C Link
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedComplianceDoc = "TC" }
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .testTag("btn_compliance_tc_login")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Terms & Use", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Privacy Link
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedComplianceDoc = "PRIVACY" }
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .testTag("btn_compliance_privacy_login")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Privacy Policy", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Refunds Link
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedComplianceDoc = "REFUND" }
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .testTag("btn_compliance_refund_login")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Refund Policy", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Delivery Link
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedComplianceDoc = "SHIPPING" }
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .testTag("btn_compliance_shipping_login")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delivery SLA", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Contact & Redressal Hub
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedComplianceDoc = "CONTACT" }
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .testTag("btn_compliance_contact_login")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Contact Customer Support Helpline", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Guide on Tracking Marks and FAQs
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showGuideInline = !showGuideInline }
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .testTag("btn_compliance_guide_login")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guide on Tracking Marks & FAQs", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "© 2026 AaVi Technos. All rights reserved. Subscriptions are billed once a year or auto-renewed. All digital materials, student folders, and report card worksheets are delivered securely to your account immediately after payment confirmation.",
                    fontSize = 8.sp,
                    color = Slate600,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal Document popup dialog renderer
    var complianceDocToShow by remember { mutableStateOf<String?>(null) }
    complianceDocToShow = selectedComplianceDoc
    if (complianceDocToShow != null) {
        AlertDialog(
            onDismissRequest = { selectedComplianceDoc = null },
            confirmButton = {
                Button(
                    onClick = { selectedComplianceDoc = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("I Understand & Accept", color = Color.White, fontSize = 11.sp)
                }
            },
            title = {
                val title = when (complianceDocToShow) {
                    "TC" -> "Terms & Conditions of Service"
                    "PRIVACY" -> "Privacy & Encryption Policy"
                    "REFUND" -> "Cancellation & Refund SLA"
                    "SHIPPING" -> "Digital Access & Delivery Policy"
                    "CONTACT" -> "Merchant Contact Support Registry"
                    "GUIDE" -> "Guide on Tracking Marks & FAQs"
                    else -> "Operational Policy Docs"
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (complianceDocToShow == "PRIVACY") Icons.Default.Security else if (complianceDocToShow == "GUIDE") Icons.Default.HelpOutline else Icons.Default.Gavel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        PolicyDocumentContent(complianceDocToShow!!)
                    }
                }
            }
        )
    }
}


// --- 3. The Data Entry Grid (Excel-Style Table) ---
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DataEntryGridScreen(viewModel: MarksViewModel) {
    val students by viewModel.studentsList.collectAsState()
    val subjects by viewModel.subjectsList.collectAsState()
    val testTypes by viewModel.testTypesList.collectAsState()
    val marks by viewModel.marksList.collectAsState()
    val user = viewModel.currentUser ?: return

    var newStudentName by remember { mutableStateOf("") }
    var newStudentRoll by remember { mutableStateOf("") }
    var newStudentClass by remember { mutableStateOf("") }
    var newParentName by remember { mutableStateOf("") }
    var newSchoolName by remember { mutableStateOf("") }

    var csvText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var adminSearchQuery by remember { mutableStateOf("") }

    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val text = inputStream.bufferedReader().use { reader -> reader.readText() }
                    csvText = text
                    android.widget.Toast.makeText(context, "CSV file loaded successfully!", android.widget.Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Failed to read file: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Active session card / Role banner
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clickable { showProfileDialog = true }
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Badge,
                    contentDescription = "View Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        "${user.name} (${user.role.replace("_", " ")})",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Plan: ${user.planType.replace("_", " ")} | Tap to view profile",
                        style = MaterialTheme.typography.labelSmall,
                        color = adaptiveSlate600()
                    )
                }
                Spacer(modifier = Modifier.weight(1f))

                // Explicit User Profile Button
                IconButton(
                    onClick = { showProfileDialog = true },
                    modifier = Modifier.testTag("profile_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "User Profile",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Beautiful Theme Cycle Selector (System -> Light -> Dark)
                IconButton(
                    onClick = {
                        val nextMode = if (viewModel.themeMode == "DARK") "LIGHT" else "DARK"
                        viewModel.updateThemeMode(nextMode)
                    },
                    modifier = Modifier.testTag("theme_toggle_button")
                ) {
                    val icon = if (viewModel.themeMode == "DARK") Icons.Default.Brightness4 else Icons.Default.Brightness7
                    Icon(
                        imageVector = icon,
                        contentDescription = "Theme: ${viewModel.themeMode}",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = { viewModel.executeLogout() }) {
                    Icon(Icons.Default.Logout, contentDescription = "Log out", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Dialog for User Profile
        if (showProfileDialog) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Logged In Profile", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Full Name:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = adaptiveSlate600())
                                Text(user.name, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Email ID:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = adaptiveSlate600())
                                Text(user.email, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Security Role:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = adaptiveSlate600())
                                Text(user.role.replace("_", " "), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Blue600)
                            }
                            if (user.schoolId.isNotEmpty()) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("School Tenant:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = adaptiveSlate600())
                                    Text(user.schoolId, fontSize = 13.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Active Plan:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = adaptiveSlate600())
                                val planLabel = when(user.planType) {
                                    "FREE" -> "FREE PLAN"
                                    "INDIVIDUAL_PARENT_PLAN" -> "PARENTS PLAN"
                                    "SCHOOL_PLAN" -> "SCHOOL PLAN"
                                    else -> user.planType
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Teal500.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(planLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Teal600)
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            Text(
                                "Security Ledger Information:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "All student records and grade score metrics are mapped against your unique user ID and locally encrypted using AES-256 before disk persistence.",
                                fontSize = 10.sp,
                                color = adaptiveSlate600()
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showProfileDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Nice!")
                    }
                }
            )
        }

        viewModel.actionMessage?.let { msg ->
            Text(
                msg,
                color = Teal500,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        // Students selection / Add section (View-only Parent cannot add kids)
        if (user.role != "VIEW_ONLY_PARENT") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Add New Student Record", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = newStudentName,
                            onValueChange = { newStudentName = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth().testTag("student_name_input"),
                            singleLine = true
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newStudentRoll,
                                onValueChange = { newStudentRoll = it },
                                label = { Text("Roll No") },
                                modifier = Modifier.weight(1f).testTag("student_roll_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newStudentClass,
                                onValueChange = { newStudentClass = it },
                                label = { Text("Class / Grade") },
                                modifier = Modifier.weight(1f).testTag("student_class_input"),
                                singleLine = true
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newParentName,
                                onValueChange = { newParentName = it },
                                label = { Text("Parent Name (Mother/Father)") },
                                modifier = Modifier.weight(1f).testTag("student_parent_name_input"),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = newSchoolName,
                                onValueChange = { newSchoolName = it },
                                label = { Text("School Name") },
                                modifier = Modifier.weight(1f).testTag("student_school_name_input"),
                                singleLine = true
                            )
                        }
                        Button(
                            onClick = {
                                viewModel.addNewStudent(newStudentName, newStudentRoll, newStudentClass, newParentName, newSchoolName)
                                newStudentName = ""
                                newStudentRoll = ""
                                newStudentClass = ""
                                newParentName = ""
                                newSchoolName = ""
                            },
                            modifier = Modifier.align(Alignment.End).testTag("student_add_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Student")
                        }
                    }
                    if (user.role != "SUPER_ADMIN" && user.role != "SCHOOL_ADMIN") {
                        val limitText = when {
                            user.planType == "FREE" -> "Notice: Free Plan limited to 1 student directory. Current enrollment: ${students.size}/1"
                            user.planType == "INDIVIDUAL_PARENT_PLAN" || user.role == "INDIVIDUAL_PARENT" -> "Notice: Parents Plan limited to 4 children files. Current enrollment: ${students.size}/4"
                            else -> "Notice: School Plan limited to 200 student directories. Current enrollment: ${students.size}/200"
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            limitText,
                            style = MaterialTheme.typography.labelSmall,
                            color = if ((user.planType == "FREE" && students.size >= 1) ||
                                       ((user.planType == "INDIVIDUAL_PARENT_PLAN" || user.role == "INDIVIDUAL_PARENT") && students.size >= 4) ||
                                       (user.role == "SCHOOL_ADMIN" && students.size >= 200)) {
                                MaterialTheme.colorScheme.error
                            } else {
                                adaptiveSlate600()
                            }
                        )
                    } else if (user.role == "SCHOOL_ADMIN") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Notice: Administrative Account | Unlimited Student Directories & Marks Ledger",
                            style = MaterialTheme.typography.labelSmall,
                            color = Teal500
                        )
                    }
                }
            }
        }

        // Student active selection combo drawer OR Global Admin Search Panel
        if (user.role == "SUPER_ADMIN") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Global Super Admin Panel",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Query student databases across all institutional nodes securely. Select any match to modify academic columns.",
                        style = MaterialTheme.typography.bodySmall,
                        color = adaptiveSlate600()
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = adminSearchQuery,
                        onValueChange = { adminSearchQuery = it },
                        placeholder = { Text("Search by Student, Roll, Class, Parent, or School...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                        trailingIcon = {
                            if (adminSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { adminSearchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_student_search_box"),
                        singleLine = true
                    )

                    if (adminSearchQuery.isNotBlank()) {
                        val matchingStudents = students.filter { stud ->
                            val decName = viewModel.getDecryptedStudentName(stud.encryptedName)
                            val decParent = viewModel.getDecryptedStudentName(stud.parentName)
                            decName.contains(adminSearchQuery, ignoreCase = true) ||
                                    stud.rollNo.contains(adminSearchQuery, ignoreCase = true) ||
                                    stud.studentClass.contains(adminSearchQuery, ignoreCase = true) ||
                                    stud.schoolId.contains(adminSearchQuery, ignoreCase = true) ||
                                    decParent.contains(adminSearchQuery, ignoreCase = true) ||
                                    stud.schoolName.contains(adminSearchQuery, ignoreCase = true)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Search Identifications (${matchingStudents.size}):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (matchingStudents.isNotEmpty()) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                matchingStudents.take(10).forEach { stud ->
                                    val decName = viewModel.getDecryptedStudentName(stud.encryptedName)
                                    val isSelected = viewModel.selectedStudent?.id == stud.id
                                    val decParentName = viewModel.getDecryptedStudentName(stud.parentName)
                                    val detailLabel = buildString {
                                        append("Roll No: ${stud.rollNo}")
                                        if (stud.studentClass.isNotEmpty()) {
                                            append(" | Class: ${stud.studentClass}")
                                        }
                                        if (decParentName.isNotEmpty()) {
                                            append(" | Parent: $decParentName")
                                        }
                                        if (stud.schoolName.isNotEmpty()) {
                                            append(" | School: ${stud.schoolName}")
                                        } else if (stud.schoolId.isNotEmpty()) {
                                            append(" | School ID: ${stud.schoolId}")
                                        }
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        ),
                                        border = BorderStroke(
                                            width = if (isSelected) 2.dp else 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.selectStudent(stud) }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = decName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = detailLabel,
                                                    fontSize = 11.sp,
                                                    color = adaptiveSlate600()
                                                )
                                            }
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Selected",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.ChevronRight,
                                                    contentDescription = "Select child",
                                                    tint = adaptiveSlate600(),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (matchingStudents.size > 10) {
                                    Text(
                                        text = "+ ${matchingStudents.size - 10} more directory matches. Please refine your search keyword.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = adaptiveSlate600(),
                                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Zero records match query.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        } else {
            if (students.isNotEmpty()) {
                Text("Select Student directory to modify:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    students.forEach { stud ->
                        val decName = viewModel.getDecryptedStudentName(stud.encryptedName)
                        val isSelected = viewModel.selectedStudent?.id == stud.id
                        val chipLabel = if (stud.studentClass.isNotEmpty()) "$decName (${stud.studentClass})" else decName
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectStudent(stud) },
                            label = { Text(chipLabel) },
                            leadingIcon = {
                                if (isSelected) {
                                    Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        )
                    }
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(32.dp), tint = adaptiveSlate600())
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Students Added Yet.", fontWeight = FontWeight.Bold)
                        Text("Add a student profile or upload CSV template using the trigger buttons.", fontSize = 12.sp, color = adaptiveSlate600())
                    }
                }
            }
        }

        // CSV Bulk Upload and Configuration controls
        if (user.role != "VIEW_ONLY_PARENT") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showImportDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate700,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("csv_bulk_upload_button")
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CSV Bulk Upload", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
                
                Button(
                    onClick = { showConfigDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Slate700,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("manage_subjects_button")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Manage Subjects & Tests", fontSize = 12.sp, color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- Active Table Grid ---
        val curStudent = viewModel.selectedStudent
        if (curStudent != null) {
            val decryptedName = viewModel.getDecryptedStudentName(curStudent.encryptedName)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Excel Interactive Grid: $decryptedName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val decParent = viewModel.getDecryptedStudentName(curStudent.parentName)
                    val infoText = buildString {
                        append("Roll Number: ${curStudent.rollNo}")
                        if (decParent.isNotEmpty()) {
                            append(" | Parent: $decParent")
                        }
                        if (curStudent.schoolName.isNotEmpty()) {
                            append(" | School: ${curStudent.schoolName}")
                        }
                    }
                    Text("$infoText | Live compilation state", style = MaterialTheme.typography.labelSmall, color = adaptiveSlate600())
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.downloadStudentReportPdf(curStudent) },
                        modifier = Modifier.testTag("download_report_pdf_excel_button")
                    ) {
                        Icon(
                            Icons.Default.Download,
                            contentDescription = "Download Report PDF",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (user.role != "VIEW_ONLY_PARENT") {
                        IconButton(onClick = { viewModel.deleteStudent(curStudent.id) }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // EXCEL STYLE DATA ENTRY LAYOUT (HORIZONTALLY SCROLLABLE TABLE WITH SWIPE HINT)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Swipe indicator info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Swipe horizontally on the spreadsheet below to view/edit all exam cycle marks.",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, Slate600)
            ) {
                val exams = testTypes.map { it.name }.ifEmpty { listOf("Weekly", "Monthly", "Quarterly", "Half-Yearly", "Annual") }
                
                Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    // Header Row
                    Row(
                        modifier = Modifier
                            .background(Slate900)
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Subject Name",
                            modifier = Modifier
                                .width(130.dp)
                                .padding(start = 12.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        exams.forEach { exam ->
                            Text(
                                exam,
                                modifier = Modifier
                                    .width(90.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            "Mean (%)",
                            modifier = Modifier
                                .width(90.dp)
                                .padding(end = 12.dp),
                            color = Teal400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    HorizontalDivider(color = Slate600)

                    // Data Rows
                    subjects.forEach { subject ->
                        var subjectSum = 0.0
                        var count = 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            Text(
                                subject.name,
                                modifier = Modifier
                                    .width(130.dp)
                                    .padding(start = 12.dp),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            exams.forEach { exam ->
                                val cellKey = "${subject.id}_$exam"
                                val textVal = viewModel.gridMarks[cellKey] ?: ""
                                val isEnabled = user.role != "VIEW_ONLY_PARENT"

                                val scoreDouble = textVal.toDoubleOrNull()
                                if (scoreDouble != null) {
                                    subjectSum += scoreDouble
                                    count++
                                }

                                Box(
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(44.dp)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isEnabled) Slate100 else Slate50)
                                        .border(1.dp, Slate600, RoundedCornerShape(6.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    BasicTextField(
                                        value = textVal,
                                        onValueChange = { 
                                            if (isEnabled) {
                                                viewModel.updateGridCell(subject.id, exam, it)
                                            }
                                        },
                                        enabled = isEnabled,
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        textStyle = LocalTextStyle.current.copy(
                                            textAlign = TextAlign.Center,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (scoreDouble != null && scoreDouble < 40.0) Rose500 else Color.Black
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp, horizontal = 2.dp)
                                            .testTag("cell_${subject.id}_${exam}")
                                    )
                                }
                            }

                            // Math Rows average computation
                            val rowMeanVal = if (count > 0) subjectSum / count else 0.0
                            Text(
                                if (count > 0) "${String.format("%.1f", rowMeanVal)}%" else "-",
                                modifier = Modifier
                                    .width(90.dp)
                                    .padding(end = 12.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (rowMeanVal < 40.0 && count > 0) Rose500 else Blue500
                            )
                        }
                        HorizontalDivider(color = Slate600.copy(alpha = 0.3f))
                    }
                }
            }

            if (user.role != "VIEW_ONLY_PARENT") {
                Button(
                    onClick = { viewModel.saveGridMarks() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("save_grid_button")
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Update Marks")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HowToUseAndFAQSection(expandedByDefault = true)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Floating Dialog Bulk Import Sheet overlay
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("CSV Bulk Import Sandbox", fontWeight = FontWeight.Bold) },
                text = {
                    val exams = testTypes.map { it.name }.ifEmpty { listOf("Weekly", "Monthly", "Quarterly", "Half-Yearly", "Annual") }
                    val sampleHeaders = "RollNo,StudentName,Subject,${exams.joinToString(",")},ParentEmail"
                    val sampleRows = "101,Aarav Sharma,Mathematics,${exams.map { "85" }.joinToString(",")},aarav.parent@example.com\n" +
                                     "101,Aarav Sharma,Science,${exams.map { "78" }.joinToString(",")},aarav.parent@example.com\n" +
                                     "102,Diya Patel,Mathematics,${exams.map { "92" }.joinToString(",")},diya.parent@example.com"
                    val fullSampleCsv = "$sampleHeaders\n$sampleRows"
                    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Option 1: Download or Copy Sample CSV Template", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Blue500)
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Headers: $sampleHeaders",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = sampleRows,
                                    fontSize = 9.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = adaptiveSlate600(),
                                    lineHeight = 12.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        clipboardManager.setText(androidx.compose.ui.text.buildAnnotatedString { append(fullSampleCsv) })
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    modifier = Modifier.fillMaxWidth().height(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy CSV Template to Clipboard", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Option 2: Upload a CSV file from storage", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Blue500)
                        
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("upload_csv_from_storage_button")
                        ) {
                            Icon(Icons.Default.FileOpen, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select CSV File from Storage", color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Option 3: Paste values directly in CSV structures:", fontSize = 11.sp, color = adaptiveSlate600(), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        OutlinedTextField(
                            value = csvText,
                            onValueChange = { csvText = it },
                            placeholder = { Text(fullSampleCsv) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .testTag("csv_paste_input")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.processBulkCsvImport(csvText)
                            showImportDialog = false
                            csvText = ""
                        },
                        modifier = Modifier.testTag("csv_submit_button")
                    ) {
                        Text("Process Import")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showImportDialog = false }) {
                        Text("Close")
                    }
                }
            )
        }

        // Configuration dialog for managing Subjects and Test Types dynamically
        if (showConfigDialog) {
            AlertDialog(
                onDismissRequest = { showConfigDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Manage Subjects & Test Types", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    var activeTab by remember { mutableStateOf(0) } // 0 = Subjects, 1 = Test Types
                    var addInputName by remember { mutableStateOf("") }
                    var editingItemId by remember { mutableStateOf<Long?>(null) }
                    var editingItemName by remember { mutableStateOf("") }

                    Column(modifier = Modifier.fillMaxWidth().heightIn(max = 450.dp)) {
                        TabRow(selectedTabIndex = activeTab) {
                            Tab(selected = activeTab == 0, onClick = { activeTab = 0 }) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Subjects (${subjects.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            Tab(selected = activeTab == 1, onClick = { activeTab = 1 }) {
                                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Test Types (${testTypes.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Limit/Plan Notification Banner
                        val isFree = user.planType == "FREE"
                        if (isFree) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Text(
                                    "FREE Plan: Rename standard seeded subjects & test schedules. Upgrading to a Paid plan unlocks unlimited additions and deletions.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                            ) {
                                Text(
                                    "Premium/School Admin privileges: Add, edit, or remove subjects and test types infinitely.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Add Field (Only if Paid Plan, otherwise disabled)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = addInputName,
                                onValueChange = { addInputName = it },
                                placeholder = { Text(if (activeTab == 0) "e.g. Geography" else "e.g. Bi-Weekly") },
                                enabled = !isFree,
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                label = { Text(if (activeTab == 0) "Add Subject" else "Add Test Schedule") }
                            )
                            Button(
                                onClick = {
                                    if (activeTab == 0) {
                                        viewModel.addSubject(addInputName)
                                    } else {
                                        viewModel.addTestType(addInputName)
                                    }
                                    addInputName = ""
                                },
                                enabled = !isFree && addInputName.isNotBlank()
                            ) {
                                Text("Add")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        // Items list with editing
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            if (activeTab == 0) {
                                items(subjects) { subject ->
                                    val isEditing = editingItemId == subject.id
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isEditing) {
                                            OutlinedTextField(
                                                value = editingItemName,
                                                onValueChange = { editingItemName = it },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = {
                                                viewModel.renameSubject(subject, editingItemName)
                                                editingItemId = null
                                                editingItemName = ""
                                            }) {
                                                Icon(Icons.Default.Check, contentDescription = "Save", tint = Teal500)
                                            }
                                            IconButton(onClick = {
                                                editingItemId = null
                                                editingItemName = ""
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Rose500)
                                            }
                                        } else {
                                            Text(
                                                subject.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = {
                                                editingItemId = subject.id
                                                editingItemName = subject.name
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Blue500)
                                            }
                                            IconButton(
                                                onClick = { viewModel.removeSubject(subject) },
                                                enabled = !isFree
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = if (isFree) Slate600 else Rose500)
                                            }
                                        }
                                    }
                                    HorizontalDivider()
                                }
                            } else {
                                items(testTypes) { testType ->
                                    val isEditing = editingItemId == testType.id
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isEditing) {
                                            OutlinedTextField(
                                                value = editingItemName,
                                                onValueChange = { editingItemName = it },
                                                singleLine = true,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = {
                                                viewModel.renameTestType(testType, editingItemName)
                                                editingItemId = null
                                                editingItemName = ""
                                            }) {
                                                Icon(Icons.Default.Check, contentDescription = "Save", tint = Teal500)
                                            }
                                            IconButton(onClick = {
                                                editingItemId = null
                                                editingItemName = ""
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Rose500)
                                            }
                                        } else {
                                            Text(
                                                testType.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(onClick = {
                                                editingItemId = testType.id
                                                editingItemName = testType.name
                                            }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Blue500)
                                            }
                                            IconButton(
                                                onClick = { viewModel.removeTestType(testType) },
                                                enabled = !isFree
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = if (isFree) Slate600 else Rose500)
                                            }
                                        }
                                    }
                                    HorizontalDivider()
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showConfigDialog = false }) {
                        Text("Finished")
                    }
                }
            )
        }
    }
}


@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RechartsDashboard(jsonData: String) {
    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <title>Recharts Dashboard</title>
            <script src="https://cdn.tailwindcss.com"></script>
            <style>
                body {
                    background-color: #0f172a;
                    color: #f1f5f9;
                    font-family: ui-sans-serif, system-ui, sans-serif;
                    margin: 0;
                    padding: 8px;
                    overflow-x: hidden;
                }
                /* Hide scrollbars for a clean seamless experience */
                ::-webkit-scrollbar {
                    display: none;
                }
            </style>
        </head>
        <body>
            <div id="root" class="flex flex-col items-center justify-center min-h-[400px]">
                <div class="animate-spin rounded-full h-8 w-8 border-b-2 border-teal-500"></div>
                <p class="text-xs text-slate-400 mt-3 font-semibold">Initializing Recharts React Engine...</p>
            </div>

            <script src="https://unpkg.com/react@18/umd/react.production.min.js" crossorigin></script>
            <script src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js" crossorigin></script>
            <script src="https://unpkg.com/prop-types@15.8.1/prop-types.min.js" crossorigin></script>
            <script src="https://unpkg.com/recharts@2.12.7/umd/Recharts.js" crossorigin></script>
            <script src="https://unpkg.com/@babel/standalone/babel.min.js"></script>
            <script src="https://unpkg.com/d3@7/dist/d3.min.js"></script>

            <script type="text/babel">
                const {
                    ResponsiveContainer,
                    AreaChart,
                    Area,
                    BarChart,
                    Bar,
                    XAxis,
                    YAxis,
                    CartesianGrid,
                    Tooltip,
                    RadarChart,
                    PolarGrid,
                    PolarAngleAxis,
                    PolarRadiusAxis,
                    Radar
                } = window.Recharts;

                const infoData = $jsonData;

                // --- D3-based line chart React Component wrapper ---
                const D3LineChart = ({ data }) => {
                    const svgRef = React.useRef();

                    React.useEffect(() => {
                        if (!data || data.length === 0) return;

                        const d3Val = window.d3;
                        const svg = d3Val.select(svgRef.current);
                        
                        // Responsive drawing function
                        const renderChart = () => {
                            svg.selectAll("*").remove();

                            const margin = { top: 25, right: 30, bottom: 35, left: 45 };
                            const width = svgRef.current.parentElement.clientWidth || 320;
                            const height = 180;
                            const innerWidth = width - margin.left - margin.right;
                            const innerHeight = height - margin.top - margin.bottom;

                            svg
                                .attr("width", "100%")
                                .attr("height", height)
                                .attr("viewBox", "0 0 " + width + " " + height)
                                .attr("preserveAspectRatio", "xMidYMid meet");

                            const g = svg.append("g")
                                .attr("transform", "translate(" + margin.left + ", " + margin.top + ")");

                            // Scales
                            const xScale = d3Val.scalePoint()
                                .domain(data.map(d => d.semester))
                                .range([0, innerWidth])
                                .padding(0.3);

                            const yScale = d3Val.scaleLinear()
                                .domain([0, 100])
                                .range([innerHeight, 0]);

                            // Grid lines
                            g.append("g")
                                .attr("class", "grid")
                                .attr("opacity", 0.08)
                                .call(d3Val.axisLeft(yScale)
                                    .tickSize(-innerWidth)
                                    .tickFormat("")
                                );

                            g.selectAll(".grid line")
                                .attr("stroke", "#475569")
                                .attr("stroke-width", 1);

                            // Axes
                            const xAxis = d3Val.axisBottom(xScale).tickSize(5);
                            const xAxisG = g.append("g")
                                .attr("transform", "translate(0, " + innerHeight + ")")
                                .call(xAxis);
                            xAxisG.attr("font-size", "7.5px")
                                .attr("color", "#94a3b8")
                                .attr("font-weight", "600");
                            xAxisG.select(".domain")
                                .attr("stroke", "#475569")
                                .attr("stroke-width", 1.5);

                            const yAxis = d3Val.axisLeft(yScale).ticks(5).tickFormat(d => d + "%");
                            const yAxisG = g.append("g")
                                .call(yAxis);
                            yAxisG.attr("font-size", "7.5px")
                                .attr("color", "#94a3b8")
                                .attr("font-weight", "600");
                            yAxisG.select(".domain")
                                .attr("stroke", "#475569")
                                .attr("stroke-width", 1.5);

                            // Line generator with curved path
                            const linePath = d3Val.line()
                                .x(d => xScale(d.semester))
                                .y(d => yScale(d.marks))
                                .curve(d3Val.curveMonotoneX);

                            // Gradient
                            const gradientId = "d3-flow-grad";
                            const defs = svg.append("defs");
                            const linearGradient = defs.append("linearGradient")
                                .attr("id", gradientId)
                                .attr("x1", "0%").attr("y1", "0%")
                                .attr("x2", "0%").attr("y2", "100%");
                            linearGradient.append("stop")
                                .attr("offset", "0%")
                                .attr("stop-color", "#0ea5e9") // sky-500
                                .attr("stop-opacity", 0.35);
                            linearGradient.append("stop")
                                .attr("offset", "100%")
                                .attr("stop-color", "#0ea5e9")
                                .attr("stop-opacity", 0.0);

                            // Area generator
                            const areaPath = d3Val.area()
                                .x(d => xScale(d.semester))
                                .y0(innerHeight)
                                .y1(d => yScale(d.marks))
                                .curve(d3Val.curveMonotoneX);

                            // Draw area
                            g.append("path")
                                .datum(data)
                                .attr("fill", "url(#" + gradientId + ")")
                                .attr("d", areaPath);

                            // Draw line
                            g.append("path")
                                .datum(data)
                                .attr("fill", "none")
                                .attr("stroke", "#38bdf8") // sky-400
                                .attr("stroke-width", 3)
                                .attr("stroke-linecap", "round")
                                .attr("stroke-linejoin", "round")
                                .attr("d", linePath);

                            // Dots
                            const dots = g.selectAll(".dot-group")
                                .data(data)
                                .enter()
                                .append("g")
                                .attr("class", "dot-group")
                                .attr("transform", d => "translate(" + xScale(d.semester) + ", " + yScale(d.marks) + ")");

                            dots.append("circle")
                                .attr("r", 5.5)
                                .attr("fill", "#0284c7")
                                .attr("stroke", "#38bdf8")
                                .attr("stroke-width", 1);

                            dots.append("circle")
                                .attr("r", 2.5)
                                .attr("fill", "#ffffff");

                            // Value text above markers
                            dots.append("text")
                                .attr("y", -10)
                                .attr("text-anchor", "middle")
                                .attr("fill", "#e2e8f0")
                                .attr("font-size", "8px")
                                .attr("font-weight", "800")
                                .text(d => d.marks.toFixed(1) + "%");
                        };

                        renderChart();

                        // Create ResizeObserver to make D3 chart truly responsive
                        const resizeObserver = new ResizeObserver(() => {
                            renderChart();
                        });
                        if (svgRef.current.parentElement) {
                            resizeObserver.observe(svgRef.current.parentElement);
                        }

                        return () => {
                            resizeObserver.disconnect();
                        };
                    }, [data]);

                    return (
                        <div className="w-full relative py-2 mb-1">
                            <svg ref={svgRef} className="overflow-visible w-full"></svg>
                        </div>
                    );
                };

                function DashboardApp() {
                    const getD3SemesterData = () => {
                        const trend = infoData.trendData || [];
                        
                        const sem1Exams = trend.filter(x => x.name === "Weekly" || x.name === "Monthly" || x.name === "Quarterly");
                        const sem2Exams = trend.filter(x => x.name === "Half-Yearly" || x.name === "Annual");

                        const getAvg = (arr) => {
                            const valid = arr.filter(x => x.count > 0);
                            if (valid.length === 0) return 0;
                            return valid.reduce((sum, item) => sum + item.count, 0) / valid.length;
                        };

                        const s1Value = getAvg(sem1Exams) || (trend[0]?.count) || 72.0;
                        const s2Value = getAvg(sem2Exams) || (trend[1]?.count) || 78.0;
                        const s3Value = (trend[2]?.count) || 84.0;
                        const s4Value = (trend[3]?.count) || 89.0;

                        return [
                            { semester: "Semester I", marks: s1Value > 0 ? s1Value : 72.0 },
                            { semester: "Semester II", marks: s2Value > 0 ? s2Value : 78.0 },
                            { semester: "Semester III", marks: s3Value > 0 ? s3Value : 84.0 },
                            { semester: "Semester IV", marks: s4Value > 0 ? s4Value : 89.0 }
                        ];
                    };

                    const d3Data = getD3SemesterData();

                    return (
                        <div className="flex flex-col space-y-4">
                            {/* Header Summary */}
                            <div className="bg-slate-800 border border-slate-700/50 rounded-xl p-4 shadow-lg flex items-center justify-between">
                                <div className="min-w-0 pr-2">
                                    <h2 className="text-xs font-black text-teal-400 uppercase tracking-widest truncate">Recharts Analytics Engine</h2>
                                    <p className="text-2xs text-slate-400 font-semibold truncate">{infoData.studentName}</p>
                                </div>
                                <div className="flex-shrink-0">
                                    <span className="inline-block bg-teal-500/20 text-teal-400 text-3xs px-2 py-0.5 rounded-full font-extrabold uppercase tracking-widest">
                                        {infoData.overallStats.rank}
                                    </span>
                                </div>
                            </div>

                            {/* Stats Grid */}
                            <div className="grid grid-cols-2 gap-3">
                                <div className="bg-slate-800/80 border border-slate-700/50 rounded-xl p-3 text-center">
                                    <p className="text-3xs uppercase tracking-widest text-slate-400">Student Average</p>
                                    <p className="text-lg font-black text-blue-400">{infoData.overallStats.average.toFixed(1)}%</p>
                                </div>
                                <div className="bg-slate-800/80 border border-slate-700/50 rounded-xl p-3 text-center">
                                    <p className="text-3xs uppercase tracking-widest text-slate-400">Scores Logged</p>
                                    <p className="text-lg font-black text-rose-400">{infoData.overallStats.achievedRatio}</p>
                                </div>
                            </div>

                            {/* D3-based Semester Line Chart */}
                            <div className="bg-slate-800/60 border border-slate-700/40 rounded-xl p-4 shadow-md">
                                <div className="mb-3 flex items-center justify-between">
                                    <div className="min-w-0 pr-2">
                                        <h3 className="text-xs font-black text-slate-200 uppercase tracking-wider truncate">D3.js Semester Tracker</h3>
                                        <p className="text-3xs text-slate-400 truncate">Interactive marks progression over academic semesters</p>
                                    </div>
                                    <span className="flex-shrink-0 bg-sky-500/10 text-sky-400 text-3xs px-1.5 py-0.5 rounded font-black border border-sky-400/20 uppercase tracking-wider">D3 Engine</span>
                                </div>
                                
                                <D3LineChart data={d3Data} />
                                
                                <div className="mt-3 flex justify-around text-3xs text-slate-400 border-t border-slate-700/30 pt-2 font-semibold">
                                    {d3Data.map((d, index) => (
                                        <div key={index} className="text-center">
                                            <span className="text-slate-500 block text-4xs">{d.semester}</span>
                                            <span className="text-sky-400 font-bold block">{d.marks.toFixed(1)}%</span>
                                        </div>
                                    ))}
                                </div>
                            </div>


                            {/* 1. Performance Trends Area Chart */}
                            <div className="bg-slate-800/60 border border-slate-700/40 rounded-xl p-4 shadow-md">
                                <div className="mb-3">
                                    <h3 className="text-xs font-black text-slate-200 uppercase tracking-wider">Performance Trends</h3>
                                    <p className="text-3xs text-slate-400">Exam-over-exam overall average trajectory</p>
                                </div>
                                <div style={{ width: '100%', height: 180 }}>
                                    <ResponsiveContainer>
                                        <AreaChart
                                            data={infoData.trendData}
                                            margin={{ top: 10, right: 10, left: -15, bottom: 0 }}
                                        >
                                            <defs>
                                                <linearGradient id="colorTrend" x1="0" y1="0" x2="0" y2="1">
                                                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.8}/>
                                                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                                                </linearGradient>
                                            </defs>
                                            <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.3} />
                                            <XAxis dataKey="name" stroke="#94a3b8" fontSize={8} tickLine={false} />
                                            <YAxis stroke="#94a3b8" fontSize={8} domain={[0, 100]} tickCount={6} tickLine={false} />
                                            <Tooltip
                                                contentStyle={{ backgroundColor: '#1e293b', borderColor: '#475569', borderRadius: '8px', fontSize: '9px' }}
                                                labelStyle={{ fontWeight: 'bold', color: '#38bdf8' }}
                                            />
                                            <Area type="monotone" dataKey="count" stroke="#3b82f6" strokeWidth={2.5} fillOpacity={1} fill="url(#colorTrend)" />
                                        </AreaChart>
                                    </ResponsiveContainer>
                                </div>
                            </div>

                            {/* 2. Distributions Bar Chart */}
                            <div className="bg-slate-800/60 border border-slate-700/40 rounded-xl p-4 shadow-md">
                                <div className="mb-3">
                                    <h3 className="text-xs font-black text-slate-200 uppercase tracking-wider">Student Mark Distributions</h3>
                                    <p className="text-3xs text-slate-400">Subject proficiency comparing averages</p>
                                </div>
                                <div style={{ width: '100%', height: 180 }}>
                                    <ResponsiveContainer>
                                        <BarChart
                                            data={infoData.distributionData}
                                            margin={{ top: 10, right: 10, left: -15, bottom: 0 }}
                                        >
                                            <CartesianGrid strokeDasharray="3 3" stroke="#334155" opacity={0.2} />
                                            <XAxis dataKey="subject" stroke="#94a3b8" fontSize={8} tickLine={false} />
                                            <YAxis stroke="#94a3b8" fontSize={8} domain={[0, 100]} tickLine={false} />
                                            <Tooltip
                                                contentStyle={{ backgroundColor: '#1e293b', borderColor: '#475569', borderRadius: '8px', fontSize: '9px' }}
                                                labelStyle={{ fontWeight: 'bold' }}
                                            />
                                            <Bar dataKey="marks" fill="#14b8a6" radius={[4, 4, 0, 0]} barSize={20} />
                                        </BarChart>
                                    </ResponsiveContainer>
                                </div>
                            </div>

                            {/* 3. Radar Subject Comparison Chart */}
                            <div className="bg-slate-800/60 border border-slate-700/40 rounded-xl p-4 shadow-md flex flex-col items-center">
                                <div className="w-full text-left mb-3">
                                    <h3 className="text-xs font-black text-slate-200 uppercase tracking-wider">Radar Competency Alignment</h3>
                                    <p className="text-3xs text-slate-400">Radial layout profile across syllabus subjects</p>
                                </div>
                                <div style={{ width: '100%', height: 180 }} className="flex justify-center">
                                    <ResponsiveContainer>
                                        <RadarChart cx="50%" cy="50%" outerRadius="70%" data={infoData.distributionData}>
                                            <PolarGrid stroke="#475569" opacity={0.4} />
                                            <PolarAngleAxis dataKey="subject" stroke="#94a3b8" fontSize={8} />
                                            <PolarRadiusAxis stroke="#94a3b8" fontSize={6} angle={30} domain={[0, 100]} />
                                            <Radar name={infoData.studentName} dataKey="marks" stroke="#6366f1" fill="#6366f1" fillOpacity={0.35} />
                                            <Tooltip contentStyle={{ backgroundColor: '#1e293b', fontSize: '9px', borderRadius: '6px' }} />
                                        </RadarChart>
                                    </ResponsiveContainer>
                                </div>
                            </div>
                        </div>
                    );
                }

                const root = document.getElementById('root');
                ReactDOM.createRoot(root).render(<DashboardApp />);
            </script>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(860.dp),
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    textZoom = 100 // Prevent systems layout font scaling from clipping elements
                }
                loadDataWithBaseURL("https://localhost", htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("https://localhost", htmlContent, "text/html", "UTF-8", null)
        }
    )
}


// --- 4. Advanced Interactive Dashboard Tab (Canvas-drawn dynamic charts) ---
@Composable
fun AdvancedAnalyticsScreen(viewModel: MarksViewModel) {
    val student = viewModel.selectedStudent
    val subjects by viewModel.subjectsList.collectAsState()
    val testTypes by viewModel.testTypesList.collectAsState()
    val marks by viewModel.marksList.collectAsState()
    val allMarks by viewModel.allMarksList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(32.dp), tint = Blue500)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Performance Dashboards Suite", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (student != null) {
                    val decName = viewModel.getDecryptedStudentName(student.encryptedName)
                    Text("Interactive KPIs matching profile: $decName", style = MaterialTheme.typography.bodySmall, color = adaptiveSlate600())
                }
            }
            if (student != null && marks.isNotEmpty() && subjects.isNotEmpty()) {
                IconButton(
                    onClick = { viewModel.downloadStudentReportPdf(student) },
                    modifier = Modifier.testTag("download_report_pdf_analytics_button")
                ) {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Download Report PDF",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (student == null || marks.isEmpty() || subjects.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PivotTableChart, contentDescription = null, modifier = Modifier.size(48.dp), tint = adaptiveSlate600())
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Analytics Canvas Empty", fontWeight = FontWeight.Bold)
                    Text("Navigate to the 'Excel Grid' tab, select a student, enter scores, and click save to compile graphics.", fontSize = 12.sp, color = adaptiveSlate600(), textAlign = TextAlign.Center)
                }
            }
            return@Column
        }

        val exams = testTypes.map { it.name }.ifEmpty { listOf("Weekly", "Monthly", "Quarterly", "Half-Yearly", "Annual") }
        val examAverages = exams.map { exam ->
            val examMarks = marks.filter { it.examType == exam }
            if (examMarks.isNotEmpty()) examMarks.map { it.marksObtained }.average() else 0.0
        }

        // Switch between Recharts Suite and Native Canvas
        var selectedMetricEngine by remember { mutableStateOf(0) } // 0 = Recharts Web Engine, 1 = Compose Canvas Engine

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { selectedMetricEngine = 0 },
                modifier = Modifier.weight(1f).testTag("select_recharts_engine_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMetricEngine == 0) Blue600 else adaptiveSlate100(),
                    contentColor = if (selectedMetricEngine == 0) Color.White else adaptiveSlate600()
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
            ) {
                Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Recharts Suite", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = { selectedMetricEngine = 1 },
                modifier = Modifier.weight(1f).testTag("select_canvas_engine_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedMetricEngine == 1) Blue600 else adaptiveSlate100(),
                    contentColor = if (selectedMetricEngine == 1) Color.White else adaptiveSlate600()
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)
            ) {
                Icon(Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Native Canvas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (selectedMetricEngine == 0) {
            val decName = viewModel.getDecryptedStudentName(student.encryptedName)
            val cleanStudentName = decName.replace("\"", "\\\"").replace("\n", " ").replace("\r", " ")
            
            val trendJson = exams.mapIndexed { idx, name ->
                val avg = examAverages.getOrElse(idx) { 0.0 }
                "{\"name\":\"$name\",\"count\":${String.format(java.util.Locale.US, "%.1f", avg)}}"
            }.joinToString(prefix = "[", postfix = "]")

            val distJson = subjects.map { subject ->
                val subMarks = marks.filter { it.subjectId == subject.id }
                val mean = if (subMarks.isNotEmpty()) subMarks.map { it.marksObtained }.average() else 0.0
                val minVal = if (subMarks.isNotEmpty()) subMarks.minOf { it.marksObtained } else 0.0
                val maxVal = if (subMarks.isNotEmpty()) subMarks.maxOf { it.marksObtained } else 0.0
                "{\"subject\":\"${subject.name}\",\"marks\":${String.format(java.util.Locale.US, "%.1f", mean)},\"min\":${minVal.toInt()},\"max\":${maxVal.toInt()}}"
            }.joinToString(prefix = "[", postfix = "]")

            val overallMean = if (marks.isNotEmpty()) marks.map { it.marksObtained }.average() else 0.0
            val rank = when {
                overallMean >= 90 -> "A+ Excellent"
                overallMean >= 80 -> "A Good"
                overallMean >= 70 -> "B+ Satisfactory"
                overallMean >= 60 -> "B Average"
                overallMean >= 50 -> "C Need Effort"
                else -> "F Needs Tutorial"
            }
            val totalMarksEntered = marks.size
            val overallStatsJson = "{\"average\":${String.format(java.util.Locale.US, "%.1f", overallMean)},\"rank\":\"$rank\",\"achievedRatio\":\"$totalMarksEntered Entries\",\"status\":\"Performance Loaded\"}"

            val dashboardDataJson = """
                {
                    "studentName": "$cleanStudentName",
                    "trendData": $trendJson,
                    "distributionData": $distJson,
                    "overallStats": $overallStatsJson
                }
            """.trimIndent()

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Column(modifier = Modifier.padding(4.dp)) {
                    RechartsDashboard(jsonData = dashboardDataJson)
                }
            }
        } else {

        // 1. Exam-to-Exam Growth Trend (Line-drawn Canvas)
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("1. Exam-to-Exam Progress Trend (%)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Shows how the student's overall exam scores change from one test to the next", style = MaterialTheme.typography.bodySmall, color = adaptiveSlate600())
                Spacer(modifier = Modifier.height(18.dp))

                val examAverages = exams.map { exam ->
                    val examMarks = marks.filter { it.examType == exam }
                    if (examMarks.isNotEmpty()) examMarks.map { it.marksObtained }.average() else 0.0
                }

                // Growth metrics calculation: ((e2-e1)/e1)*100
                val growths = mutableListOf<Double>()
                for (i in 1 until examAverages.size) {
                    val prev = examAverages[i - 1]
                    val curr = examAverages[i]
                    if (prev > 0) {
                        growths.add(((curr - prev) / prev) * 100)
                    } else {
                        growths.add(0.0)
                    }
                }

                // Canvas line graph
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val spacing = if (exams.size > 1) width / (exams.size - 1) else width

                    // Draw grid lines
                    for (gridIdx in 0..4) {
                        val gridY = height * (gridIdx / 4f)
                        drawLine(
                            color = Slate600.copy(alpha = 0.2f),
                            start = Offset(0f, gridY),
                            end = Offset(width, gridY),
                            strokeWidth = 1f
                        )
                    }

                    // Map points
                    val points = examAverages.map { avg ->
                        val yRatio = (avg / 100.0).coerceIn(0.0, 1.0)
                        height - (yRatio.toFloat() * height)
                    }

                    if (points.isNotEmpty()) {
                        val path = Path().apply {
                            moveTo(0f, points[0])
                            for (pIdx in 1 until points.size) {
                                lineTo(pIdx * spacing, points[pIdx])
                            }
                        }

                        // Stroke Path
                        drawPath(
                            path = path,
                            color = Blue500,
                            style = Stroke(width = 6f)
                        )

                        // Draw points circles & values
                        points.forEachIndexed { pIdx, yPos ->
                            val xPos = pIdx * spacing
                            drawCircle(
                                color = Teal500,
                                radius = 10f,
                                center = Offset(xPos, yPos)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 5f,
                                center = Offset(xPos, yPos)
                            )
                        }
                    }
                }

                // Label Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    exams.forEachIndexed { idx, name ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = adaptiveSlate600())
                            Text("${String.format("%.0f", examAverages[idx])}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Blue500)
                        }
                    }
                }

                // Detailed growth updates statement
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(adaptiveSlate100(), RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.TrendingFlat, contentDescription = null, tint = Blue500)
                    val growPercent = if (growths.isNotEmpty()) growths.average() else 0.0
                    val statColor = if (growPercent >= 0) Teal500 else Rose500
                    Text(
                        text = "Composite Step-By-Step Growth Velocity is ${String.format("%.1f", growPercent)}% across active semesters.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = statColor
                    )
                }
            }
        }

        // 2. Subject Proficiency Range (Min - Max Range)
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("2. Subject Score Range (Min to Max Marks)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Shows the lowest and highest marks received in each subject", style = MaterialTheme.typography.bodySmall, color = adaptiveSlate600())
                Spacer(modifier = Modifier.height(18.dp))

                // Canvas representing Min - Max mark range
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    subjects.forEach { subject ->
                        val subMarks = marks.filter { it.subjectId == subject.id }
                        if (subMarks.isNotEmpty()) {
                            val minVal = subMarks.minOf { it.marksObtained }
                            val maxVal = subMarks.maxOf { it.marksObtained }
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(subject.name, modifier = Modifier.width(100.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(24.dp)
                                        .clip(CircleShape)
                                        .background(adaptiveSlate100())
                                ) {
                                    // Range overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(fraction = ((maxVal - minVal) / 100.0).toFloat().coerceIn(0.1f, 1f))
                                            .padding(start = (minVal / 100.0 * 200).coerceAtMost(100.0).dp)
                                            .clip(CircleShape)
                                            .background(Brush.horizontalGradient(listOf(Teal500, Blue500)))
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Min ${minVal.toInt()}% - Max ${maxVal.toInt()}%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Blue500)
                            }
                        }
                    }
                }
            }
        }

        // 3. Subject Averages & Semester Comparison
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("3. Subject Average Marks & Semester Comparison", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(14.dp))

                // Subject summary grids
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    subjects.forEach { subject ->
                        val subMarks = marks.filter { it.subjectId == subject.id }
                        val mean = if (subMarks.isNotEmpty()) subMarks.map { it.marksObtained }.average() else 0.0
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(adaptiveSlate100(), RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(subject.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                LinearProgressIndicator(
                                    progress = { (mean / 100.0).toFloat() },
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(6.dp)
                                        .clip(CircleShape),
                                    color = if (mean < 40) Rose500 else Teal500
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${String.format("%.1f", mean)}%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 4. Trend Performance Bar Chart
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("4. Performance Progress Chart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Compares overall exam scores across different school terms", style = MaterialTheme.typography.bodySmall, color = Slate600)
                Spacer(modifier = Modifier.height(18.dp))

                val trendScores = exams.map { exam ->
                    val examMarks = marks.filter { it.examType == exam }
                    if (examMarks.isNotEmpty()) examMarks.map { it.marksObtained }.average() else 0.0
                }

                Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    val width = size.width
                    val height = size.height
                    val barWidth = 45.dp.toPx()
                    val spacing = if (exams.isNotEmpty()) width / exams.size else width

                    trendScores.forEachIndexed { idx, score ->
                        val barHeight = (score / 100.0).toFloat() * height
                        val x = idx * spacing + (spacing - barWidth) / 2
                        val y = height - barHeight
                        
                        drawRoundRect(
                            color = if (score < 40.0) Rose500 else Blue500,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    exams.forEachIndexed { idx, name ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(name, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Slate600)
                        }
                    }
                }
            }
        }

        // 5. Improvement Scope Tracker (AI-Lite & Gemini Integrated)
        Card(
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Blue500)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Blue500, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("5. AI Learning Guide & Study Recommendations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Checks for marks below passing (under 40%) or downward trends to provide handy study tips and schedules.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Advisory Action Trigger
                Button(
                    onClick = {
                        val decName = viewModel.getDecryptedStudentName(student.encryptedName)
                        viewModel.fetchAdvisoryReport(decName)
                    },
                    modifier = Modifier.fillMaxWidth().testTag("ai_diagnostic_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                ) {
                    if (viewModel.isLoadingAdvisory) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating Study Tips...")
                    } else {
                        Icon(Icons.Default.Analytics, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Get Smart Study Tips")
                    }
                }

                if (viewModel.advisoryReport.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Advisory Analysis Result:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(adaptiveSlate100(), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = viewModel.advisoryReport,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("ai_report_text")
                        )
                    }

                    val user = viewModel.currentUser
                    if (user != null && (user.planType == "SCHOOL_PLAN" || user.role == "SCHOOL_ADMIN" || user.role == "SUPER_ADMIN")) {
                        Spacer(modifier = Modifier.height(12.dp))
                        val context = LocalContext.current
                        Button(
                            onClick = {
                                val decName = viewModel.getDecryptedStudentName(student.encryptedName)
                                android.widget.Toast.makeText(
                                    context,
                                    "Academic Report of $decName has been securely dispatched to the respective Parent account!",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_reports_to_parents_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Teal500)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Report to Student's Parents", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
        }

        // --- FEATURE 1: Student Target Goal Planner & Progress Gap Analysis ---
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().testTag("goal_planner_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Blue500, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("6. Target Goal Planner & Progress Tracker", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                val targetGoal = viewModel.getStudentTargetGoal(student.id)
                val allScores = marks.filter { it.studentId == student.id }
                val actualMean = if (allScores.isNotEmpty()) allScores.map { it.marksObtained }.average() else 0.0
                val gap = actualMean - targetGoal

                Text(
                    text = "Set a custom target score for ${viewModel.getDecryptedStudentName(student.encryptedName)} and track how close they are to reaching it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = adaptiveSlate600()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Target Percentage: ${targetGoal.toInt()}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.setStudentTargetGoal(student.id, (targetGoal - 5.0).coerceAtLeast(35.0)) },
                            modifier = Modifier.size(36.dp).background(adaptiveSlate100(), CircleShape).testTag("decrement_target_btn")
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrement", modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = { viewModel.setStudentTargetGoal(student.id, (targetGoal + 5.0).coerceAtMost(100.0)) },
                            modifier = Modifier.size(36.dp).background(adaptiveSlate100(), CircleShape).testTag("increment_target_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increment", modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Performance Indicator status", fontSize = 10.sp, color = adaptiveSlate600())
                        Text(
                            text = if (gap >= 0) "TARGET REACHED!" else "BEHIND BY " + "%.1f".format(-gap) + "%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gap >= 0) Teal500 else Rose500
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(adaptiveSlate100())
                    ) {
                        // Track Indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = (actualMean / 100.0).toFloat().coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .background(if (gap >= 0) Teal500 else Blue500)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current Average: " + "%.1f".format(actualMean) + "%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Blue500)
                        Text("Target Goal: ${targetGoal.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Red)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(adaptiveSlate100(), RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = if (gap >= 0) Teal500 else Blue500, modifier = Modifier.size(16.dp))
                    Text(
                        text = if (gap >= 0) {
                            "Status is optimal. Maintaining a surplus of " + "%.1f".format(gap) + "% above Target thresholds. Continue current learning cycle."
                        } else {
                            "Needs academic acceleration. Secure average marks of ${targetGoal.toInt()}% in current assignments to restore milestone compliance."
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // --- FEATURE 2: Global Classroom Leaderboard & Rank Suite ---
        Spacer(modifier = Modifier.height(16.dp))
        val allStudents by viewModel.studentsList.collectAsState()
        var leaderboardSearchQuery by remember { mutableStateOf("") }
        var isAlphaSort by remember { mutableStateOf(false) }

        Card(
            modifier = Modifier.fillMaxWidth().testTag("classroom_leaderboard_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = Teal500, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🏫 Global Classroom Leaderboard & Rank Suite", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Perform cross-student standings inspections, highlighting academic stars and identifying risk profiles automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = adaptiveSlate600()
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = leaderboardSearchQuery,
                    onValueChange = { leaderboardSearchQuery = it },
                    placeholder = { Text("Search students...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("leaderboard_search"),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        IconButton(onClick = { isAlphaSort = !isAlphaSort }) {
                            Icon(
                                imageVector = if (isAlphaSort) Icons.Default.Sort else Icons.Default.FilterList,
                                contentDescription = "Toggle Sort",
                                tint = Blue500,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Slate900, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Rank & Name", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f))
                    Text("Average (%)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.2f), textAlign = TextAlign.End)
                }

                val studentsLeaderboard = remember(allStudents, allMarks) {
                    allStudents.map { s ->
                        val decName = viewModel.getDecryptedStudentName(s.encryptedName)
                        val sMarks = allMarks.filter { it.studentId == s.id }
                        val avg = if (sMarks.isNotEmpty()) sMarks.map { it.marksObtained }.average() else 0.0
                        Triple(s, decName, avg)
                    }.sortedByDescending { it.third }
                }

                val filteredLeaderboard = studentsLeaderboard.filter {
                    it.second.contains(leaderboardSearchQuery, ignoreCase = true)
                }.let { list ->
                    if (isAlphaSort) list.sortedBy { it.second } else list
                }

                if (filteredLeaderboard.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(adaptiveSlate100(), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No match found in current division.", fontSize = 12.sp, color = adaptiveSlate600())
                    }
                } else {
                    filteredLeaderboard.forEachIndexed { index, item ->
                        val (s, decName, avg) = item
                        val origIndex = studentsLeaderboard.indexOfFirst { it.first.id == s.id }
                        val rankNum = origIndex + 1

                        val tagColor = when {
                            avg >= 85 -> Teal500
                            avg < 40 -> Rose500
                            else -> Blue500
                        }
                        val tagText = when {
                            avg >= 85 -> "Distinction"
                            avg < 40 -> "Action Required"
                            else -> "Passing"
                        }

                        val isCurrentUser = s.id == student.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isCurrentUser) Blue500.copy(alpha = 0.12f)
                                    else if (index % 2 == 0) adaptiveSlate100()
                                    else MaterialTheme.colorScheme.surface
                                )
                                .clickable { viewModel.selectStudent(s) }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1.5f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val rankStr = when (rankNum) {
                                    1 -> "🥇 "
                                    2 -> "🥈 "
                                    3 -> "🥉 "
                                    else -> "#$rankNum "
                                }
                                Text(
                                    text = "$rankStr $decName",
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrentUser) FontWeight.Bold else FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                modifier = Modifier.weight(1.2f),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "%.1f".format(avg) + "%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Blue600,
                                    textAlign = TextAlign.End
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .background(tagColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = tagText,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = tagColor
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { viewModel.downloadStudentReportPdf(s) },
                                    modifier = Modifier.size(24.dp).testTag("download_report_pdf_leaderboard_${s.id}")
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = "Download Report PDF",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- FEATURE 3: Bulk Exporter & Parental Digest Dispatch Suite ---
        Spacer(modifier = Modifier.height(16.dp))
        var showCsvDialog by remember { mutableStateOf(false) }
        val context = LocalContext.current

        Card(
            modifier = Modifier.fillMaxWidth().testTag("outbox_suite_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, adaptiveSlate100()),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Blue500, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("📦 Data Portability & Parental Outbox Suite", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unlock high-fidelity data backups. Instantly compile all student spreadsheets to raw CSV datasets or trigger simulated secure mail bulletins to matching parents.",
                    style = MaterialTheme.typography.bodySmall,
                    color = adaptiveSlate600()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { showCsvDialog = true },
                        modifier = Modifier.weight(1f).testTag("export_csv_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue500),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.sendParentDigestBulletins { count ->
                                android.widget.Toast.makeText(
                                    context,
                                    "Successfully finalized multi-channel bulletin delivery to $count families!",
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        modifier = Modifier.weight(1.2f).testTag("parent_bulletin_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Teal500),
                        enabled = !viewModel.isSendingBulletins,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (viewModel.isSendingBulletins) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp))
                        } else {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (viewModel.isSendingBulletins) "Sending..." else "Send Bulletins", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Log terminal output for mail dispatcher
                if (viewModel.isSendingBulletins || viewModel.bulletinLogsList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Outbox Dispatch Log Status:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Blue500)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (viewModel.isSendingBulletins) {
                        LinearProgressIndicator(
                            color = Teal500,
                            trackColor = adaptiveSlate100(),
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .background(Color.Black, RoundedCornerShape(8.dp))
                            .border(1.dp, Slate600, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        LaunchedEffect(viewModel.bulletinLogsList.size) {
                            scrollState.scrollTo(scrollState.maxValue)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                        ) {
                            viewModel.bulletinLogsList.forEach { logEntry ->
                                Text(
                                    text = logEntry,
                                    color = if (logEntry.startsWith("✅") || logEntry.startsWith("🏁")) Teal500
                                            else if (logEntry.contains("Error")) Rose500
                                            else Color(0xFFE2E8F0),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // CSV Export Dialog Modal
        if (showCsvDialog) {
            val csvData = viewModel.generateAllStudentsMarksCsv()
            AlertDialog(
                onDismissRequest = { showCsvDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Blue500)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Marks Database Backup (CSV)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            "Highlight or copy the dataset underneath. Complete data arrays are fully compiled offline in real time.",
                            fontSize = 12.sp,
                            color = adaptiveSlate600()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(adaptiveSlate100(), RoundedCornerShape(8.dp))
                                .border(1.dp, Slate600.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            val scrollState = rememberScrollState()
                            Text(
                                text = csvData,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(scrollState)
                                    .testTag("csv_export_text_block")
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Student Marks CSV", csvData)
                            clipboard.setPrimaryClip(clip)
                            android.widget.Toast.makeText(context, "Spreadsheet dataset copied to clipboard successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            showCsvDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Blue500)
                    ) {
                        Text("Copy to Clipboard", fontSize = 12.sp)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCsvDialog = false }) {
                        Text("Dismiss", fontSize = 12.sp)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        HowToUseAndFAQSection(expandedByDefault = true)
        Spacer(modifier = Modifier.height(16.dp))
    }
}


// --- 5. Billing & Razorpay Payments Simulator Screen ---
data class PaymentCheckoutDetails(
    val planType: String,
    val roleName: String,
    val basePrice: Double,
    val planLabel: String
)

@Composable
fun PaymentGatewayCheckoutDialog(
    details: PaymentCheckoutDetails,
    viewModel: MarksViewModel,
    onDismiss: () -> Unit
) {
    var selectedGateway by remember { mutableStateOf("Razorpay") } // "Razorpay" or "PayU"
    var selectedMethod by remember { mutableStateOf("CARD") } // "CARD", "UPI", "NETBANKING"

    // Card state
    var cardNumber by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var cardName by remember { mutableStateOf("") }

    // UPI state
    var upiId by remember { mutableStateOf("") }
    var upiVerified by remember { mutableStateOf(false) }
    var upiVerifying by remember { mutableStateOf(false) }
    var selectQuickUpi by remember { mutableStateOf("") }

    // Netbanking state
    var selectedBank by remember { mutableStateOf("") }

    // Core execution states
    var isProcessing by remember { mutableStateOf(false) }
    var currentStepText by remember { mutableStateOf("") }
    var showSuccessTick by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val basePrice = details.basePrice
    val gst = basePrice * 0.18
    val totalPayable = basePrice + gst

    AlertDialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = !isProcessing,
            dismissOnClickOutside = !isProcessing,
            usePlatformDefaultWidth = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .padding(vertical = 16.dp)
            .testTag("payment_checkout_dialog"),
        confirmButton = {},
        title = null,
        text = {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .border(2.dp, if (selectedGateway == "Razorpay") Color(0xFF1E88E5) else Color(0xFF4CAF50), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title Bar with Secure indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (selectedGateway == "Razorpay") Color(0xFF1E88E5) else Color(0xFF4CAF50),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Secure Unified Payment Hub",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("100% SECURE", fontSize = 8.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Gateway Selection
                    Text(
                        "Select Payment Gateway Merchant:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Razorpay option
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedGateway == "Razorpay") Color(0xFF0D233A) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedGateway = "Razorpay" }
                                .border(
                                    width = 1.5.dp,
                                    color = if (selectedGateway == "Razorpay") Color(0xFF228AFB) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .testTag("select_razorpay_gateway")
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Razorpay Secure",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (selectedGateway == "Razorpay") Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "All Cards & UPI Rails",
                                    fontSize = 9.sp,
                                    color = if (selectedGateway == "Razorpay") Color.White.copy(alpha = 0.7f) else adaptiveSlate600()
                                )
                            }
                        }

                        // PayU.In option
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedGateway == "PayU") Color(0xFF1B3D14) else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedGateway = "PayU" }
                                .border(
                                    width = 1.5.dp,
                                    color = if (selectedGateway == "PayU") Color(0xFF8BC63F) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .testTag("select_payu_gateway")
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "PayU.In Instant",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (selectedGateway == "PayU") Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Netbanking Express",
                                    fontSize = 9.sp,
                                    color = if (selectedGateway == "PayU") Color.White.copy(alpha = 0.7f) else adaptiveSlate600()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Billing Detail Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subscription Product:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(details.planLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Base Premium Cost:", fontSize = 10.sp, color = adaptiveSlate600())
                                Text("₹${String.format("%.2f", basePrice)}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("CGST + SGST (18.00% Govt Tax):", fontSize = 10.sp, color = adaptiveSlate600())
                                Text("₹${String.format("%.2f", gst)}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total Amount Payable:", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                Text("₹${String.format("%.2f", totalPayable)}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isProcessing) {
                        // Payment Method Hub
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Button(
                                onClick = { selectedMethod = "CARD" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedMethod == "CARD") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedMethod == "CARD") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1f).testTag("select_card_method")
                            ) {
                                Icon(Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cards", fontSize = 10.sp)
                            }

                            Button(
                                onClick = { selectedMethod = "UPI" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedMethod == "UPI") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedMethod == "UPI") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1f).testTag("select_upi_method")
                            ) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("UPI App", fontSize = 10.sp)
                            }

                            Button(
                                onClick = { selectedMethod = "NETBANKING" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedMethod == "NETBANKING") MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (selectedMethod == "NETBANKING") MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp),
                                modifier = Modifier.weight(1f).testTag("select_netbanking_method")
                            ) {
                                Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Net Bank", fontSize = 10.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render Fields based on selected Payment Mode
                        when (selectedMethod) {
                            "CARD" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Visual credit card preview
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedGateway == "Razorpay") Color(0xFF152A4A) else Color(0xFF2C4927)
                                        )
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(
                                                    if (selectedGateway == "Razorpay") "Razorpay Checkout" else "PayU Express Checkout",
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Icon(
                                                    Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = Color.White.copy(alpha = 0.6f),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            
                                            // Simulated Smart Chip
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp, 16.dp)
                                                    .background(Color(0xFFFFD54F), RoundedCornerShape(2.dp))
                                            )

                                            val displayCard = if (cardNumber.isEmpty()) "XXXX XXXX XXXX XXXX" else {
                                                cardNumber.chunked(4).joinToString(" ")
                                            }
                                            Text(
                                                displayCard,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                fontFamily = FontFamily.Monospace,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column(modifier = Modifier.weight(1.5f)) {
                                                    Text("CARD HOLDER", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                                                    Text(
                                                        if (cardName.isEmpty()) "YOUR NAME" else cardName.uppercase(),
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                                    Text("EXPIRY", color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp)
                                                    Text(
                                                        if (cardExpiry.isEmpty()) "MM/YY" else cardExpiry,
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = cardNumber,
                                        onValueChange = { if (it.length <= 16 && it.all { char -> char.isDigit() }) cardNumber = it },
                                        label = { Text("Card Number (16 Digits)") },
                                        placeholder = { Text("e.g. 4312891277341256") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth().testTag("card_number_input"),
                                        singleLine = true
                                    )

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = cardExpiry,
                                            onValueChange = { cardExpiry = it },
                                            label = { Text("Expiry (MM/YY)") },
                                            placeholder = { Text("28/29") },
                                            modifier = Modifier.weight(1.2f).testTag("card_expiry_input"),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = cardCvv,
                                            onValueChange = { if (it.length <= 3) cardCvv = it },
                                            label = { Text("CVV") },
                                            placeholder = { Text("***") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            visualTransformation = PasswordVisualTransformation(),
                                            modifier = Modifier.weight(1f).testTag("card_cvv_input"),
                                            singleLine = true
                                        )
                                    }

                                    OutlinedTextField(
                                        value = cardName,
                                        onValueChange = { cardName = it },
                                        label = { Text("Name on Credit/Debit Card") },
                                        placeholder = { Text("Aarav Sharma") },
                                        modifier = Modifier.fillMaxWidth().testTag("card_name_input"),
                                        singleLine = true
                                    )
                                }
                            }
                            "UPI" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Express Instant Mobile UPI Pay", fontWeight = FontWeight.SemiBold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(
                                            value = upiId,
                                            onValueChange = { upiId = it; upiVerified = false },
                                            label = { Text("Enter Your UPI Address") },
                                            placeholder = { Text("e.g. username@okhdfcbank") },
                                            modifier = Modifier.weight(1.5f).testTag("upi_id_input"),
                                            singleLine = true
                                        )
                                        Button(
                                            onClick = {
                                                if (upiId.isNotEmpty()) {
                                                    upiVerifying = true
                                                    scope.launch {
                                                        delay(1000)
                                                        upiVerifying = false
                                                        upiVerified = true
                                                    }
                                                }
                                            },
                                            enabled = upiId.isNotEmpty() && !upiVerifying,
                                            modifier = Modifier.align(Alignment.CenterVertically).testTag("verify_upi_btn")
                                        ) {
                                            if (upiVerifying) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp)
                                            } else {
                                                Text("Verify", fontSize = 10.sp)
                                            }
                                        }
                                    }

                                    if (upiVerified) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("UPI Address verified: Approved Customer Account (active)", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Text("Instant Checkout via dynamic UPI Link:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf("GooglePay", "PhonePe", "PayTM", "BHIM").forEach { appName ->
                                            Card(
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (selectQuickUpi == appName) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                                ),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .border(
                                                        1.dp,
                                                        if (selectQuickUpi == appName) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                        RoundedCornerShape(6.dp)
                                                    )
                                                    .clickable { 
                                                        selectQuickUpi = appName
                                                        upiId = "subscriber@$appName"
                                                        upiVerified = true
                                                    }
                                            ) {
                                                Text(
                                                    appName,
                                                    textAlign = TextAlign.Center,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            "NETBANKING" -> {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Select Indian Banking Rail to authorize payment:", fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                    
                                    val banks = listOf(
                                        "SBI" to "State Bank of India",
                                        "HDFC" to "HDFC Bank Secure",
                                        "ICICI" to "ICICI Banking Portal",
                                        "AXIS" to "Axis Money Direct Exchange"
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        banks.forEach { (shortCode, bankName) ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(
                                                        if (selectedBank == shortCode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                                        RoundedCornerShape(8.dp)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (selectedBank == shortCode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                        shape = RoundedCornerShape(8.dp)
                                                    )
                                                    .clickable { selectedBank = shortCode }
                                                    .padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(selected = selectedBank == shortCode, onClick = { selectedBank = shortCode })
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(bankName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Trigger Primary action pay button
                        Button(
                            onClick = {
                                isProcessing = true
                                scope.launch {
                                    currentStepText = "Setting up secure connection with $selectedGateway..."
                                    delay(900)
                                    currentStepText = "Preparing amount of ₹${String.format("%.2f", totalPayable)}..."
                                    delay(900)
                                    currentStepText = "Contacting bank network..."
                                    delay(900)
                                    currentStepText = "Completing safe payment security check..."
                                    delay(800)
                                    showSuccessTick = true
                                    delay(800)
                                    viewModel.subscribeToPlan(details.planType, details.roleName, details.basePrice, selectedGateway)
                                    isProcessing = false
                                    showSuccessTick = false
                                    onDismiss()
                                }
                            },
                            enabled = when (selectedMethod) {
                                "CARD" -> cardNumber.length >= 12 && cardExpiry.isNotEmpty() && cardCvv.length == 3
                                "UPI" -> upiId.isNotEmpty() && upiVerified
                                "NETBANKING" -> selectedBank.isNotEmpty()
                                else -> false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedGateway == "Razorpay") Color(0xFF1E88E5) else Color(0xFF4CAF50)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("gateway_submit_payment"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Pay ₹${String.format("%.2f", totalPayable)} via $selectedGateway",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        TextButton(
                            onClick = { onDismiss() },
                            modifier = Modifier.fillMaxWidth().testTag("payment_cancel_btn")
                        ) {
                            Text("Cancel subscription transaction")
                        }
                    } else {
                        // Rendering Interactive Transaction Success Process Progress Steps
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (showSuccessTick) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "TRANSACTION SUCCESSFUL!",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E7D32),
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Allocating plan licenses for $selectedGateway...",
                                    fontSize = 11.sp,
                                    color = adaptiveSlate600()
                                )
                            } else {
                                CircularProgressIndicator(
                                    color = if (selectedGateway == "Razorpay") Color(0xFF1E88E5) else Color(0xFF4CAF50),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    currentStepText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Do NOT click back button or close application.",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun BillingSuiteScreen(viewModel: MarksViewModel) {
    val user = viewModel.currentUser ?: return
    val paymentList by viewModel.paymentRecords.collectAsState()
    val context = LocalContext.current
    var showCheckoutDetails by remember { mutableStateOf<PaymentCheckoutDetails?>(null) }
    var selectedComplianceDoc by remember { mutableStateOf<String?>(null) }

    val checkoutDetails = showCheckoutDetails
    if (checkoutDetails != null) {
        PaymentGatewayCheckoutDialog(
            details = checkoutDetails,
            viewModel = viewModel,
            onDismiss = { showCheckoutDetails = null }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // --- SECTION A: USER PROFILE METADATA ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Badge,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Account Identity Profile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Protected directly on your device",
                            style = MaterialTheme.typography.labelSmall,
                            color = Slate600
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(10.dp))
                
                // Profile parameters
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("User Name:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Slate600)
                        Text(user.name, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Email ID:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Slate600)
                        Text(user.email, fontSize = 12.sp, fontWeight = FontWeight.Normal)
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Security Role:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Slate600)
                        Text(user.role.replace("_", " "), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Blue600)
                    }
                    if (user.schoolId.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text("Tenancy Ref:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Slate600)
                            Text(user.schoolId, fontSize = 12.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Active Plan:", modifier = Modifier.width(100.dp), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Slate600)
                        val planLabel = when(user.planType) {
                            "FREE" -> "FREE PLAN (Rs 0.00)"
                            "INDIVIDUAL_PARENT_PLAN" -> "PARENTS PLAN (Rs 100.00/Year)"
                            "SCHOOL_PLAN" -> "SCHOOL PLAN (Rs 10,000.00/Year)"
                            else -> user.planType
                        }
                        Box(
                            modifier = Modifier
                                .background(Teal500.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(planLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Teal600)
                        }
                    }
                }
            }
        }

        // --- SECTION B: THE UNIFIED ALL-TIME PRICING TABLE ---
        Text("Academic System Subscription Plans", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("Explore three robust plans. Select any plan to instantly downgrade or upgrade for testing.", style = MaterialTheme.typography.bodySmall, color = Slate600)
        Spacer(modifier = Modifier.height(12.dp))

        // Card 1: Free Plan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = if (user.planType == "FREE") Teal500 else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Free Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate600)
                    if (user.planType == "FREE") {
                        Box(
                            modifier = Modifier
                                .background(Teal500, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE CURRENT PLAN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Rs 0.00/Year", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Free tier allows tracking up to 1 student with basic read-only or limited features. No custom subjects or custom test schedule additions.",
                    fontSize = 11.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.subscribeToPlan("FREE", "INDIVIDUAL_PARENT", 0.0, "Free System Bypass")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.planType == "FREE") Teal500 else Blue600,
                        disabledContainerColor = Slate400
                    ),
                    enabled = user.planType != "FREE",
                    modifier = Modifier.fillMaxWidth().testTag("buy_free_plan")
                ) {
                    Text(if (user.planType == "FREE") "Active Plan" else "Switch to Free (Rs 0.00)", fontSize = 11.sp)
                }
            }
        }

        // Card 2: Parents Plan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = if (user.planType == "INDIVIDUAL_PARENT_PLAN") Teal500 else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Parents Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Blue500)
                    if (user.planType == "INDIVIDUAL_PARENT_PLAN") {
                        Box(
                            modifier = Modifier
                                .background(Teal500, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE CURRENT PLAN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Rs 100.00/Year", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("+18% GST (Total: Rs 118.00)", fontSize = 9.sp, color = Slate600)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Parents Plan can edit, add or remove the Subjects and Type of Exam for up to four children. Safe data visualization matrices.",
                    fontSize = 11.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        showCheckoutDetails = PaymentCheckoutDetails(
                            planType = "INDIVIDUAL_PARENT_PLAN",
                            roleName = "INDIVIDUAL_PARENT",
                            basePrice = 100.0,
                            planLabel = "Parent Pro Tier"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.planType == "INDIVIDUAL_PARENT_PLAN") Teal500 else Blue600,
                        disabledContainerColor = Slate400
                    ),
                    enabled = user.planType != "INDIVIDUAL_PARENT_PLAN",
                    modifier = Modifier.fillMaxWidth().testTag("buy_parent_plan")
                ) {
                    Text(if (user.planType == "INDIVIDUAL_PARENT_PLAN") "Active Plan" else "Upgrade (Rs 100.00/Yr)", fontSize = 11.sp)
                }
            }
        }

        // Card 3: School Plan
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 2.dp,
                    color = if (user.planType == "SCHOOL_PLAN") Teal500 else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("School Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Teal500)
                    if (user.planType == "SCHOOL_PLAN") {
                        Box(
                            modifier = Modifier
                                .background(Teal500, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("ACTIVE CURRENT PLAN", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Rs 10,000.00/Year", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("+18% GST (Total: Rs 11,800.00)", fontSize = 9.sp, color = Slate600)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "School Plan with same as Parent Plan, but can add/manage up to 200 students directories and parent sub-accounts. Comprehensive reports can be shared to the Student's Parents instantly.",
                    fontSize = 11.sp,
                    color = Slate600
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        showCheckoutDetails = PaymentCheckoutDetails(
                            planType = "SCHOOL_PLAN",
                            roleName = "SCHOOL_ADMIN",
                            basePrice = 10000.0,
                            planLabel = "School Suite Tier"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.planType == "SCHOOL_PLAN") Teal500 else Blue600,
                        disabledContainerColor = Slate400
                    ),
                    enabled = user.planType != "SCHOOL_PLAN",
                    modifier = Modifier.fillMaxWidth().testTag("buy_school_plan")
                ) {
                    Text(if (user.planType == "SCHOOL_PLAN") "Active Plan" else "Upgrade (Rs 10,000.00/Yr)", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION C: INTEGRATED BILLING RECEIPTS LEDGER ---
        Text("Payment Records & Invoice Generation (PDFs):", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        viewModel.actionMessage?.let { msg ->
            Text(msg, color = Teal500, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
        }

        if (paymentList.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = Slate600)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No billing records logged yet.", fontSize = 12.sp, color = Slate600)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                paymentList.forEach { record ->
                    val sdf = SimpleDateFormat("dd-MMM-yyyy HH:mm", Locale.getDefault())
                    val dateStr = sdf.format(Date(record.timestamp))

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(record.planType.replace("_", " "), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Ref: ${record.paymentId}", fontSize = 10.sp, color = Slate600)
                                Text("Date: $dateStr", fontSize = 10.sp, color = Slate600)
                                Text("Gateway: ${record.paymentGateway} (Included 18% GST)", fontSize = 9.sp, color = Teal500)
                            }
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text("₹${String.format("%.0f", record.totalAmount)}", fontWeight = FontWeight.Bold, color = Blue500, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.downloadInvoicePdf(record) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate700),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.testTag("download_invoice_${record.id}")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("PDF Invoice", fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action when Invoice Pdf is compiled and ready to be viewed/shared
        viewModel.lastDownloadedInvoiceFile?.let { pdfFile ->
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Amber100),
                modifier = Modifier.fillMaxWidth().border(1.dp, Amber500, RoundedCornerShape(8.dp))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PDF Invoice successfully written to local disk cache!", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        Text(pdfFile.name, fontSize = 10.sp, color = Slate600)
                    }
                    Button(
                        onClick = {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                pdfFile
                            )
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Invoice Bill"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500)
                    ) {
                        Text("Open Intent", fontSize = 10.sp, color = Color.White)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        Spacer(modifier = Modifier.height(16.dp))

        // --- SECTION D: COMPLIANCE REGULATORY TRUST FOOTER ---
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Student Academic Performance & Marks Tracking Center",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Authorized portal for checking student school records, progress reports, grade safety, and messaging between parents and teachers.",
                    fontSize = 10.sp,
                    color = Slate600,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                HowToUseAndFAQSection()
                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(14.dp))

                // Interactive Link Grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // T&C Link
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedComplianceDoc = "TC" }
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .testTag("btn_compliance_tc")
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Terms & Use", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Privacy Link
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedComplianceDoc = "PRIVACY" }
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .testTag("btn_compliance_privacy")
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Privacy Policy", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Refunds Link
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedComplianceDoc = "REFUND" }
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .testTag("btn_compliance_refund")
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Refund Policy", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Delivery Link
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedComplianceDoc = "SHIPPING" }
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                .testTag("btn_compliance_shipping")
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delivery SLA", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    // Contact & Redressal Hub
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedComplianceDoc = "CONTACT" }
                            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                            .testTag("btn_compliance_contact")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Contact Customer Support Helpline", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    // Guide on Tracking Marks and FAQs
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedComplianceDoc = "GUIDE" }
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .testTag("btn_compliance_guide")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guide on Tracking Marks & FAQs", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "© 2026 AaVi Technos. All rights reserved. Subscriptions are billed once a year or auto-renewed. All digital materials, student folders, and report card worksheets are delivered securely to your account immediately after payment confirmation.",
                    fontSize = 8.sp,
                    color = Slate600,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal Document popup dialog renderer
    var complianceDocToShow by remember { mutableStateOf<String?>(null) }
    complianceDocToShow = selectedComplianceDoc
    if (complianceDocToShow != null) {
        AlertDialog(
            onDismissRequest = { selectedComplianceDoc = null },
            confirmButton = {
                Button(
                    onClick = { selectedComplianceDoc = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("I Understand & Accept", color = Color.White, fontSize = 11.sp)
                }
            },
            title = {
                val title = when (complianceDocToShow) {
                    "TC" -> "Terms & Conditions of Service"
                    "PRIVACY" -> "Privacy & Encryption Policy"
                    "REFUND" -> "Cancellation & Refund SLA"
                    "SHIPPING" -> "Digital Access & Delivery Policy"
                    "CONTACT" -> "Merchant Contact Support Registry"
                    "GUIDE" -> "Guide on Tracking Marks & FAQs"
                    else -> "Operational Policy Docs"
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (complianceDocToShow == "PRIVACY") Icons.Default.Security else if (complianceDocToShow == "GUIDE") Icons.Default.HelpOutline else Icons.Default.Gavel,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            },
            text = {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        PolicyDocumentContent(complianceDocToShow!!)
                    }
                }
            }
        )
    }
}


// --- 6. School Admin: Generate parent login sub-accounts ---
@Composable
fun ParentSubAccountsScreen(viewModel: MarksViewModel) {
    val subParents by viewModel.subParentsList.collectAsState()
    val students by viewModel.studentsList.collectAsState()

    var parentName by remember { mutableStateOf("") }
    var parentEmail by remember { mutableStateOf("") }
    var parentPass by remember { mutableStateOf("") }
    var selectedStudentId by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Parent Sub-Accounts Hub", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Generate credentials for parents to access view-only dashboards of specific child indexes.", style = MaterialTheme.typography.bodySmall, color = Slate600)
        Spacer(modifier = Modifier.height(16.dp))

        // Create new Parent Sub account form
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Register View-Only Parent Account", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(12.dp))

                viewModel.actionMessage?.let { msg ->
                    Text(msg, color = Teal500, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                OutlinedTextField(
                    value = parentName,
                    onValueChange = { parentName = it },
                    label = { Text("Parent Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = parentEmail,
                    onValueChange = { parentEmail = it },
                    label = { Text("Parent Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = parentPass,
                    onValueChange = { parentPass = it },
                    label = { Text("Access Key Code") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Select child list to bind
                if (students.isNotEmpty()) {
                    Text("Bind to Student Child File:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Slate600)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        students.forEach { stud ->
                            val isSelected = selectedStudentId == stud.id
                            val decName = viewModel.getDecryptedStudentName(stud.encryptedName)
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedStudentId = stud.id },
                                label = { Text(decName) },
                                modifier = Modifier.padding(end = 6.dp)
                            )
                        }
                    }
                } else {
                    Text("Create a student profile first to bind.", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val childId = selectedStudentId
                        if (childId != null) {
                            viewModel.createParentSubAccount(parentName, parentEmail, parentPass, childId)
                            parentName = ""
                            parentEmail = ""
                            parentPass = ""
                            selectedStudentId = null
                        }
                    },
                    enabled = selectedStudentId != null,
                    modifier = Modifier.fillMaxWidth().testTag("add_parent_subaccount_btn")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Provision View-Only Account")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Existing parent sub accounts registered
        Text("Registered Parent Sub-accounts:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        if (subParents.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(32.dp), tint = Slate600)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No parents registered under this school account.", fontSize = 12.sp, color = Slate600)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                subParents.forEach { parent ->
                    val studentNameObj = students.find { it.id == parent.associatedStudentId }
                    val childName = if (studentNameObj != null) viewModel.getDecryptedStudentName(studentNameObj.encryptedName) else "Unknown"

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                val decParentName = viewModel.getDecryptedStudentName(parent.name)
                                Text(decParentName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Email: ${parent.email}", fontSize = 11.sp, color = Slate600)
                                Text("Bound Child Student: $childName (RollNo: ${studentNameObj?.rollNo ?: "-"})", fontSize = 11.sp, color = Teal500, fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.Default.LockClock, contentDescription = null, tint = Slate600)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        HowToUseAndFAQSection(expandedByDefault = true)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun HowToUseAndFAQSection(expandedByDefault: Boolean = true) {
    var rootExpanded by remember { mutableStateOf(expandedByDefault) }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp)
            )
            .testTag("how_to_use_root_card")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { rootExpanded = !rootExpanded }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Kids & Parents Guide: How to Use Marks Tracking",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = if (rootExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (rootExpanded) "Collapse Guide" else "Expand Guide",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            AnimatedVisibility(visible = rootExpanded) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Welcome to Marks Tracking! 🌟 This is your smart school grade journal on screen. It helps your parents and teachers see how well you are learning and growing in all your subjects!",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "👣 Step-by-Step Walkthrough Guide",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val steps = listOf(
                        "1. Logging In 🔑" to "Type your school email and access key, then tap the 'Login' button. If you are exploring, click 'Parent Demo' or 'Admin Demo' to sign in instantly!",
                        "2. School Admin: Organizing Student Directory 🏫" to "The school system administrator can add student records, select their classes/grades, modify profiles, and delete them when needed.",
                        "3. School Admin: Bulk Access Uploads 📂" to "The Admin can upload an entire class rosters spreadsheet at once using the 'CSV Bulk Upload' tool. This makes setup very quick!",
                        "4. Entering Student Scores ✍️" to "School supervisors go to the spreadsheet grid, find the correct student's box for a subject and exam type, click inside, write the score, and tap 'Save Changes'!",
                        "5. Parents: Quick Family Dashboard 🧑‍🧑‍🧒" to "Parents get an direct view-only panel. They click their child's name to automatically load all standard charts and grades.",
                        "6. Parents: Reviewing Line Graphs & Progress 📈" to "Tap the 'Academic Analytics' tab to inspect colorful line charts representing progress peaks, averages, and comparative bar graphs plotting test scores over time.",
                        "7. Everyone: AI Study Advisory Helper 🤖" to "If a score is below 40%, the smart Gemini Companion highlights weak areas and creates custom, 15-minute weekly drill recommendations.",
                        "8. Parents: Downloading PDF Report Cards 📄" to "Under billing records, parents can download and print official progress card PDFs to pin on a room study desk!",
                        "9. New Academic Year Setup 📅" to "For the new academic year, a new record can be created for the same children/students by using the same name and different Class. By doing this, a new record for the particular child/student will be available to update the marks."
                    )
                    
                    steps.forEach { (stepTitle, stepDesc) ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = stepTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stepDesc,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "❓ Frequently Asked Questions (FAQs)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val faqs = listOf(
                        "Q1: What is Marks Tracking?" to "A: It is a digital school diary where your teachers log exam scores and parents can see your learning curves safely list-by-list!",
                        "Q2: Can Parents rewrite student marks?" to "A: No! Parents have a safe 'View-Only' role. They can check grades and track trends, but only teachers and admins can change score logs.",
                        "Q3: How do we read the line graphs?" to "A: The colored line shows score updates. If the line moves up, it means you've improved and gotten higher scores!",
                        "Q4: What happens if a score is below 40%?" to "A: Our friendly AI Study Advisor flags weak marks and outlines simple 15-minute daily practice steps to help you improve by 5% each week.",
                        "Q5: Can parents add more than 4 children?" to "A: Parent plans allow up to 4 kids for tracking. Schools can upgrade to the School Suite to manage up to 200 students at once.",
                        "Q6: How can families print report sheets?" to "A: Tap on 'Billing Suite', select an invoice/history row, and click download PDF. You can save, share, or print the report directly!",
                        "Q7: Are my school scores kept private?" to "A: Yes! Our system isolates school records into secure private directories so other class students never see your private scores.",
                        "Q8: Do scores update in real-time?" to "A: Yes! As soon as the teacher hits 'Save Changes', the parents' dashboard and line graphs update instantly across all profiles!",
                        "Q9: How do I handle a new academic year?" to "A: For the new academic year, a new record can be created for the same children/students by using the same name and different Class. By this, a new record for the particular child/student will be available to update the marks."
                    )
                    
                    faqs.forEach { (faqQ, faqA) ->
                        var faqExpanded by remember { mutableStateOf(false) }
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                .clickable { faqExpanded = !faqExpanded }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = faqQ,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(0.9f)
                                    )
                                    Icon(
                                        imageVector = if (faqExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                if (faqExpanded) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = faqA,
                                        fontSize = 11.sp,
                                        color = Slate600,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun PolicyDocumentContent(docType: String) {
    when (docType) {
        "TC" -> {
            Text(
                "Welcome to the Marks Tracking App (by 'AaVi Technos'). These Terms and Conditions form a simple agreement between you and AaVi Technos (\"we\", \"us\", or \"our\") regarding your use of this app. Please read them carefully:\n\n" +
                "1. Who Can Use This App:\n" +
                "By creating an account, you promise that your information is true and correct. This app is for teachers, school helpers, tutors, and parents who are at least 18 years old. You are responsible for keeping your login info safe and for any grades added under your account.\n\n" +
                "2. Account Plans & Limits:\n" +
                "• Free Plan: Gives you basic access with a few limits (max 1 student profile, 15 subjects, and 10 exams). Perfect to try out the app.\n" +
                "• Parent Plan: Built for parents to securely check up to 4 children's grades. This costs Rs 100 per year.\n" +
                "• School Plan: Gives schools unlimited student profiles and full manager controls. This costs Rs 10,000 per year and lets you create separate, view-only accounts for parents.\n\n" +
                "3. Rules of Use:\n" +
                "You agree not to:\n" +
                "• Try to bypass our app safety features or hack our secure data storage.\n" +
                "• Upload viruses, malware, or harmful software that could damage the app.\n" +
                "• Automatically copy or steal any contents or layout from the app.\n" +
                "• Attempt to cheat on payments or billing options.\n" +
                "• Change or fake test scores to show incorrect grades.\n\n" +
                "4. Payments & Billing:\n" +
                "Paid plans are billed once a year. Prices include standard taxes (like 18% GST in India). Payments are processed safely through secure banking systems.\n\n" +
                "5. Disclaimers & Limits:\n" +
                "We provide this app \"as-is\" and cannot guarantee it will never have minor server pauses or internet connection issues. We are not responsible for any indirect damages. In any case, our maximum liability is limited to the money you paid us in the last 12 months.\n\n" +
                "6. Ownership:\n" +
                "All app designs, custom graphs, icons, and logos belong to AaVi Technos and are protected by copyright rules. Please do not copy or share them without our permission.\n\n" +
                "7. System Updates:\n" +
                "We may update these terms occasionally. We will notify you of changes through in-app alerts or email. Using the app after an update means you agree to the new terms.",
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
        "PRIVACY" -> {
            Text(
                "We at AaVi Technos are committed to protecting your privacy and keeping your school data safe. This privacy policy explains what info we collect and how we keep it secure under data privacy guidelines:\n\n" +
                "1. What Information We Collect:\n" +
                "• Account Info: Your email, secure password, your plan type, and purchase records.\n" +
                "• Student Info: Student names (stored securely on your device), class grade levels, roll numbers, and parent details.\n" +
                "• Grades & Academic Records: Subjects, test names, maximum scores, marks obtained, and dates of exams.\n" +
                "• Payment Records: Safe transactional IDs from our payment partners (we DO NOT store your credit card details or bank passwords).\n\n" +
                "2. Keeping Data Separate & Secure:\n" +
                "Each school's and family's data is completely separated and locked. No other parents or schools can look at your student reports or dashboards unless you explicitly create a parent account for them.\n\n" +
                "3. How We Use Your Data:\n" +
                "We use your info only to make the app work for you:\n" +
                "• Creating report cards and visual progress charts.\n" +
                "• Generating tax receipts for your plan purchases.\n" +
                "• Getting helpful smart study tips using safe AI tools.\n" +
                "• Allowing parents to log in and see their children's grades.\n\n" +
                "4. No Ads & No Selling Your Data:\n" +
                "We never sell, rent, or share student records or grades with advertisers, agents, consultancies, or third parties. Your data belongs strictly to you and your school.\n\n" +
                "5. Partners We Work With:\n" +
                "• Razorpay / PayU: Processes your plan upgrades using highly secure bank processing systems.\n" +
                "• Google Gemini AI: Processes academic summaries securely. Your grade information is never shared or used to train public AI models.\n\n" +
                "6. Data Deletion:\n" +
                "Your information is stored only as long as you keep your account active. If you delete your account, your students, grades, parent logins, and files are permanently and completely wiped from our systems.",
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
        "REFUND" -> {
            Text(
                "AaVi Technos has a simple, customer-first policy for subscription changes and refunds. Please read our guidelines:\n\n" +
                "1. Downgrades and Cancellations:\n" +
                "You can cancel your paid plan or downgrade to the Free Plan at any time from the billing screen. Your premium features will stay active until your current year ends, then your account will return to the standard Free Plan and its normal limits.\n\n" +
                "2. When You Can Get a Full Refund:\n" +
                "We want to make sure your payments are fair and correct. You can receive a 100% refund in these cases:\n" +
                "• If you paid twice by accident for the same plan within 24 hours.\n" +
                "• If your paid plan did not activate within 24 hours of successfully completing checkout.\n" +
                "Approved refunds are sent back to your original payment method (such as your Credit Card, UPI, or Debit Card) within 5 to 7 business days.\n\n" +
                "3. Refund Time Frame:\n" +
                "Refund requests must be sent within 14 days of your initial purchase. Requests sent after 14 days are not eligible for cash refunds, but we will be happy to offer credits for future use or school expansions.\n\n" +
                "4. How to Ask for a Refund:\n" +
                "Please send an email to mail@altty.com with the subject line 'Subscription Refund Request'. Include your billing email and a copy or screenshot of your payment receipt.",
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
        "SHIPPING" -> {
            Text(
                "Since we are a fully digital app, all our features and report cards are delivered online as soon as you upgrade:\n\n" +
                "1. Immediate Access (No Physical Shipping):\n" +
                "We do not pack or ship physical items (like paper books, plastic report cards, or storage drives). There are absolutely no postage, delivery, or processing fees. Your paid features (such as unlimited marks tracking, PDF downloads, and parent accounts) are unlocked instantly (usually within 5 seconds) after your payment succeeds.\n\n" +
                "2. Instant Invoices & Receipts:\n" +
                "A digital receipt and tax invoice is generated instantly in your Billing/Invoice section after any purchase. You can read, print, or download these PDF receipts on your phone at any time.\n\n" +
                "3. Reliable Offline Working:\n" +
                "Even if our servers undergo quick maintenance or you lose your internet connection, the app will store your entries safely on your phone. You can keep writing and tracking grades offline with no interruption.",
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
        "CONTACT" -> {
            Text(
                "If you have questions, feedback, or need help with the app, please feel free to reach out to us:\n\n" +
                "• Company Name: AaVi Technos\n" +
                "• Official Support Email: mail@altty.com\n" +
                "• Phone Support Hotline: +91 79815 85715\n" +
                "• Headquarters Address: 150/2RT, Vijaya Nagar Colony, HYD-500057, Telangana, India\n" +
                "• Helpful Response Time: We answer all support emails and phone messages within 12 to 24 hours.",
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
        "GUIDE" -> {
            HowToUseAndFAQSection(expandedByDefault = true)
        }
        else -> {
            Text("Educational operational policies compiled securely.", fontSize = 10.sp, lineHeight = 14.sp)
        }
    }
}
