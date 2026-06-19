package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.db.AppDatabase
import com.example.data.model.AppConfig
import com.example.data.model.UserAccount
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.data.model.Mark
import com.example.data.model.PaymentRecord
import com.example.data.model.TestType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.security.MessageDigest

// --- Database Provider Singleton ---
object DatabaseProvider {
    @Volatile
    private var instance: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "marks_tracking_db"
            )
            .addMigrations(com.example.data.db.MIGRATION_5_6, com.example.data.db.MIGRATION_6_7)
            .fallbackToDestructiveMigration()
            .fallbackToDestructiveMigrationOnDowngrade()
            .build().also { instance = it }
        }
    }
}

// --- Cryptographic SHA-256 Hashing Extension ---
fun String.sha256(): String {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(this.toByteArray(Charsets.UTF_8))
        hash.joinToString("") { String.format("%02x", it) }
    } catch (e: Exception) {
        this
    }
}

class MarksRepository(context: Context) {
    private val db = DatabaseProvider.getDatabase(context)
    private val configDao = db.appConfigDao()
    private val userDao = db.userAccountDao()
    private val studentDao = db.studentDao()
    private val subjectDao = db.subjectDao()
    private val markDao = db.markDao()
    private val testTypeDao = db.testTypeDao()
    private val paymentRecordDao = db.paymentRecordDao()
    val syncQueueDao = db.syncQueueDao()

    // --- App Config Operations ---
    val appConfig: Flow<AppConfig?> = configDao.getAppConfig()
    
    suspend fun getAppConfigSync(): AppConfig? = withContext(Dispatchers.IO) {
        configDao.getAppConfigSync()
    }
    
    suspend fun saveAppConfig(config: AppConfig) = withContext(Dispatchers.IO) {
        configDao.insertAppConfig(config)
    }

    // --- User Operations ---
    suspend fun getUserByEmail(email: String): UserAccount? = withContext(Dispatchers.IO) {
        userDao.getUserByEmail(email)
    }

    fun getUserById(id: Long): Flow<UserAccount?> = userDao.getUserById(id)

    fun getAssociatedParents(adminId: Long): Flow<List<UserAccount>> = userDao.getAssociatedParents(adminId)

    suspend fun insertUser(user: UserAccount): Long = withContext(Dispatchers.IO) {
        // Automatically hash password only if it's not already a 64-character SHA-256 hexadecimal string
        val isAlreadyHashed = user.passwordHash.length == 64 && user.passwordHash.all { 
            it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' 
        }
        val hashedUser = if (isAlreadyHashed) {
            user
        } else {
            user.copy(passwordHash = user.passwordHash.sha256())
        }
        userDao.insertUser(hashedUser)
    }

    // --- Student Operations ---
    fun getAllStudents(): Flow<List<Student>> = studentDao.getAllStudents()

    fun getAllStudentsBySchool(schoolId: String): Flow<List<Student>> = studentDao.getAllStudentsBySchool(schoolId)

    fun getAllStudentsByParent(parentId: Long): Flow<List<Student>> = studentDao.getAllStudentsByParent(parentId)

    suspend fun getStudentById(id: Long): Student? = withContext(Dispatchers.IO) {
        studentDao.getStudentById(id)
    }

    private suspend fun enqueueSync(entityType: String, operation: String, entityId: Long, payload: org.json.JSONObject) {
        val entry = com.example.data.sync.SyncQueueEntry(
            entityType = entityType,
            operation = operation,
            entityId = entityId,
            payloadJson = payload.toString()
        )
        syncQueueDao.insertQueueEntry(entry)
        com.example.data.sync.SyncService.triggerSync()
    }

    suspend fun insertStudent(student: Student): Long = withContext(Dispatchers.IO) {
        val insertedId = studentDao.insertStudent(student)
        val payload = org.json.JSONObject().apply {
            put("id", insertedId)
            put("encryptedName", student.encryptedName)
            put("rollNo", student.rollNo)
            put("studentClass", student.studentClass)
            put("schoolId", student.schoolId)
        }
        enqueueSync("STUDENT", "INSERT_OR_UPDATE", insertedId, payload)
        insertedId
    }

