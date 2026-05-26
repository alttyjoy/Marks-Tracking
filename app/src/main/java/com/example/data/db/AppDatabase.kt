package com.example.data.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.example.data.model.AppConfig
import com.example.data.model.UserAccount
import com.example.data.model.Student
import com.example.data.model.Subject
import com.example.data.model.Mark
import com.example.data.model.PaymentRecord
import com.example.data.model.TestType
import kotlinx.coroutines.flow.Flow

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getAppConfig(): Flow<AppConfig?>

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getAppConfigSync(): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppConfig(config: AppConfig)
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserAccount?

    @Query("SELECT * FROM user_accounts WHERE id = :id LIMIT 1")
    fun getUserById(id: Long): Flow<UserAccount?>

    @Query("SELECT * FROM user_accounts WHERE belongsToOwnerId = :adminId")
    fun getAssociatedParents(adminId: Long): Flow<List<UserAccount>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccount): Long
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE schoolId = :schoolId")
    fun getAllStudentsBySchool(schoolId: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE parentId = :parentId")
    fun getAllStudentsByParent(parentId: Long): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: Long): Student?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Query("SELECT COUNT(*) FROM students WHERE schoolId = :schoolId")
    suspend fun getStudentCountForSchool(schoolId: String): Int
}

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects WHERE belongsToId = :belongsToId")
    fun getSubjects(belongsToId: String): Flow<List<Subject>>

    @Query("SELECT * FROM subjects WHERE belongsToId = :belongsToId")
    suspend fun getSubjectsSync(belongsToId: String): List<Subject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubjectById(subjectId: Long)

    @Update
    suspend fun updateSubject(subject: Subject)
}

@Dao
interface MarkDao {
    @Query("SELECT * FROM marks WHERE studentId = :studentId")
    fun getMarksForStudent(studentId: Long): Flow<List<Mark>>

    @Query("SELECT * FROM marks WHERE studentId = :studentId")
    suspend fun getMarksForStudentSync(studentId: Long): List<Mark>

    @Query("SELECT * FROM marks WHERE studentId = :studentId AND subjectId = :subjectId AND examType = :examType LIMIT 1")
    suspend fun getMark(studentId: Long, subjectId: Long, examType: String): Mark?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMark(mark: Mark): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarks(marks: List<Mark>)

    @Update
    suspend fun updateMark(mark: Mark)

    @Query("DELETE FROM marks WHERE studentId = :studentId")
    suspend fun deleteMarksByStudent(studentId: Long)

    @Query("DELETE FROM marks WHERE subjectId = :subjectId")
    suspend fun deleteMarksBySubject(subjectId: Long)

    @Query("DELETE FROM marks WHERE examType = :examType")
    suspend fun deleteMarksByExamType(examType: String)
}

@Dao
interface TestTypeDao {
    @Query("SELECT * FROM test_types WHERE belongsToId = :belongsToId")
    fun getTestTypes(belongsToId: String): Flow<List<TestType>>

    @Query("SELECT * FROM test_types WHERE belongsToId = :belongsToId")
    suspend fun getTestTypesSync(belongsToId: String): List<TestType>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestType(testType: TestType): Long

    @Query("DELETE FROM test_types WHERE id = :testTypeId")
    suspend fun deleteTestTypeById(testTypeId: Long)

    @Update
    suspend fun updateTestType(testType: TestType)
}

@Dao
interface PaymentRecordDao {
    @Query("SELECT * FROM payment_records WHERE userId = :userId ORDER BY timestamp DESC")
    fun getPaymentsForUser(userId: Long): Flow<List<PaymentRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentRecord): Long
}

@Database(
    entities = [
        AppConfig::class,
        UserAccount::class,
        Student::class,
        Subject::class,
        Mark::class,
        PaymentRecord::class,
        TestType::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appConfigDao(): AppConfigDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun studentDao(): StudentDao
    abstract fun subjectDao(): SubjectDao
    abstract fun markDao(): MarkDao
    abstract fun testTypeDao(): TestTypeDao
    abstract fun paymentRecordDao(): PaymentRecordDao
}
