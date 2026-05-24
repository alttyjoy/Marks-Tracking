package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// --- SaaS System Configuration Entity (Setup Wizard) ---
@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,
    val dbHost: String = "",
    val dbName: String = "",
    val dbUser: String = "",
    val dbPass: String = "",
    
    val smtpHost: String = "",
    val smtpPort: String = "",
    val smtpUser: String = "",
    val smtpPass: String = "",
    
    val gatewayKey: String = "",
    val gatewaySecret: String = "",
    val gatewayType: String = "Razorpay", // Razorpay or PayU
    
    val adminName: String = "",
    val adminEmail: String = "",
    val adminPassHash: String = "",
    
    val setupComplete: Boolean = false
)

// --- User Accounts ---
@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String, // "SCHOOL_ADMIN", "INDIVIDUAL_PARENT", "VIEW_ONLY_PARENT"
    val planType: String = "FREE", // "FREE", "INDIVIDUAL_PARENT_PLAN" (₹100/yr), "SCHOOL_PLAN" (₹10,000/yr)
    val schoolId: String = "", // tenancy identifier
    val associatedStudentId: Long? = null, // Used for "View-Only" parents under School accounts
    val belongsToOwnerId: Long? = null // back-link to the school admin/creator
)

// --- Student (Names are encrypted via AES-256 before saving) ---
@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val encryptedName: String,
    val rollNo: String = "",
    val studentClass: String = "",
    val schoolId: String = "", // school tenant link
    val parentId: Long? = null // parent account link (if separate user role)
)

// --- Subject ---
@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val belongsToId: String = "" // can be a schoolId or direct parent ID for rows-isolation
)

// --- TestType ---
@Entity(tableName = "test_types")
data class TestType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val belongsToId: String = "" // can be a schoolId or direct parent ID for rows-isolation
)

// --- Mark ---
@Entity(tableName = "marks")
data class Mark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val subjectId: Long,
    val examType: String, // "Weekly", "Monthly", "Quarterly", "Half-Yearly", "Annual"
    val marksObtained: Double,
    val maxMarks: Double = 100.0
)

// --- Billing/Payments Invoice Records ---
@Entity(tableName = "payment_records")
data class PaymentRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val userEmail: String,
    val planType: String, // "INDIVIDUAL_PARENT_PLAN", "SCHOOL_PLAN"
    val basePrice: Double, // base price (e.g. 100 or 10000)
    val gstAmount: Double, // 18% GST (e.g. 18 or 1800)
    val totalAmount: Double, // base + GST
    val paymentGateway: String, // "Razorpay" or "PayU"
    val paymentId: String, // gateway transaction ID (e.g. pay_N2hK91x...)
    val timestamp: Long = System.currentTimeMillis()
)
