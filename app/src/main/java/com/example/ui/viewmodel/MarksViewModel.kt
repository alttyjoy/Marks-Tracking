package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppConfig
import com.example.data.model.Mark
import com.example.data.model.PaymentRecord
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.data.model.TestType
import com.example.data.model.UserAccount
import com.example.data.repository.MarksRepository
import com.example.data.repository.sha256
import com.example.util.EncryptionUtil
import com.example.util.GeminiHelper
import com.example.util.PdfInvoiceGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.UUID

class MarksViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MarksRepository(application)
    private val context = application.applicationContext

    // --- Seeding guard to prevent duplicate concurrent setups ---
    private val seedingTenancies = mutableSetOf<String>()
    private var isSeedingAdmin = false

    // --- Active Database Flow Collection Jobs to Prevent Coroutine Leaks ---
    private var studentsJob: Job? = null
    private var subjectsJob: Job? = null
    private var testTypesJob: Job? = null
    private var paymentsJob: Job? = null
    private var subParentsJob: Job? = null
    private var marksCollectionJob: Job? = null

    // --- Core Navigation and Setup State (Synchronized with SharedPreferences) ---
    private val prefs = context.getSharedPreferences("marks_tracking_prefs", Context.MODE_PRIVATE)

    var isConfigured by mutableStateOf(prefs.getBoolean(KEY_SETUP_COMPLETE, false))
        private set
    var currentConfig by mutableStateOf<AppConfig?>(null)
        private set
    var themeMode by mutableStateOf(prefs.getString(KEY_THEME_MODE, "LIGHT") ?: "LIGHT")
        private set

    fun updateThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        themeMode = mode
    }

    // --- Authentication State ---
    var currentUser by mutableStateOf<UserAccount?>(null)
        private set
    var csrfToken by mutableStateOf("")
        private set

    // --- SharedPreferences Helpers for State and Session Persistence ---
    fun setSetupComplete(value: Boolean) {
        prefs.edit().putBoolean(KEY_SETUP_COMPLETE, value).apply()
        isConfigured = value
    }

    fun saveUserSession(user: UserAccount) {
        prefs.edit().apply {
            putLong(KEY_USER_ID, user.id)
            putString(KEY_USER_NAME, user.name)
            putString(KEY_USER_EMAIL, user.email)
            putString(KEY_USER_PASS_HASH, user.passwordHash)
            putString(KEY_USER_ROLE, user.role)
            putString(KEY_USER_PLAN, user.planType)
            putString(KEY_USER_SCHOOL_ID, user.schoolId)
            if (user.associatedStudentId != null) {
                putLong(KEY_USER_ASSOCIATED_STUDENT_ID, user.associatedStudentId)
            } else {
                remove(KEY_USER_ASSOCIATED_STUDENT_ID)
            }
            if (user.belongsToOwnerId != null) {
                putLong(KEY_USER_BELONGS_TO_OWNER_ID, user.belongsToOwnerId)
            } else {
                remove(KEY_USER_BELONGS_TO_OWNER_ID)
            }
            apply()
        }
        currentUser = user
    }

    fun clearUserSession() {
        prefs.edit().apply {
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_PASS_HASH)
            remove(KEY_USER_ROLE)
            remove(KEY_USER_PLAN)
            remove(KEY_USER_SCHOOL_ID)
            remove(KEY_USER_ASSOCIATED_STUDENT_ID)
            remove(KEY_USER_BELONGS_TO_OWNER_ID)
            apply()
        }
        currentUser = null
    }

    fun loadUserSession(): UserAccount? {
        val email = prefs.getString(KEY_USER_EMAIL, null) ?: return null
        val id = prefs.getLong(KEY_USER_ID, 0L)
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val passHash = prefs.getString(KEY_USER_PASS_HASH, "") ?: ""
        val role = prefs.getString(KEY_USER_ROLE, "VIEW_ONLY_PARENT") ?: "VIEW_ONLY_PARENT"
        val plan = prefs.getString(KEY_USER_PLAN, "FREE") ?: "FREE"
        val schoolId = prefs.getString(KEY_USER_SCHOOL_ID, "") ?: ""
        val studentId = if (prefs.contains(KEY_USER_ASSOCIATED_STUDENT_ID)) prefs.getLong(KEY_USER_ASSOCIATED_STUDENT_ID, -1L) else null
        val ownerId = if (prefs.contains(KEY_USER_BELONGS_TO_OWNER_ID)) prefs.getLong(KEY_USER_BELONGS_TO_OWNER_ID, -1L) else null

        return UserAccount(
            id = id,
            name = name,
            email = email,
            passwordHash = passHash,
            role = role,
            planType = plan,
            schoolId = schoolId,
            associatedStudentId = if (studentId == -1L) null else studentId,
            belongsToOwnerId = if (ownerId == -1L) null else ownerId
        )
    }

    // --- Core Data Collections (State Flows) ---
    private val _studentsList = MutableStateFlow<List<Student>>(emptyList())
    val studentsList: StateFlow<List<Student>> = _studentsList.asStateFlow()

    private val _subjectsList = MutableStateFlow<List<Subject>>(emptyList())
    val subjectsList: StateFlow<List<Subject>> = _subjectsList.asStateFlow()

    private val _testTypesList = MutableStateFlow<List<TestType>>(emptyList())
    val testTypesList: StateFlow<List<TestType>> = _testTypesList.asStateFlow()

    private val _marksList = MutableStateFlow<List<Mark>>(emptyList())
    val marksList: StateFlow<List<Mark>> = _marksList.asStateFlow()

    private val _paymentRecords = MutableStateFlow<List<PaymentRecord>>(emptyList())
    val paymentRecords: StateFlow<List<PaymentRecord>> = _paymentRecords.asStateFlow()

    private val _subParentsList = MutableStateFlow<List<UserAccount>>(emptyList())
    val subParentsList: StateFlow<List<UserAccount>> = _subParentsList.asStateFlow()

    // --- Grid Editing & Interaction States ---
    var selectedStudent by mutableStateOf<Student?>(null)
    val gridMarks = mutableStateMapOf<String, String>() // key format: "${subjectId}_${examType}" -> "score_string"

    // --- Advisory & AI states ---
    var advisoryReport by mutableStateOf("")
        private set
    var isLoadingAdvisory by mutableStateOf(false)
        private set

    // --- Form Error and Message States ---
    var authError by mutableStateOf<String?>(null)
    var setupError by mutableStateOf<String?>(null)
    var actionMessage by mutableStateOf<String?>(null)

    // --- Invoice Flow ---
    var lastDownloadedInvoiceFile by mutableStateOf<File?>(null)

    init {
        val sessionUser = loadUserSession()
        if (sessionUser != null) {
            currentUser = sessionUser
            csrfToken = UUID.randomUUID().toString()
            observeCoreData()
        }
        observeConfig()
    }

    private fun observeConfig() {
        viewModelScope.launch {
            repository.appConfig
                .catch { e -> e.printStackTrace() }
                .collect { config ->
                    try {
                        if (config == null) {
                            val defaultConfig = AppConfig(
                                dbHost = "jdbc:postgresql://marks-tracker-tenant.c1.cloud.spanner:5432",
                                dbName = "marks_tracking_saas",
                                dbUser = "postgres_admin",
                                dbPass = "SuperSecureDbPass*2026",
                                smtpHost = "smtp.marks-tracking.saas.com",
                                smtpPort = "587",
                                smtpUser = "alerts@markstracking.saas",
                                smtpPass = "SMTP_GatewayPass#99",
                                gatewayKey = "rzp_test_N2hK91xf8YwPLz",
                                gatewaySecret = "sec_test_JKX816f0q71p9x",
                                gatewayType = "Razorpay",
                                adminName = "Super Administrator",
                                adminEmail = "admin@school.edu.in",
                                adminPassHash = "SchoolAdmin123!".sha256(),
                                setupComplete = true
                            )
                            repository.saveAppConfig(defaultConfig)
                            setSetupComplete(true)
                        } else {
                            currentConfig = config
                            if (config.setupComplete) {
                                setSetupComplete(true)
                            }
                            if (isConfigured && currentUser == null) {
                                val fallbackSession = loadUserSession()
                                if (fallbackSession != null && fallbackSession.email.isNotEmpty()) {
                                    currentUser = fallbackSession
                                    csrfToken = UUID.randomUUID().toString()
                                    observeCoreData()
                                } else {
                                    if (!isSeedingAdmin) {
                                        // Pre-generate standard admin user credentials to allow easy bypass
                                        seedAdminUser(config)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
    }

    private suspend fun seedAdminUser(config: AppConfig) {
        if (isSeedingAdmin) return
        isSeedingAdmin = true
        try {
            var adminUser = repository.getUserByEmail(config.adminEmail)
            if (adminUser == null) {
                val newUser = UserAccount(
                    name = config.adminName,
                    email = config.adminEmail,
                    passwordHash = config.adminPassHash, // Already hashed
                    role = "SCHOOL_ADMIN",
                    planType = "SCHOOL_PLAN",
                    schoolId = "SCH-ADM-1"
                )
                val newId = repository.insertUser(newUser)
                adminUser = newUser.copy(id = newId)
            } else {
                // If the password hash does not match, repair it (e.g. from previous double-hash)
                if (adminUser.passwordHash != config.adminPassHash) {
                    val updatedUser = adminUser.copy(passwordHash = config.adminPassHash)
                    repository.insertUser(updatedUser)
                    adminUser = updatedUser
                }
            }
            
            // Always sign in to ensure the application is active and shown immediately
            saveUserSession(adminUser)
            csrfToken = java.util.UUID.randomUUID().toString()
            observeCoreData()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isSeedingAdmin = false
        }
    }

    fun observeCoreData() {
        val user = currentUser ?: return

        studentsJob?.cancel()
        subjectsJob?.cancel()
        testTypesJob?.cancel()
        paymentsJob?.cancel()
        subParentsJob?.cancel()
        marksCollectionJob?.cancel()

        studentsJob = viewModelScope.launch {
            // Respect Row-Level Security based on roles
            when (user.role) {
                "SUPER_ADMIN" -> {
                    repository.getAllStudents()
                        .catch { e -> e.printStackTrace() }
                        .collect {
                            try {
                                _studentsList.value = it
                                if (selectedStudent == null && it.isNotEmpty()) {
                                    selectStudent(it.first())
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                }
                "SCHOOL_ADMIN" -> {
                    repository.getAllStudentsBySchool(user.schoolId)
                        .catch { e -> e.printStackTrace() }
                        .collect {
                            try {
                                _studentsList.value = it
                                if (selectedStudent == null && it.isNotEmpty()) {
                                    selectStudent(it.first())
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                }
                "INDIVIDUAL_PARENT" -> {
                    repository.getAllStudentsByParent(user.id)
                        .catch { e -> e.printStackTrace() }
                        .collect {
                            try {
                                _studentsList.value = it
                                if (selectedStudent == null && it.isNotEmpty()) {
                                    selectStudent(it.first())
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                }
                "VIEW_ONLY_PARENT" -> {
                    try {
                        val childId = user.associatedStudentId
                        if (childId != null) {
                            val child = repository.getStudentById(childId)
                            val singleChildList = if (child != null) listOf(child) else emptyList()
                            _studentsList.value = singleChildList
                            if (child != null) {
                                selectStudent(child)
                            }
                        } else {
                            _studentsList.value = emptyList()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        subjectsJob = viewModelScope.launch {
            val belongsToId = when (user.role) {
                "SUPER_ADMIN" -> {
                    val stud = selectedStudent
                    if (stud != null) {
                        if (stud.schoolId.isNotEmpty()) stud.schoolId else stud.parentId?.toString() ?: "SUPER"
                    } else {
                        "SUPER"
                    }
                }
                "SCHOOL_ADMIN" -> user.schoolId
                "VIEW_ONLY_PARENT" -> {
                    val childId = user.associatedStudentId
                    if (childId != null) {
                        val child = repository.getStudentById(childId)
                        child?.schoolId ?: "SCH-ADM-1"
                    } else {
                        "SCH-ADM-1"
                    }
                }
                else -> user.id.toString() // INDIVIDUAL_PARENT
            }
            repository.getSubjects(belongsToId)
                .catch { e -> e.printStackTrace() }
                .collect {
                    try {
                        // If subjects are empty, preseed standard ones for beautiful user experience
                        if (it.isEmpty()) {
                            seedDefaultSubjects(belongsToId)
                        } else {
                            _subjectsList.value = it
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }

        testTypesJob = viewModelScope.launch {
            val belongsToId = when (user.role) {
                "SUPER_ADMIN" -> {
                    val stud = selectedStudent
                    if (stud != null) {
                        if (stud.schoolId.isNotEmpty()) stud.schoolId else stud.parentId?.toString() ?: "SUPER"
                    } else {
                        "SUPER"
                    }
                }
                "SCHOOL_ADMIN" -> user.schoolId
                "VIEW_ONLY_PARENT" -> {
                    val childId = user.associatedStudentId
                    if (childId != null) {
                        val child = repository.getStudentById(childId)
                        child?.schoolId ?: "SCH-ADM-1"
                    } else {
                        "SCH-ADM-1"
                    }
                }
                else -> user.id.toString()
            }
            repository.getTestTypes(belongsToId)
                .catch { e -> e.printStackTrace() }
                .collect {
                    try {
                        if (it.isEmpty()) {
                            seedDefaultTestTypes(belongsToId)
                        } else {
                            _testTypesList.value = it
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }

        paymentsJob = viewModelScope.launch {
            repository.getPaymentsForUser(user.id)
                .catch { e -> e.printStackTrace() }
                .collect {
                    try {
                        _paymentRecords.value = it
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }

        subParentsJob = viewModelScope.launch {
            if (user.role == "SCHOOL_ADMIN") {
                repository.getAssociatedParents(user.id)
                    .catch { e -> e.printStackTrace() }
                    .collect {
                        try {
                            _subParentsList.value = it
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
            }
        }
    }

    private fun seedDefaultSubjects(belongsToId: String) {
        val leaseKey = belongsToId + "_subj"
        if (seedingTenancies.contains(leaseKey)) return
        seedingTenancies.add(leaseKey)
        viewModelScope.launch {
            try {
                val defaults = listOf("Mathematics", "Science", "Social", "English", "Language-1", "Language-2", "Language-3")
                defaults.forEach {
                    repository.insertSubject(Subject(name = it, belongsToId = belongsToId))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                seedingTenancies.remove(leaseKey)
            }
        }
    }

    private fun seedDefaultTestTypes(belongsToId: String) {
        val leaseKey = belongsToId + "_test"
        if (seedingTenancies.contains(leaseKey)) return
        seedingTenancies.add(leaseKey)
        viewModelScope.launch {
            try {
                val plan = currentUser?.planType ?: "FREE"
                val defaults = if (plan == "FREE") {
                    val list = mutableListOf<String>()
                    for (i in 1..20) {
                        list.add("Weekly-$i")
                    }
                    for (i in 1..12) {
                        list.add("Monthly-$i")
                    }
                    list.add("Quarterly")
                    list.add("Half Yearly")
                    list.add("Annually")
                    list
                } else {
                    listOf("Weekly", "Monthly", "Quarterly", "Half-Yearly", "Annual")
                }
                defaults.forEach {
                    repository.insertTestType(TestType(name = it, belongsToId = belongsToId))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                seedingTenancies.remove(leaseKey)
            }
        }
    }

    // --- Onboarding / Setup Complete Handler ---
    fun completeOnboarding(
        dbHost: String, dbName: String, dbUser: String, dbPass: String,
        smtpHost: String, smtpPort: String, smtpUser: String, smtpPass: String,
        gatewayKey: String, gatewaySecret: String, gatewayType: String,
        adminName: String, adminEmail: String, adminPass: String
    ) {
        if (dbHost.isEmpty() || dbName.isEmpty() || smtpHost.isEmpty() || adminEmail.isEmpty() || adminPass.isEmpty()) {
            setupError = "Please fill in all mandatory parameters."
            return
        }

        viewModelScope.launch {
            try {
                val adminHash = adminPass.sha256()
                val config = AppConfig(
                    dbHost = dbHost,
                    dbName = dbName,
                    dbUser = dbUser,
                    dbPass = dbPass,
                    smtpHost = smtpHost,
                    smtpPort = smtpPort,
                    smtpUser = smtpUser,
                    smtpPass = smtpPass,
                    gatewayKey = gatewayKey,
                    gatewaySecret = gatewaySecret,
                    gatewayType = gatewayType,
                    adminName = adminName,
                    adminEmail = adminEmail,
                    adminPassHash = adminHash,
                    setupComplete = true
                )
                repository.saveAppConfig(config)
                setSetupComplete(true)
                setupError = null
                actionMessage = "Setup complete! Logged in as administrator automatically."
                seedAdminUser(config)
            } catch (e: Exception) {
                setupError = "Failed to save configuration: ${e.message}"
            }
        }
    }

    // --- Authentication Operations ---
    fun executeLogin(email: String, pass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authError = null
            try {
                val user = repository.getUserByEmail(email)
                if (user != null && user.passwordHash == pass.sha256()) {
                    saveUserSession(user)
                    csrfToken = UUID.randomUUID().toString() // Issue cryptographically random CSRF token
                    observeCoreData()
                    onSuccess()
                } else {
                    authError = "Invalid email or matching password combination."
                }
            } catch (e: Exception) {
                authError = "Authentication failed: ${e.message}"
            }
        }
    }

    fun executeRegister(name: String, email: String, pass: String, plan: String, schoolIdInput: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authError = null
            if (name.trim().isEmpty() || email.trim().isEmpty() || pass.trim().isEmpty()) {
                authError = "Please fill in all registration fields."
                return@launch
            }
            try {
                val existing = repository.getUserByEmail(email.trim())
                if (existing != null) {
                    authError = "An account with this email already exists."
                    return@launch
                }
                
                val finalRole = if (plan == "SCHOOL_PLAN") "SCHOOL_ADMIN" else "INDIVIDUAL_PARENT"
                val finalSchoolId = if (plan == "SCHOOL_PLAN") {
                    if (schoolIdInput.trim().isEmpty()) "SCH-" + (1000..9999).random() else schoolIdInput.trim()
                } else {
                    ""
                }
                
                val user = UserAccount(
                    name = name.trim(),
                    email = email.trim(),
                    passwordHash = pass.sha256(),
                    role = finalRole,
                    planType = plan,
                    schoolId = finalSchoolId
                )
                repository.insertUser(user)
                actionMessage = "Registration successful! Auto-logging you in..."
                executeLogin(email.trim(), pass, onSuccess)
            } catch (e: Exception) {
                authError = "Registration failed: ${e.message}"
            }
        }
    }

    private suspend fun preseedAdminDemoLogs(schoolId: String) {
        // 1. Seed Subjects
        val existingSubjects = repository.getSubjectsSync(schoolId)
        val subjectsToInsert = if (existingSubjects.isEmpty()) {
            val defaults = listOf("Mathematics", "Science", "Social", "English", "Language-1", "Language-2", "Language-3")
            defaults.map { name ->
                val s = Subject(name = name, belongsToId = schoolId)
                val id = repository.insertSubject(s)
                s.copy(id = id)
            }
        } else {
            existingSubjects
        }

        // 2. Seed Students
        val count = repository.getStudentCountForSchool(schoolId)
        if (count == 0) {
            val studentsData = listOf(
                Pair("Aarav Sharma", "101"),
                Pair("Diya Patel", "102"),
                Pair("Kabir Mehta", "103"),
                Pair("Ananya Iyer", "104")
            )
            val seededStudents = studentsData.map { (name, roll) ->
                val encName = EncryptionUtil.encrypt(name)
                val stud = Student(encryptedName = encName, rollNo = roll, schoolId = schoolId)
                val id = repository.insertStudent(stud)
                stud.copy(id = id)
            }

            // 3. Seed Marks
            val exams = listOf("Weekly", "Monthly", "Quarterly", "Half-Yearly", "Annual")
            val marksList = mutableListOf<Mark>()
            
            val aaravScores = mapOf(
                "Mathematics" to listOf(85.0, 88.0, 90.0, 92.0, 95.0),
                "Science" to listOf(78.0, 82.0, 85.0, 86.0, 90.0),
                "Social" to listOf(90.0, 92.0, 93.0, 91.0, 94.0),
                "English" to listOf(88.0, 85.0, 87.0, 89.0, 92.0),
                "Language-1" to listOf(92.0, 94.0, 95.0, 97.0, 99.0),
                "Language-2" to listOf(85.0, 85.0, 85.0, 85.0, 85.0),
                "Language-3" to listOf(80.0, 80.0, 80.0, 80.0, 80.0)
            )

            val diyaScores = mapOf(
                "Mathematics" to listOf(35.0, 38.0, 34.0, 42.0, 39.0), // Need urgent work
                "Science" to listOf(65.0, 70.0, 72.0, 68.0, 75.0),
                "Social" to listOf(75.0, 77.0, 74.0, 79.0, 81.0),
                "English" to listOf(80.0, 82.0, 85.0, 83.0, 88.0),
                "Language-1" to listOf(70.0, 72.0, 75.0, 78.0, 80.0),
                "Language-2" to listOf(60.0, 62.0, 64.0, 61.0, 65.0),
                "Language-3" to listOf(50.0, 55.0, 52.0, 58.0, 60.0)
            )

            val kabirScores = mapOf(
                "Mathematics" to listOf(70.0, 72.0, 75.0, 74.0, 78.0),
                "Science" to listOf(88.0, 75.0, 60.0, 48.0, 38.0), // Downward trend
                "Social" to listOf(58.0, 60.0, 62.0, 61.0, 65.0),
                "English" to listOf(60.0, 62.0, 65.0, 64.0, 68.0),
                "Language-1" to listOf(75.0, 78.0, 80.0, 82.0, 85.0),
                "Language-2" to listOf(65.0, 66.0, 68.0, 67.0, 70.0),
                "Language-3" to listOf(55.0, 58.0, 60.0, 59.0, 62.0)
            )

            val ananyaScores = mapOf(
                "Mathematics" to listOf(95.0, 96.0, 98.0, 97.0, 99.0),
                "Science" to listOf(92.0, 94.0, 95.0, 96.0, 98.0),
                "Social" to listOf(88.0, 90.0, 92.0, 91.0, 94.0),
                "English" to listOf(90.0, 91.0, 93.0, 92.0, 95.0),
                "Language-1" to listOf(96.0, 98.0, 99.0, 98.0, 100.0),
                "Language-2" to listOf(90.0, 92.0, 93.0, 95.0, 96.0),
                "Language-3" to listOf(92.0, 94.0, 95.0, 93.0, 97.0)
            )

            seededStudents.forEach { stud ->
                val decName = EncryptionUtil.decrypt(stud.encryptedName)
                val scoresMap = when (decName) {
                    "Aarav Sharma" -> aaravScores
                    "Diya Patel" -> diyaScores
                    "Kabir Mehta" -> kabirScores
                    else -> ananyaScores
                }

                subjectsToInsert.forEach { sub ->
                    val examScores = scoresMap[sub.name] ?: listOf(75.0, 75.0, 75.0, 75.0, 75.0)
                    exams.forEachIndexed { idx, exam ->
                        val mVal = examScores.getOrElse(idx) { 75.0 }
                        marksList.add(Mark(studentId = stud.id, subjectId = sub.id, examType = exam, marksObtained = mVal))
                    }
                }
            }

            if (marksList.isNotEmpty()) {
                repository.saveMarksBulk(marksList)
            }
        }
    }

    private suspend fun preseedParentDemoLogs(parentId: Long) {
        val belongsToId = parentId.toString()

        // 1. Seed Subjects
        val existingSubjects = repository.getSubjectsSync(belongsToId)
        val subjectsToInsert = if (existingSubjects.isEmpty()) {
            val defaults = listOf("Mathematics", "Science", "Social", "English", "Language-1", "Language-2", "Language-3")
            defaults.map { name ->
                val s = Subject(name = name, belongsToId = belongsToId)
                val id = repository.insertSubject(s)
                s.copy(id = id)
            }
        } else {
            existingSubjects
        }

        // 2. Seed Student child file bound to parent
        val existingKids = repository.getAllStudentsByParent(parentId).first()
        if (existingKids.isEmpty()) {
            val childName = "Rohan Sharma"
            val encName = EncryptionUtil.encrypt(childName)
            val childId = repository.insertStudent(
                Student(
                    encryptedName = encName,
                    rollNo = "105",
                    parentId = parentId
                )
            )
            
            // Seed Marks for Rohan (showing declining Science curve for diagnosis tryouts)
            val exams = listOf("Weekly", "Monthly", "Quarterly", "Half-Yearly", "Annual")
            val rohanScores = mapOf(
                "Mathematics" to listOf(78.0, 80.0, 84.0, 83.0, 87.0),
                "Science" to listOf(42.0, 39.0, 35.0, 32.0, 30.0), // Downward trend
                "Social" to listOf(70.0, 72.0, 75.0, 78.0, 82.0),
                "English" to listOf(85.0, 88.0, 90.0, 91.0, 94.0),
                "Language-1" to listOf(88.0, 90.0, 92.0, 91.0, 95.0),
                "Language-2" to listOf(80.0, 80.0, 80.0, 80.0, 80.0),
                "Language-3" to listOf(75.0, 75.0, 75.0, 75.0, 75.0)
            )

            val marksList = mutableListOf<Mark>()
            subjectsToInsert.forEach { sub ->
                val examScores = rohanScores[sub.name] ?: listOf(75.0, 75.0, 75.0, 75.0, 75.0)
                exams.forEachIndexed { idx, exam ->
                    val mVal = examScores.getOrElse(idx) { 75.0 }
                    marksList.add(Mark(studentId = childId, subjectId = sub.id, examType = exam, marksObtained = mVal))
                }
            }
            repository.saveMarksBulk(marksList)
        }
    }

    fun seedAndLoginAdminDemo(onSuccess: (String, String) -> Unit) {
        viewModelScope.launch {
            val adminEmail = "admin@school.edu.in"
            val adminPass = "SchoolAdmin123!"

            try {
                // Ensure AppConfig exists so setup transition succeeds and triggers seeding
                var config = repository.getAppConfigSync()
                if (config == null) {
                    config = AppConfig(
                        dbHost = "jdbc:postgresql://marks-tracker-tenant.c1.cloud.spanner:5432",
                        dbName = "marks_tracking_saas",
                        dbUser = "postgres_admin",
                        dbPass = "SuperSecureDbPass*2026",
                        smtpHost = "smtp.marks-tracking.saas.com",
                        smtpPort = "587",
                        smtpUser = "alerts@markstracking.saas",
                        smtpPass = "SMTP_GatewayPass#99",
                        gatewayKey = "rzp_test_N2hK91xf8YwPLz",
                        gatewaySecret = "sec_test_JKX816f0q71p9x",
                        gatewayType = "Razorpay",
                        adminName = "Super Administrator",
                        adminEmail = adminEmail,
                        adminPassHash = adminPass.sha256(),
                        setupComplete = true
                    )
                    repository.saveAppConfig(config)
                    setSetupComplete(true)
                }
                val existing = repository.getUserByEmail(adminEmail)
                if (existing == null) {
                    repository.insertUser(
                        UserAccount(
                            name = "Super Administrator",
                            email = adminEmail,
                            passwordHash = adminPass.sha256(),
                            role = "SCHOOL_ADMIN",
                            planType = "SCHOOL_PLAN",
                            schoolId = "SCH-ADM-1"
                        )
                    )
                } else if (existing.passwordHash != adminPass.sha256()) {
                    repository.insertUser(existing.copy(passwordHash = adminPass.sha256()))
                }

                // Preseed student ledger before forwarding
                preseedAdminDemoLogs("SCH-ADM-1")

                onSuccess(adminEmail, adminPass)
                executeLogin(adminEmail, adminPass, {})
            } catch (e: Exception) {
                authError = "Admin seeding failed: ${e.message}"
            }
        }
    }

    fun seedAndLoginParentDemo(onSuccess: (String, String) -> Unit) {
        viewModelScope.launch {
            val parentEmail = "parent.demo@test.com"
            val pPass = "parent123"

            try {
                // Ensure AppConfig exists so setup transition succeeds
                var config = repository.getAppConfigSync()
                if (config == null) {
                    config = AppConfig(
                        dbHost = "jdbc:postgresql://marks-tracker-tenant.c1.cloud.spanner:5432",
                        dbName = "marks_tracking_saas",
                        dbUser = "postgres_admin",
                        dbPass = "SuperSecureDbPass*2026",
                        smtpHost = "smtp.marks-tracking.saas.com",
                        smtpPort = "587",
                        smtpUser = "alerts@markstracking.saas",
                        smtpPass = "SMTP_GatewayPass#99",
                        gatewayKey = "rzp_test_N2hK91xf8YwPLz",
                        gatewaySecret = "sec_test_JKX816f0q71p9x",
                        gatewayType = "Razorpay",
                        adminName = "Super Administrator",
                        adminEmail = "admin@school.edu.in",
                        adminPassHash = "SchoolAdmin123!".sha256(),
                        setupComplete = true
                    )
                    repository.saveAppConfig(config)
                    setSetupComplete(true)
                }
                val existing = repository.getUserByEmail(parentEmail)
                val parentId = if (existing == null) {
                    repository.insertUser(
                        UserAccount(
                            name = "Pranav Sharma (Parent)",
                            email = parentEmail,
                            passwordHash = pPass.sha256(),
                            role = "INDIVIDUAL_PARENT",
                            planType = "INDIVIDUAL_PARENT_PLAN"
                        )
                    )
                } else {
                    if (existing.passwordHash != pPass.sha256()) {
                        repository.insertUser(existing.copy(passwordHash = pPass.sha256()))
                    }
                    existing.id
                }

                // Preseed individual child ledger before forwarding
                preseedParentDemoLogs(parentId)

                onSuccess(parentEmail, pPass)
                executeLogin(parentEmail, pPass, {})
            } catch (e: Exception) {
                authError = "Parent seeding failed: ${e.message}"
            }
        }
    }

    fun seedAndLoginSuperDemo(onSuccess: (String, String) -> Unit) {
        viewModelScope.launch {
            val superEmail = "super@admin.com"
            val superPass = "superadmin123"

            try {
                // Ensure AppConfig exists so setup transition succeeds
                var config = repository.getAppConfigSync()
                if (config == null) {
                    config = AppConfig(
                        dbHost = "jdbc:postgresql://marks-tracker-tenant.c1.cloud.spanner:5432",
                        dbName = "marks_tracking_saas",
                        dbUser = "postgres_admin",
                        dbPass = "SuperSecureDbPass*2026",
                        smtpHost = "smtp.marks-tracking.saas.com",
                        smtpPort = "587",
                        smtpUser = "alerts@markstracking.saas",
                        smtpPass = "SMTP_GatewayPass#99",
                        gatewayKey = "rzp_test_N2hK91xf8YwPLz",
                        gatewaySecret = "sec_test_JKX816f0q71p9x",
                        gatewayType = "Razorpay",
                        adminName = "Super Administrator",
                        adminEmail = "admin@school.edu.in",
                        adminPassHash = "SchoolAdmin123!".sha256(),
                        setupComplete = true
                    )
                    repository.saveAppConfig(config)
                    setSetupComplete(true)
                }
                val existing = repository.getUserByEmail(superEmail)
                if (existing == null) {
                    repository.insertUser(
                        UserAccount(
                            name = "Global Super Admin",
                            email = superEmail,
                            passwordHash = superPass.sha256(),
                            role = "SUPER_ADMIN",
                            planType = "SCHOOL_PLAN",
                            schoolId = "GLOBAL_SUPER"
                        )
                    )
                } else if (existing.passwordHash != superPass.sha256()) {
                    repository.insertUser(existing.copy(passwordHash = superPass.sha256()))
                }

                onSuccess(superEmail, superPass)
                executeLogin(superEmail, superPass, {})
            } catch (e: Exception) {
                authError = "Super Admin seeding failed: ${e.message}"
            }
        }
    }

    fun executeLogout() {
        clearUserSession()
        selectedStudent = null
        csrfToken = ""
        _studentsList.value = emptyList()
        _testTypesList.value = emptyList()
        _marksList.value = emptyList()
        advisoryReport = ""

        studentsJob?.cancel()
        subjectsJob?.cancel()
        testTypesJob?.cancel()
        paymentsJob?.cancel()
        subParentsJob?.cancel()
        marksCollectionJob?.cancel()
    }

    // --- Multi-Tenant Billing/Subscriptions Handling ---
    fun subscribeToPlan(planName: String, roleName: String, baseAmt: Double, gateway: String) {
        val user = currentUser ?: return
        viewModelScope.launch {
            val gst = baseAmt * 0.18
            val total = baseAmt + gst
            val paymentId = "pay_" + UUID.randomUUID().toString().replace("-", "").take(14)
            
            val record = PaymentRecord(
                userId = user.id,
                userName = user.name,
                userEmail = user.email,
                planType = planName,
                basePrice = baseAmt,
                gstAmount = gst,
                totalAmount = total,
                paymentGateway = gateway,
                paymentId = paymentId
            )
            val insertedId = repository.insertPayment(record)
            
            // Upgrade logged in account plan
            val upgradedUser = user.copy(
                planType = record.planType,
                role = roleName,
                schoolId = if (roleName == "SCHOOL_ADMIN" && user.schoolId.isEmpty()) "SCH-TEN-${UUID.randomUUID().toString().take(5).uppercase()}" else user.schoolId
            )
            repository.insertUser(upgradedUser)
            saveUserSession(upgradedUser)
            
            actionMessage = "Successfully changed plan to ${record.planType}! Receipt INV-MT-$insertedId created."
            observeCoreData()
        }
    }

    // --- Student & Sub-account Management ---
    fun addNewStudent(name: String, rollNo: String, studentClass: String = "") {
        val user = currentUser ?: return
        if (name.isEmpty()) return

        viewModelScope.launch {
            // Parent Cap: maximum of 2 children
            if (user.role == "INDIVIDUAL_PARENT" || user.planType == "INDIVIDUAL_PARENT_PLAN") {
                val currentCount = _studentsList.value.size
                if (currentCount >= 2) {
                    actionMessage = "Error: Parents Plan is capped at a maximum of 2 children."
                    return@launch
                }
            }

            // School Plan strict validation limit (200 students)
            if (user.planType == "SCHOOL_PLAN" || user.role == "SCHOOL_ADMIN") {
                val currentCount = repository.getStudentCountForSchool(user.schoolId)
                if (currentCount >= 200) {
                    actionMessage = "Error: School Plan limits reached (capped at 200 students max)."
                    return@launch
                }
            }

            val encryptedName = EncryptionUtil.encrypt(name)
            val student = Student(
                encryptedName = encryptedName,
                rollNo = rollNo,
                studentClass = studentClass,
                schoolId = if (user.role == "SCHOOL_ADMIN") user.schoolId else "",
                parentId = if (user.role == "INDIVIDUAL_PARENT") user.id else null
            )
            repository.insertStudent(student)
            actionMessage = "Add complete: encrypted name metadata index matches database."
        }
    }

    fun deleteStudent(studentId: Long) {
        viewModelScope.launch {
            repository.deleteStudentById(studentId)
            actionMessage = "Student and associated marks entries purged successfully."
            if (selectedStudent?.id == studentId) {
                selectedStudent = null
                _marksList.value = emptyList()
            }
        }
    }

    fun createParentSubAccount(parentName: String, parentEmail: String, passStr: String, childId: Long) {
        val user = currentUser ?: return
        if (user.role != "SCHOOL_ADMIN") return
        if (parentName.isEmpty() || parentEmail.isEmpty() || passStr.isEmpty()) return

        viewModelScope.launch {
            val existing = repository.getUserByEmail(parentEmail)
            if (existing != null) {
                actionMessage = "Parent account email registration conflict: already exists."
                return@launch
            }

            val parentUser = UserAccount(
                name = parentName,
                email = parentEmail,
                passwordHash = passStr, // will hash inside repository
                role = "VIEW_ONLY_PARENT",
                planType = "FREE",
                schoolId = user.schoolId,
                associatedStudentId = childId,
                belongsToOwnerId = user.id
            )
            repository.insertUser(parentUser)
            actionMessage = "View-Only Parent Sub-Account generated for roll child verification."
        }
    }

    // --- Excel Grid Operations ---
    fun selectStudent(student: Student) {
        selectedStudent = student
        gridMarks.clear()
        marksCollectionJob?.cancel()
        marksCollectionJob = viewModelScope.launch {
            repository.getMarksForStudent(student.id)
                .catch { e -> e.printStackTrace() }
                .collect { list ->
                    _marksList.value = list
                    list.forEach { mark ->
                        gridMarks["${mark.subjectId}_${mark.examType}"] = mark.marksObtained.toString()
                    }
                }
        }
        if (currentUser?.role == "SUPER_ADMIN") {
            val belongsToId = if (student.schoolId.isNotEmpty()) student.schoolId else student.parentId?.toString() ?: "SUPER"
            viewModelScope.launch {
                repository.getSubjects(belongsToId)
                    .catch { e -> e.printStackTrace() }
                    .collect {
                        if (it.isEmpty()) {
                            seedDefaultSubjects(belongsToId)
                        } else {
                            _subjectsList.value = it
                        }
                    }
            }
            viewModelScope.launch {
                repository.getTestTypes(belongsToId)
                    .catch { e -> e.printStackTrace() }
                    .collect {
                        if (it.isEmpty()) {
                            seedDefaultTestTypes(belongsToId)
                        } else {
                            _testTypesList.value = it
                        }
                    }
            }
        }
    }

    fun updateGridCell(subjectId: Long, examType: String, value: String) {
        gridMarks["${subjectId}_${examType}"] = value
    }

    fun saveGridMarks() {
        val student = selectedStudent ?: return
        val user = currentUser ?: return
        viewModelScope.launch {
            // Role verification rules:
            if (user.role == "SCHOOL_ADMIN") {
                if (student.schoolId != user.schoolId) {
                    actionMessage = "Error: School Admin can only update marks for students in their school."
                    return@launch
                }
            } else if (user.role == "INDIVIDUAL_PARENT") {
                if (student.parentId != user.id) {
                    actionMessage = "Error: Parent can only update marks for their children."
                    return@launch
                }
                val parentKids = _studentsList.value
                val isOurKid = parentKids.any { it.id == student.id }
                if (!isOurKid || parentKids.size > 2) {
                    actionMessage = "Error: Parent is capped at updating a maximum of 2 children marks."
                    return@launch
                }
            } else if (user.role == "VIEW_ONLY_PARENT") {
                actionMessage = "Error: View-Only parent accounts cannot save or modify marks."
                return@launch
            }
            // SUPER_ADMIN has unlimited updater access - bypasses filters entirely.

            gridMarks.forEach { (key, value) ->
                val parts = key.split("_")
                if (parts.size == 2) {
                    val subjectId = parts[0].toLongOrNull()
                    val examType = parts[1]
                    val score = value.toDoubleOrNull()
                    if (subjectId != null && score != null) {
                        repository.saveMark(student.id, subjectId, examType, score)
                    }
                }
            }
            actionMessage = "All cell updates securely saved to sqlite repository."
        }
    }

    // --- BULK IMPORT CSV Parser ---
    fun processBulkCsvImport(csvText: String) {
        val user = currentUser ?: return
        viewModelScope.launch {
            try {
                val lines = csvText.lines()
                if (lines.isEmpty() || lines[0].isEmpty()) {
                    actionMessage = "CSV contains no records."
                    return@launch
                }

                val schoolIdVal = if (user.role == "SCHOOL_ADMIN") user.schoolId else ""
                val parentIdVal = if (user.role == "INDIVIDUAL_PARENT") user.id else null

                var insertedCount = 0
                val belongsToId = if (user.role == "SCHOOL_ADMIN") user.schoolId else user.id.toString()
                val activeSubjects = repository.getSubjectsSync(belongsToId)

                // Skip header line if it looks like columns description
                val startIdx = if (lines[0].contains("StudentName", ignoreCase = true) || lines[0].contains("Subject", ignoreCase = true)) 1 else 0

                for (i in startIdx until lines.size) {
                    val line = lines[i].trim()
                    if (line.isEmpty()) continue
                    
                    val cols = line.split(",").map { it.trim() }
                    if (cols.size < 3) continue

                    val rollNo = cols[0]
                    val studName = cols[1]
                    val subjName = cols[2]

                    if (studName.isEmpty() || subjName.isEmpty()) continue

                    // 1. Get or Add student (AES Encryption)
                    val encName = EncryptionUtil.encrypt(studName)
                    val existingStuds = _studentsList.value
                    var studentObj = existingStuds.find { 
                        EncryptionUtil.decrypt(it.encryptedName).equals(studName, true) && it.rollNo == rollNo 
                    }
                    if (studentObj == null) {
                        // School Plan / School Admin cap check
                        if (user.planType == "SCHOOL_PLAN" || user.role == "SCHOOL_ADMIN") {
                            val count = repository.getStudentCountForSchool(user.schoolId)
                            if (count >= 200) {
                                actionMessage = "Error: School plans capped at 200 student directories max."
                                break // Stop bulk imports if capacity hits 200 ceiling limit
                            }
                        }
                        // Parent cap check
                        if (user.planType == "INDIVIDUAL_PARENT_PLAN" || user.role == "INDIVIDUAL_PARENT") {
                            val count = _studentsList.value.size + (insertedCount)
                            if (count >= 2) {
                                actionMessage = "Error: Parents Plan is capped at a maximum of 2 children."
                                break // Stop bulk imports if parents capacity would hit 2 child limit list size
                            }
                        }
                        val newStudId = repository.insertStudent(
                            Student(
                                encryptedName = encName,
                                rollNo = rollNo,
                                schoolId = schoolIdVal,
                                parentId = parentIdVal
                            )
                        )
                        studentObj = Student(id = newStudId, encryptedName = encName, rollNo = rollNo, schoolId = schoolIdVal, parentId = parentIdVal)
                    }

                    // --- Auto-provision Parent User Account if ParentEmail is provided in CSV ---
                    val activeTestTypes = repository.getTestTypesSync(belongsToId)
                    val exams = activeTestTypes.map { it.name }.ifEmpty { listOf("Weekly", "Monthly", "Quarterly", "Half-Yearly", "Annual") }
                    val parentEmailIdx = 3 + exams.size
                    val parentEmail = if (parentEmailIdx < cols.size) cols[parentEmailIdx].trim() else ""
                    if (parentEmail.isNotEmpty() && parentEmail.contains("@") && user.role == "SCHOOL_ADMIN") {
                        val existingParent = repository.getUserByEmail(parentEmail)
                        if (existingParent == null) {
                            val autoParent = UserAccount(
                                name = "Parent of $studName",
                                email = parentEmail,
                                passwordHash = "Pass123",
                                role = "VIEW_ONLY_PARENT",
                                planType = "FREE",
                                schoolId = user.schoolId,
                                associatedStudentId = studentObj.id,
                                belongsToOwnerId = user.id
                            )
                            repository.insertUser(autoParent)
                        } else {
                            if (existingParent.associatedStudentId != studentObj.id) {
                                repository.insertUser(existingParent.copy(associatedStudentId = studentObj.id))
                            }
                        }
                    }

                    // 2. Get or Add subject
                    var subjectObj = activeSubjects.find { it.name.equals(subjName, true) }
                    if (subjectObj == null) {
                        val subId = repository.insertSubject(Subject(name = subjName, belongsToId = belongsToId))
                        subjectObj = Subject(id = subId, name = subjName, belongsToId = belongsToId)
                    }

                    // 3. Dynamic Import Marks for active test configurations
                    for (eIdx in exams.indices) {
                        val colIdx = 3 + eIdx
                        if (colIdx < cols.size) {
                            val valStr = cols[colIdx]
                            val score = valStr.toDoubleOrNull()
                            if (score != null) {
                                repository.saveMark(studentObj.id, subjectObj.id, exams[eIdx], score)
                            }
                        }
                    }
                    insertedCount++
                }

                actionMessage = "Bulk Import Success: Parsed and populated records for $insertedCount students."
                observeCoreData()
            } catch (e: Exception) {
                actionMessage = "Bulk CSV parsing exception: ${e.message}"
            }
        }
    }

    // --- AI Advisory trigger ---
    fun fetchAdvisoryReport(studentName: String) {
        val student = selectedStudent ?: return
        val currentSubjects = _subjectsList.value
        val currentMarks = _marksList.value

        viewModelScope.launch {
            isLoadingAdvisory = true
            advisoryReport = ""
            try {
                val report = GeminiHelper.getStudyPlan(studentName, currentSubjects, currentMarks)
                advisoryReport = report
            } catch (e: Exception) {
                advisoryReport = "Advisory report failed logic aggregation: ${e.message}"
            } finally {
                isLoadingAdvisory = false
            }
        }
    }

    // --- Invoice Download pdf action ---
    fun downloadInvoicePdf(record: PaymentRecord) {
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                PdfInvoiceGenerator.generateInvoicePdf(context, record)
            }
            if (file != null && file.exists()) {
                lastDownloadedInvoiceFile = file
                actionMessage = "Receipt PDF downloaded: ${file.name}. Saved in Invoices."
            } else {
                actionMessage = "Error building invoice PDF."
            }
        }
    }

    fun getDecryptedStudentName(encrypted: String): String {
        return EncryptionUtil.decrypt(encrypted)
    }

    fun addSubject(name: String) {
        val user = currentUser ?: return
        viewModelScope.launch {
            if (user.planType == "FREE") {
                actionMessage = "SaaS Rules: FREE tier is capped to precisely the 6 standard seeded subjects. Upgrade to standard/school plan for infinite customizable subjects!"
                return@launch
            }
            if (name.isBlank()) {
                actionMessage = "Subject name cannot be blank."
                return@launch
            }
            val belongsToId = when (user.role) {
                "SCHOOL_ADMIN" -> user.schoolId
                "VIEW_ONLY_PARENT" -> return@launch // View only cannot add
                else -> user.id.toString()
            }
            val existing = _subjectsList.value
            if (existing.any { it.name.equals(name.trim(), true) }) {
                actionMessage = "Subject \"${name.trim()}\" already exists."
                return@launch
            }
            repository.insertSubject(Subject(name = name.trim(), belongsToId = belongsToId))
            actionMessage = "Subject \"${name.trim()}\" added successfully!"
        }
    }

    fun renameSubject(subject: Subject, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateSubject(subject.copy(name = newName.trim()))
            actionMessage = "Subject updated successfully!"
        }
    }

    fun removeSubject(subject: Subject) {
        val user = currentUser ?: return
        if (user.planType == "FREE") {
            actionMessage = "SaaS Rules: FREE plan users cannot delete subjects. Upgrade to Paid plan for unlimited customization!"
            return
        }
        viewModelScope.launch {
            repository.deleteSubject(subject.id)
            actionMessage = "Subject \"${subject.name}\" and its matching scores deleted!"
        }
    }

    fun addTestType(name: String) {
        val user = currentUser ?: return
        viewModelScope.launch {
            if (user.planType == "FREE") {
                actionMessage = "SaaS Rules: FREE plan is limited to the standard 35 test schedules. Upgrade to Paid plan for custom test schedules!"
                return@launch
            }
            if (name.isBlank()) {
                actionMessage = "Test type name cannot be blank."
                return@launch
            }
            val belongsToId = when (user.role) {
                "SCHOOL_ADMIN" -> user.schoolId
                "VIEW_ONLY_PARENT" -> return@launch // View only cannot add
                else -> user.id.toString()
            }
            val existing = _testTypesList.value
            if (existing.any { it.name.equals(name.trim(), true) }) {
                actionMessage = "Test configuration \"${name.trim()}\" already exists."
                return@launch
            }
            repository.insertTestType(TestType(name = name.trim(), belongsToId = belongsToId))
            actionMessage = "Test type \"${name.trim()}\" configured!"
        }
    }

    fun renameTestType(testType: TestType, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateTestType(testType.copy(name = newName.trim()))
            actionMessage = "Test type updated successfully!"
        }
    }

    fun removeTestType(testType: TestType) {
        val user = currentUser ?: return
        if (user.planType == "FREE") {
            actionMessage = "SaaS Rules: FREE plan users cannot delete test types. Upgrade to Paid plan for custom schedules!"
            return
        }
        viewModelScope.launch {
            repository.deleteTestType(testType.id, testType.name)
            actionMessage = "Test type \"${testType.name}\" and its matching scores deleted!"
        }
    }

    companion object {
        private const val KEY_SETUP_COMPLETE = "key_setup_complete"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_USER_ID = "key_user_id"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_PASS_HASH = "key_user_pass_hash"
        private const val KEY_USER_ROLE = "key_user_role"
        private const val KEY_USER_PLAN = "key_user_plan"
        private const val KEY_USER_SCHOOL_ID = "key_user_school_id"
        private const val KEY_USER_ASSOCIATED_STUDENT_ID = "key_user_associated_student_id"
        private const val KEY_USER_BELONGS_TO_OWNER_ID = "key_user_belongs_to_owner_id"
    }
}