    suspend fun deleteStudentById(id: Long) = withContext(Dispatchers.IO) {
        markDao.deleteMarksByStudent(id)
        studentDao.deleteStudentById(id)
        val payload = org.json.JSONObject().apply {
            put("id", id)
        }
        enqueueSync("STUDENT", "DELETE", id, payload)
        
        val markPayload = org.json.JSONObject().apply {
            put("studentId", id)
        }
        enqueueSync("MARK", "DELETE", id, markPayload)
    }

    suspend fun getStudentCountForSchool(schoolId: String): Int = withContext(Dispatchers.IO) {
        studentDao.getStudentCountForSchool(schoolId)
    }

    // --- Subject Operations ---
    fun getSubjects(belongsToId: String): Flow<List<Subject>> = subjectDao.getSubjects(belongsToId)

    suspend fun getSubjectsSync(belongsToId: String): List<Subject> = withContext(Dispatchers.IO) {
        subjectDao.getSubjectsSync(belongsToId)
    }

    suspend fun insertSubject(subject: Subject): Long = withContext(Dispatchers.IO) {
        val insertedId = subjectDao.insertSubject(subject)
        val payload = org.json.JSONObject().apply {
            put("id", insertedId)
            put("name", subject.name)
            put("belongsToId", subject.belongsToId)
        }
        enqueueSync("SUBJECT", "INSERT_OR_UPDATE", insertedId, payload)
        insertedId
    }

    suspend fun insertSubjects(subjects: List<Subject>) = withContext(Dispatchers.IO) {
        subjectDao.insertSubjects(subjects)
        subjects.forEach { subject ->
            val finalSubject = if (subject.id == 0L) {
                // Find generated subject
                subjectDao.getSubjectsSync(subject.belongsToId).firstOrNull { it.name == subject.name } ?: subject
            } else {
                subject
            }
            val payload = org.json.JSONObject().apply {
                put("id", finalSubject.id)
                put("name", finalSubject.name)
                put("belongsToId", finalSubject.belongsToId)
            }
            enqueueSync("SUBJECT", "INSERT_OR_UPDATE", finalSubject.id, payload)
        }
    }

    suspend fun deleteSubject(subjectId: Long) = withContext(Dispatchers.IO) {
        markDao.deleteMarksBySubject(subjectId)
        subjectDao.deleteSubjectById(subjectId)
        val payload = org.json.JSONObject().apply {
            put("id", subjectId)
        }
        enqueueSync("SUBJECT", "DELETE", subjectId, payload)
    }

    suspend fun updateSubject(subject: Subject) = withContext(Dispatchers.IO) {
        subjectDao.updateSubject(subject)
        val payload = org.json.JSONObject().apply {
            put("id", subject.id)
            put("name", subject.name)
            put("belongsToId", subject.belongsToId)
        }
        enqueueSync("SUBJECT", "INSERT_OR_UPDATE", subject.id, payload)
    }

    // --- Test Type Operations ---
    fun getTestTypes(belongsToId: String): Flow<List<TestType>> = testTypeDao.getTestTypes(belongsToId)

    suspend fun getTestTypesSync(belongsToId: String): List<TestType> = withContext(Dispatchers.IO) {
        testTypeDao.getTestTypesSync(belongsToId)
    }

    suspend fun insertTestType(testType: TestType): Long = withContext(Dispatchers.IO) {
        val insertedId = testTypeDao.insertTestType(testType)
        val payload = org.json.JSONObject().apply {
            put("id", insertedId)
            put("name", testType.name)
            put("belongsToId", testType.belongsToId)
        }
        enqueueSync("TEST_TYPE", "INSERT_OR_UPDATE", insertedId, payload)
        insertedId
    }

