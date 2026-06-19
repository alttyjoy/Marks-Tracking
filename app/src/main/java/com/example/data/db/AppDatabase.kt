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
import com.example.data.sync.SyncQueueEntry
import com.example.data.sync.SyncQueueDao
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<Subject>)

    @Query("DELETE FROM subjects WHERE id = :subjectId")
    suspend fun deleteSubjectById(subjectId: Long)

    @Update
    suspend fun updateSubject(subject: Subject)
}

@Dao
interface MarkDao {
    @Query("SELECT * FROM marks")
    fun getAllMarks(): Flow<List<Mark>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestTypes(testTypes: List<TestType>)

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
        TestType::class,
        SyncQueueEntry::class
    ],
    version = 7,
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
    abstract fun syncQueueDao(): SyncQueueDao
}

val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `payment_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `userId` INTEGER NOT NULL, `userName` TEXT NOT NULL, `userEmail` TEXT NOT NULL, `planType` TEXT NOT NULL, `basePrice` REAL NOT NULL, `gstAmount` REAL NOT NULL, `totalAmount` REAL NOT NULL, `paymentGateway` TEXT NOT NULL, `paymentId` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `test_types` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `belongsToId` TEXT NOT NULL)")

        // Add potentially missing columns safely
        try {
            db.execSQL("ALTER TABLE `user_accounts` ADD COLUMN `associatedStudentId` INTEGER")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            db.execSQL("ALTER TABLE `user_accounts` ADD COLUMN `belongsToOwnerId` INTEGER")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            db.execSQL("ALTER TABLE `students` ADD COLUMN `parentId` INTEGER")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            db.execSQL("ALTER TABLE `students` ADD COLUMN `parentName` TEXT NOT NULL DEFAULT ''")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            db.execSQL("ALTER TABLE `students` ADD COLUMN `schoolName` TEXT NOT NULL DEFAULT ''")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            db.execSQL("ALTER TABLE `marks` ADD COLUMN `maxMarks` REAL NOT NULL DEFAULT 100.0")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `sync_queue` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `entityType` TEXT NOT NULL, `operation` TEXT NOT NULL, `entityId` INTEGER NOT NULL, `payloadJson` TEXT NOT NULL, `timestamp` INTEGER NOT NULL)")
    }
}
