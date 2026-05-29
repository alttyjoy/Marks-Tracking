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

    suspend fun insertStudent(student: Student): Long = withContext(Dispatchers.IO) {
        studentDao.insertStudent(student)
    }

    suspend fun deleteStudentById(id: Long) = withContext(Dispatchers.IO) {
        markDao.deleteMarksByStudent(id)
        studentDao.deleteStudentById(id)
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
        subjectDao.insertSubject(subject)
    }

    suspend fun insertSubjects(subjects: List<Subject>) = withContext(Dispatchers.IO) {
        subjectDao.insertSubjects(subjects)
    }

    suspend fun deleteSubject(subjectId: Long) = withContext(Dispatchers.IO) {
        markDao.deleteMarksBySubject(subjectId)
        subjectDao.deleteSubjectById(subjectId)
    }

    suspend fun updateSubject(subject: Subject) = withContext(Dispatchers.IO) {
        subjectDao.updateSubject(subject)
    }

    // --- Test Type Operations ---
    fun getTestTypes(belongsToId: String): Flow<List<TestType>> = testTypeDao.getTestTypes(belongsToId)

    suspend fun getTestTypesSync(belongsToId: String): List<TestType> = withContext(Dispatchers.IO) {
        testTypeDao.getTestTypesSync(belongsToId)
    }

    suspend fun insertTestType(testType: TestType): Long = withContext(Dispatchers.IO) {
        testTypeDao.insertTestType(testType)
    }

    suspend fun insertTestTypes(testTypes: List<TestType>) = withContext(Dispatchers.IO) {
        testTypeDao.insertTestTypes(testTypes)
    }

    suspend fun deleteTestType(testTypeId: Long, examType: String) = withContext(Dispatchers.IO) {
        markDao.deleteMarksByExamType(examType)
        testTypeDao.deleteTestTypeById(testTypeId)
    }

    suspend fun updateTestType(testType: TestType) = withContext(Dispatchers.IO) {
        testTypeDao.updateTestType(testType)
    }

    // --- Mark Operations ---
    fun getAllMarks(): Flow<List<Mark>> = markDao.getAllMarks()

    fun getMarksForStudent(studentId: Long): Flow<List<Mark>> = markDao.getMarksForStudent(studentId)

    suspend fun getMarksForStudentSync(studentId: Long): List<Mark> = withContext(Dispatchers.IO) {
        markDao.getMarksForStudentSync(studentId)
    }

    suspend fun saveMark(studentId: Long, subjectId: Long, examType: String, score: Double, maxScore: Double = 100.0) = withContext(Dispatchers.IO) {
        val existing = markDao.getMark(studentId, subjectId, examType)
        if (existing != null) {
            markDao.updateMark(existing.copy(marksObtained = score, maxMarks = maxScore))
        } else {
            markDao.insertMark(Mark(studentId = studentId, subjectId = subjectId, examType = examType, marksObtained = score, maxMarks = maxScore))
        }
    }

    suspend fun saveMarksBulk(marks: List<Mark>) = withContext(Dispatchers.IO) {
        markDao.insertMarks(marks)
    }

    // --- Payment / Invoice Operations ---
    fun getPaymentsForUser(userId: Long): Flow<List<PaymentRecord>> = paymentRecordDao.getPaymentsForUser(userId)

    suspend fun insertPayment(payment: PaymentRecord): Long = withContext(Dispatchers.IO) {
        paymentRecordDao.insertPayment(payment)
    }
}