    suspend fun insertTestTypes(testTypes: List<TestType>) = withContext(Dispatchers.IO) {
        testTypeDao.insertTestTypes(testTypes)
        testTypes.forEach { tt ->
            val finalTt = if (tt.id == 0L) {
                testTypeDao.getTestTypesSync(tt.belongsToId).firstOrNull { it.name == tt.name } ?: tt
            } else {
                tt
            }
            val payload = org.json.JSONObject().apply {
                put("id", finalTt.id)
                put("name", finalTt.name)
                put("belongsToId", finalTt.belongsToId)
            }
            enqueueSync("TEST_TYPE", "INSERT_OR_UPDATE", finalTt.id, payload)
        }
    }

    suspend fun deleteTestType(testTypeId: Long, examType: String) = withContext(Dispatchers.IO) {
        markDao.deleteMarksByExamType(examType)
        testTypeDao.deleteTestTypeById(testTypeId)
        val payload = org.json.JSONObject().apply {
            put("id", testTypeId)
        }
        enqueueSync("TEST_TYPE", "DELETE", testTypeId, payload)
    }

    suspend fun updateTestType(testType: TestType) = withContext(Dispatchers.IO) {
        testTypeDao.updateTestType(testType)
        val payload = org.json.JSONObject().apply {
            put("id", testType.id)
            put("name", testType.name)
            put("belongsToId", testType.belongsToId)
        }
        enqueueSync("TEST_TYPE", "INSERT_OR_UPDATE", testType.id, payload)
    }

    // --- Mark Operations ---
    fun getAllMarks(): Flow<List<Mark>> = markDao.getAllMarks()

    fun getMarksForStudent(studentId: Long): Flow<List<Mark>> = markDao.getMarksForStudent(studentId)

    suspend fun getMarksForStudentSync(studentId: Long): List<Mark> = withContext(Dispatchers.IO) {
        markDao.getMarksForStudentSync(studentId)
    }

    suspend fun saveMark(studentId: Long, subjectId: Long, examType: String, score: Double, maxScore: Double = 100.0) = withContext(Dispatchers.IO) {
        val existing = markDao.getMark(studentId, subjectId, examType)
        val finalMark = if (existing != null) {
            val updated = existing.copy(marksObtained = score, maxMarks = maxScore)
            markDao.updateMark(updated)
            updated
        } else {
            val newMark = Mark(studentId = studentId, subjectId = subjectId, examType = examType, marksObtained = score, maxMarks = maxScore)
            val generatedId = markDao.insertMark(newMark)
            newMark.copy(id = generatedId)
        }
        val payload = org.json.JSONObject().apply {
            put("id", finalMark.id)
            put("studentId", finalMark.studentId)
            put("subjectId", finalMark.subjectId)
            put("examType", finalMark.examType)
            put("marksObtained", finalMark.marksObtained)
            put("maxMarks", finalMark.maxMarks)
        }
        enqueueSync("MARK", "INSERT_OR_UPDATE", finalMark.id, payload)
    }

    suspend fun saveMarksBulk(marks: List<Mark>) = withContext(Dispatchers.IO) {
        markDao.insertMarks(marks)
        marks.forEach { mark ->
            val finalMark = if (mark.id == 0L) {
                markDao.getMark(mark.studentId, mark.subjectId, mark.examType) ?: mark
            } else {
                mark
            }
            val payload = org.json.JSONObject().apply {
                put("id", finalMark.id)
                put("studentId", finalMark.studentId)
                put("subjectId", finalMark.subjectId)
                put("examType", finalMark.examType)
                put("marksObtained", finalMark.marksObtained)
                put("maxMarks", finalMark.maxMarks)
            }
            enqueueSync("MARK", "INSERT_OR_UPDATE", finalMark.id, payload)
        }
    }

    // --- Payment / Invoice Operations ---
    fun getPaymentsForUser(userId: Long): Flow<List<PaymentRecord>> = paymentRecordDao.getPaymentsForUser(userId)

    suspend fun insertPayment(payment: PaymentRecord): Long = withContext(Dispatchers.IO) {
        paymentRecordDao.insertPayment(payment)
    }
}
