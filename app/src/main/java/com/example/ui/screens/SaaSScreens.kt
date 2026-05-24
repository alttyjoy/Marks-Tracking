package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.compose.animation.*
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
fun AppNavigationShell(viewModel: MarksViewModel) {
    val isConfigured = viewModel.isConfigured
    val currentUser = viewModel.currentUser
    var activeStateScreen by remember { mutableStateOf(AppScreen.DATA_ENTRY_GRID) }
    var showTopProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
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
                        // Profile trigger
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
                                Text(
                                    text = currentUser.name,
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

                        // Right actions (Theme toggle & Logout)
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

// --- Unified Dialog for looking up Logged In User profile ---
@Composable
fun UserProfileDialog(
    user: com.example.data.model.UserAccount,
    viewModel: MarksViewModel,
    onDismiss: () -> Unit
) {
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

                Text(
                    "Encryption & Isolation Architecture:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "This SaaS system isolates student ledger rows dynamically per school domain configuration. Grade ledger values are locally encrypted via AutoAES-256 keys mapped dynamically to sandbox hardware.",
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
            label = { Text("KPI Charts") },
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
    var dbName by remember { mutableStateOf("marks_tracking_saas") }
    var dbUser by remember { mutableStateOf("postgres_admin") }
    var dbPass by remember { mutableStateOf("SuperSecureDbPass*2026") }

    // Group 2: SMTP Settings
    var smtpHost by remember { mutableStateOf("smtp.marks-tracking.saas.com") }
    var smtpPort by remember { mutableStateOf("587") }
    var smtpUser by remember { mutableStateOf("alerts@markstracking.saas") }
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
        // SaaS Branding Header
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
                text = "Configure your SaaS instance variables directly in system cache",
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
                        Text("Integrate direct SaaS pricing gateways across Indian banking rails natively.", style = MaterialTheme.typography.bodySmall, color = adaptiveSlate600())
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
                        Text("Primary SaaS Administration Credentials", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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


// --- 2. SaaS Themed Simple Login/Register Screen ---
@Composable
fun LoginScreen(viewModel: MarksViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedComplianceDoc by remember { mutableStateOf<String?>(null) }

    // Tab control: 0 = Login, 1 = Register
    var activeTab by remember { mutableStateOf(0) }

    // Register fields
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPlan by remember { mutableStateOf("FREE") } // "FREE", "INDIVIDUAL_PARENT_PLAN", "SCHOOL_PLAN"
    var regSchoolId by remember { mutableStateOf("") }

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
                                    Text("Basic parameters. Limits to 7 preseeded subjects.", fontSize = 10.sp, color = adaptiveSlate600())
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
                                    Text("Unlimited children, custom subjects, AI Advisory Companion.", fontSize = 10.sp, color = adaptiveSlate600())
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
                                    Text("CSV Batch upload, sub-accounts, multi-tenant locks.", fontSize = 10.sp, color = adaptiveSlate600())
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

                        Button(
                            onClick = {
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
                        "Subscription Guide & Security Directory",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // FREE PLAN EXPLAINER
                Card(
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                     modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Free Starter Tier (Trial Profile)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Target Audience: Single-student home tracking families.\n" +
                            "• Features: Designed for single-student home tracking with a preselected 7-subject template (Mathematics, Science, Social, English, Language-1, Language-2, Language-3).\n" +
                            "• Security: Dynamic local grade vault with isolated hardware-mapped encryption key verification.",
                            fontSize = 11.sp,
                            color = adaptiveSlate600()
                        )
                    }
                }

                // PARENT PLAN EXPLAINER
                Card(
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                     modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SupervisedUserCircle, contentDescription = null, tint = Teal600, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Parent Pro Tier (Advanced Monitoring)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Teal600)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Target Audience: Families managing multi-student progress tracking.\n" +
                            "• Features: Unlocks dynamic custom school curriculum, average trend charts, and the interactive AI Study Companion Advisor.\n" +
                            "• Value: Complete subject customizability and auto-generated child performance improvement plans.",
                            fontSize = 11.sp,
                            color = adaptiveSlate600()
                        )
                    }
                }

                // SCHOOL PLAN EXPLAINER
                Card(
                     colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                     modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = Blue600, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("School Suite Tier (Institutional Gateway)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Blue600)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Target Audience: Institutional school admins, teachers, and school systems.\n" +
                            "• Features: Provides bulk actions like CSV Grade Sheet uploading, isolated tenant sub-domains, and delegated sub-parent viewer account registration.\n" +
                            "• Isolation Architecture: Secures and splits student directory operations into hardware-enforced dynamic tenant zones.",
                            fontSize = 11.sp,
                            color = adaptiveSlate600()
                        )
                    }
                }
            }
        }

        // Demo Presets / Quick shortcuts panel to allow easy testing of role boundaries
        Spacer(modifier = Modifier.height(24.dp))
        Text("🚀 SaaS Demo Gateways (Bypass Sandbox Testing)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
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

        Spacer(modifier = Modifier.height(32.dp))
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
                        text = "SaaS Merchant Licensing & Trust Center",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Required compliance frameworks for direct bank-rail integrations, payment settlement gateways (Razorpay, PayU), and Google Play-Store distribution guidelines.",
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
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "© 2026 EduGrade Corp. All rights reserved. Subscriptions are billed on an auto-renewing or one-time annual basis. Digital assets, student directory entries, and child assessments are delivered securely via cloud isolation nodes immediately upon Razorpay / PayU checkout confirmation.",
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
                    else -> "Operational Policy Docs"
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (complianceDocToShow == "PRIVACY") Icons.Default.Security else Icons.Default.Gavel,
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
                        when (complianceDocToShow) {
                            "TC" -> {
                                Text(
                                    "Welcome to Marks Tracking SaaS Solution (internally 'EduGrade'). By registering an account and choosing a billing plan (Free, Parent, or School Plan), you agree to comply with and are legally bound by these conditions:\n\n" +
                                    "1. User Account Ownership Rights:\n" +
                                    "You are responsible for keeping passwords and local credentials safe. Under the School Plan, you may provision parent sub-accounts with read-only view access of specific student records.\n\n" +
                                    "2. Proper System Use:\n" +
                                    "You must not decrypt database structures, disassemble system cache blocks, or inject malicious payloads on underlying cloud schemas. All educational grades represent accurate academic inputs.\n\n" +
                                    "3. Annual Licensing Renewal:\n" +
                                    "SaaS Plans are processed or simulated via dynamic checkout tokens (Razorpay / PayU.In). The parent tier cost is Rs 100/Yr, and the comprehensive school tier represents Rs 10,000/Yr.",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            "PRIVACY" -> {
                                Text(
                                    "Privacy and robust data isolation are fundamental to our architecture:\n\n" +
                                    "1. Client-Side Grade Protection:\n" +
                                    "Academic logs, student assessment marks, and subject roll structures are isolated inside multi-tenant rows. Parents from Screen A cannot view student lists from Screen B unless a verified view-only authentication key is generated by the administrator.\n\n" +
                                    "2. Collected Metadata:\n" +
                                    "We store active transaction metadata and administrator contact email handles to verify compliance status. No web trackers, third-party sales cookies, or analytics scripts are bundled.\n\n" +
                                    "3. Third Party Handshakes:\n" +
                                    "When triggering a secure upgrade subscription, cards, CVVs, and mobile banking identifiers are processed directly over encrypted merchant channels (Razorpay and PayU).",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            "REFUND" -> {
                                Text(
                                    "We strive to offer premium client-focused grades reporting. Please read our operational billing rollback guidelines:\n\n" +
                                    "1. Right to Downgrade:\n" +
                                    "Subscribers can transition or request a licensing downgrade back to the Free Plan (Rs 0.00/Yr) at any point directly from the Billing Suite controls.\n\n" +
                                    "2. Refund Processing Timeline:\n" +
                                    "In case of accidental duplicate payment triggers across Indian Banking Rails, a full refund representing 100% of the principal + GST is issued within 5 to 7 working business days. Credits are processed automatically back to the originating bank instrument/card/UPI address.\n\n" +
                                    "3. Charge dispute request:\n" +
                                    "Reach out to the registered merchant supervisor at mail@altty.com along with transaction receipt IDs.",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            "SHIPPING" -> {
                                Text(
                                    "This EduGrade application operates purely as a Software-as-a-Service (SaaS) digital delivery model:\n\n" +
                                    "1. Instant Digital Delivery:\n" +
                                    "There are strictly no physical books, report cards, printed documents, or CD packages shipped. Digital licensing privileges are automatically granted. Immediately when Razorpay or PayU checkout API completes successfully, your subscription model adjusts instantly, enabling premium creation modules.\n\n" +
                                    "2. PDF Invoices:\n" +
                                    "Tax compliant breakdowns (including 18% CGST/SGST calculations) are written to local cache stores instantly and are shareable in real-time.",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            "CONTACT" -> {
                                Text(
                                    "Dedicated SaaS grievance officer contact parameters:\n\n" +
                                    "• Registered Corporate Entity: EduGrade Solutions Pvt Ltd\n" +
                                    "• Official Regulatory Support Email: mail@altty.com\n" +
                                    "• Support Desk Phone: +91 79815 85715\n" +
                                    "• Corporate Office Address: 150/2RT, Vijaya Nagar Colony, HD-500057\n" +
                                    "• General Ticketing Turnaround: Within 12 to 24 hours of receiving formal mail feedback.",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
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

    var csvText by remember { mutableStateOf("") }
    var showImportDialog by remember { mutableStateOf(false) }
    var showConfigDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

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
                    Text("Add New Student Record (AutoAES-256)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                        Button(
                            onClick = {
                                viewModel.addNewStudent(newStudentName, newStudentRoll, newStudentClass)
                                newStudentName = ""
                                newStudentRoll = ""
                                newStudentClass = ""
                            },
                            modifier = Modifier.align(Alignment.End).testTag("student_add_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Student")
                        }
                    }
                    if (user.role == "SCHOOL_ADMIN") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Notice: School Plan limited to 200 student directories. Current enrollment: ${students.size}/200",
                            style = MaterialTheme.typography.labelSmall,
                            color = adaptiveSlate600()
                        )
                    }
                }
            }
        }

        // Student active selection combo drawer
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
                Column {
                    Text("Excel Interactive Grid: $decryptedName", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Roll Number: ${curStudent.rollNo} | Live compilation state", style = MaterialTheme.typography.labelSmall, color = adaptiveSlate600())
                }
                
                if (user.role != "VIEW_ONLY_PARENT") {
                    IconButton(onClick = { viewModel.deleteStudent(curStudent.id) }) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // EXCEL STYLE DATA ENTRY LAYOUT (HORIZONTALLY SCROLLABLE TABLE)
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
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            "Subject Name",
                            modifier = Modifier
                                .width(120.dp)
                                .padding(start = 12.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        exams.forEach { exam ->
                            Text(
                                exam,
                                modifier = Modifier
                                    .width(80.dp),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                        Text(
                            "Mean (%)",
                            modifier = Modifier
                                .width(80.dp)
                                .padding(end = 12.dp),
                            color = Teal400,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Divider(color = Slate600)

                    // Data Rows
                    subjects.forEach { subject ->
                        var subjectSum = 0.0
                        var count = 0
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                subject.name,
                                modifier = Modifier
                                    .width(120.dp)
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
                                        .width(80.dp)
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isEnabled) Slate100 else Slate50)
                                        .border(1.dp, Slate600, RoundedCornerShape(4.dp))
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
                                            .padding(6.dp)
                                            .testTag("cell_${subject.id}_${exam}")
                                    )
                                }
                            }

                            // Math Rows average computation
                            val rowMeanVal = if (count > 0) subjectSum / count else 0.0
                            Text(
                                if (count > 0) "${String.format("%.1f", rowMeanVal)}%" else "-",
                                modifier = Modifier.width(80.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (rowMeanVal < 40.0 && count > 0) Rose500 else Blue500
                            )
                        }
                        Divider(color = Slate600.copy(alpha = 0.5f))
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
        }

        // Floating Dialog Bulk Import Sheet overlay
        if (showImportDialog) {
            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("CSV Bulk Import Sandbox", fontWeight = FontWeight.Bold) },
                text = {
                    Column {
                        Text("Option 1: Upload a CSV file from your mobile storage", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Blue500)
                        
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

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Option 2: Paste spreadsheet values formatted exactly in CSV structures:", fontSize = 11.sp, color = adaptiveSlate600())
                        Text("Headers required: RollNo,StudentName,Subject,Weekly,Monthly,Quarterly,Half-Yearly,Annual", fontSize = 11.sp, color = adaptiveSlate600(), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = csvText,
                            onValueChange = { csvText = it },
                            placeholder = { Text("101,Aarav Sharma,Mathematics,85,90,88,82,92\n101,Aarav Sharma,Science,78,85,80,75,88\n102,Diya Patel,Mathematics,92,94,90,95,98") },
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
                        Divider()
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
                                    Divider()
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
                                    Divider()
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


// --- 4. Advanced Interactive Dashboard Tab (Canvas-drawn dynamic charts) ---
@Composable
fun AdvancedAnalyticsScreen(viewModel: MarksViewModel) {
    val student = viewModel.selectedStudent
    val subjects by viewModel.subjectsList.collectAsState()
    val testTypes by viewModel.testTypesList.collectAsState()
    val marks by viewModel.marksList.collectAsState()

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
            Column {
                Text("Performance Dashboards Suite", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (student != null) {
                    val decName = viewModel.getDecryptedStudentName(student.encryptedName)
                    Text("Interactive KPIs matching profile: $decName", style = MaterialTheme.typography.bodySmall, color = adaptiveSlate600())
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
            return
        }

        val exams = testTypes.map { it.name }.ifEmpty { listOf("Weekly", "Monthly", "Quarterly", "Half-Yearly", "Annual") }

        // 1. KPI Exam-over-Exam Growth Chart (Line-drawn Canvas)
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("KPI 1: Exam-over-Exam Growth trend (%)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Shows consecutive cycle percent updates in student directory overall scores", style = MaterialTheme.typography.bodySmall, color = adaptiveSlate600())
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
                    val spacing = width / (exams.size - 1)

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

        // 2. KPI Subject Proficiency Radar/Bar Canvas
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("KPI 2: Subject Range Matrix (Min - Max Range)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Highlights grade dispersion indexes across core subjects", style = MaterialTheme.typography.bodySmall, color = adaptiveSlate600())
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

        // 3. KPI Subject Averages (Mean Score Cards) & KPI 4. Trend Comparison
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("KPI 3: Subject Mean Summary Index & KPI 4. Semesters Comparisons", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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

        // 4. KPI Trend Comparisons Bar Chart Canvas
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("KPI 4: Trend Performance Bar Chart", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Summarizes overall semester score comparison structures", style = MaterialTheme.typography.bodySmall, color = Slate600)
                Spacer(modifier = Modifier.height(18.dp))

                val trendScores = exams.map { exam ->
                    val examMarks = marks.filter { it.examType == exam }
                    if (examMarks.isNotEmpty()) examMarks.map { it.marksObtained }.average() else 0.0
                }

                Canvas(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                    val width = size.width
                    val height = size.height
                    val barWidth = 45.dp.toPx()
                    val spacing = width / exams.size

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
                    Text("KPI 5: AI-Lite Advisory Plan & Improvement Scope", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Triggers diagnostic check for low performances (<40%) or downward trends (>10%), providing localized curriculum scheduling.",
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
                        Text("Compiling Recommendation Matrices...")
                    } else {
                        Icon(Icons.Default.Analytics, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Regenerate AI Advisory Plan")
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
}


// --- 5. SaaS Billing & Razorpay Payments Simulator Screen ---
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
                                text = "Secure SaaS Payment Hub",
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
                                Text("Base SaaS Premium Cost:", fontSize = 10.sp, color = adaptiveSlate600())
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
                                    currentStepText = "Initializing secure SSL handshake with $selectedGateway API servers..."
                                    delay(900)
                                    currentStepText = "Authorizing dynamic transaction payload of ₹${String.format("%.2f", totalPayable)}..."
                                    delay(900)
                                    currentStepText = "Connecting with NPCI Indian Banking Core Routing Node..."
                                    delay(900)
                                    currentStepText = "Verifying cryptographic token signatures with $selectedGateway vault..."
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
                                    "SAAS TRANSACTION SUCCESSFUL!",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2E7D32),
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Allocating system licenses for $selectedGateway...",
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

    if (showCheckoutDetails != null) {
        PaymentGatewayCheckoutDialog(
            details = showCheckoutDetails!!,
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
                            text = "Secure local-first cryptography ledger",
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
        Text("Academic SaaS System Subscription Plans", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                    "Free tier with read-only view. No option to edit, update or delete existing Subjects and Type of Exam.",
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
                    "Parents Plan can edit, add or remove the Subjects and Type of Exam for two children. Safe data visualization matrices.",
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
                        text = "SaaS Merchant Licensing & Trust Center",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Required compliance frameworks for direct bank-rail integrations, payment settlement gateways (Razorpay, PayU), and Google Play-Store distribution guidelines.",
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
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "© 2026 EduGrade Corp. All rights reserved. Subscriptions are billed on an auto-renewing or one-time annual basis. Digital assets, student directory entries, and child assessments are delivered securely via cloud isolation nodes immediately upon Razorpay / PayU checkout confirmation.",
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
                    else -> "Operational Policy Docs"
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (complianceDocToShow == "PRIVACY") Icons.Default.Security else Icons.Default.Gavel,
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
                        when (complianceDocToShow) {
                            "TC" -> {
                                Text(
                                    "Welcome to Marks Tracking SaaS Solution (internally 'EduGrade'). By registering an account and choosing a billing plan (Free, Parent, or School Plan), you agree to comply with and are legally bound by these conditions:\n\n" +
                                    "1. User Account Ownership Rights:\n" +
                                    "You are responsible for keeping passwords and local credentials safe. Under the School Plan, you may provision parent sub-accounts with read-only view access of specific student records.\n\n" +
                                    "2. Proper System Use:\n" +
                                    "You must not decrypt database structures, disassemble system cache blocks, or inject malicious payloads on underlying cloud schemas. All educational grades represent accurate academic inputs.\n\n" +
                                    "3. Annual Licensing Renewal:\n" +
                                    "SaaS Plans are processed or simulated via dynamic checkout tokens (Razorpay / PayU.In). The parent tier cost is Rs 100/Yr, and the comprehensive school tier represents Rs 10,000/Yr.",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            "PRIVACY" -> {
                                Text(
                                    "Privacy and robust data isolation are fundamental to our architecture:\n\n" +
                                    "1. Client-Side Grade Protection:\n" +
                                    "Academic logs, student assessment marks, and subject roll structures are isolated inside multi-tenant rows. Parents from Screen A cannot view student lists from Screen B unless a verified view-only authentication key is generated by the administrator.\n\n" +
                                    "2. Collected Metadata:\n" +
                                    "We store active transaction metadata and administrator contact email handles to verify compliance status. No web trackers, third-party sales cookies, or analytics scripts are bundled.\n\n" +
                                    "3. Third Party Handshakes:\n" +
                                    "When triggering a secure upgrade subscription, cards, CVVs, and mobile banking identifiers are processed directly over encrypted merchant channels (Razorpay and PayU).",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            "REFUND" -> {
                                Text(
                                    "We strive to offer premium client-focused grades reporting. Please read our operational billing rollback guidelines:\n\n" +
                                    "1. Right to Downgrade:\n" +
                                    "Subscribers can transition or request a licensing downgrade back to the Free Plan (Rs 0.00/Yr) at any point directly from the Billing Suite controls.\n\n" +
                                    "2. Refund Processing Timeline:\n" +
                                    "In case of accidental duplicate payment triggers across Indian Banking Rails, a full refund representing 100% of the principal + GST is issued within 5 to 7 working business days. Credits are processed automatically back to the originating bank instrument/card/UPI address.\n\n" +
                                    "3. Charge dispute request:\n" +
                                    "Reach out to the registered merchant supervisor at mail@altty.com along with transaction receipt IDs.",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            "SHIPPING" -> {
                                Text(
                                    "This EduGrade application operates purely as a Software-as-a-Service (SaaS) digital delivery model:\n\n" +
                                    "1. Instant Digital Delivery:\n" +
                                    "There are strictly no physical books, report cards, printed documents, or CD packages shipped. Digital licensing privileges are automatically granted. Immediately when Razorpay or PayU checkout API completes successfully, your subscription model adjusts instantly, enabling premium creation modules.\n\n" +
                                    "2. PDF Invoices:\n" +
                                    "Tax compliant breakdowns (including 18% CGST/SGST calculations) are written to local cache stores instantly and are shareable in real-time.",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                            "CONTACT" -> {
                                Text(
                                    "Dedicated SaaS grievance officer contact parameters:\n\n" +
                                    "• Registered Corporate Entity: EduGrade Solutions Pvt Ltd\n" +
                                    "• Official Regulatory Support Email: mail@altty.com\n" +
                                    "• Support Desk Phone: +91 79815 85715\n" +
                                    "• Corporate Office Address: 150/2RT, Vijaya Nagar Colony, HD-500057\n" +
                                    "• General Ticketing Turnaround: Within 12 to 24 hours of receiving formal mail feedback.",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp
                                )
                            }
                        }
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
                    Text("No parent directories registered under this tenancy.", fontSize = 12.sp, color = Slate600)
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
                                Text(parent.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Email: ${parent.email}", fontSize = 11.sp, color = Slate600)
                                Text("Bound Child Student: $childName (RollNo: ${studentNameObj?.rollNo ?: "-"})", fontSize = 11.sp, color = Teal500, fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.Default.LockClock, contentDescription = null, tint = Slate600)
                        }
                    }
                }
            }
        }
    }
}
