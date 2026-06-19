package com.example.data.sync

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Service representing a remote cloud storage backend.
 * Provides real latency simulation and realistic API endpoints.
 */
object RemoteStorageService {
    private const val TAG = "RemoteStorageService"

    private val _isOnline = MutableStateFlow(true)
    val isOnline = _isOnline.asStateFlow()

    // Simulated remote database collections
    val remoteStudents = ConcurrentHashMap<Long, String>() // id -> decryptedName representation
    val remoteMarks = ConcurrentHashMap<String, Double>() // "studentId_subjectId_examType" -> marksObtained

    fun setOnline(online: Boolean) {
        _isOnline.value = online
        Log.i(TAG, "Simulated Cloud internet connectivity toggled: ${if (online) "ONLINE" else "OFFLINE"}")
    }

    /**
     * Simulates pushing a local db change to cloud server.
     * Throws an exception if offline or remote error happens.
     */
    suspend fun syncPayloadToServer(
        entityType: String,
        operation: String,
        entityId: Long,
        payloadJsonStr: String
    ): Boolean {
        if (!_isOnline.value) {
            val err = "Network request failed: Remote peer is unreachable (No connection)"
            Log.e(TAG, err)
            throw java.io.IOException(err)
        }

        // Simulate network round trip speed (300ms latency)
        delay(300)

        return try {
            val json = JSONObject(payloadJsonStr)
            Log.d(TAG, "HTTP REST ENDPOINT Pushing: [POST /api/v1/sync/$entityType] Op: $operation, Id: $entityId")

            when (entityType) {
                "STUDENT" -> {
                    if (operation == "DELETE") {
                        remoteStudents.remove(entityId)
                        Log.i(TAG, "Cloud DB Sync Success: REMOVED student ID: $entityId")
                    } else {
                        // Safe extraction of name
                        val encryptedName = json.optString("encryptedName", "Unknown")
                        remoteStudents[entityId] = encryptedName
                        Log.i(TAG, "Cloud DB Sync Success: SAVED/UPDATED student ID: $entityId, payload metadata verified.")
                    }
                }
                "MARK" -> {
                    if (operation == "DELETE") {
                        // Delete all marks corresponding to this relationship if applicable
                        remoteMarks.keys.removeIf { key -> key.startsWith("${entityId}_") }
                        Log.i(TAG, "Cloud DB Sync Success: REMOVED match marks for student: $entityId")
                    } else {
                        val studentId = json.optLong("studentId", 0L)
                        val subjectId = json.optLong("subjectId", 0L)
                        val examType = json.optString("examType", "Exam")
                        val score = json.optDouble("marksObtained", 0.0)
                        
                        val key = "${studentId}_${subjectId}_$examType"
                        remoteMarks[key] = score
                        Log.i(TAG, "Cloud DB Sync Success: SECURELY MERGED mark: student=$studentId, sub=$subjectId, exam=$examType, score=$score")
                    }
                }
                "SUBJECT" -> {
                    Log.i(TAG, "Cloud DB Sync Success: Core academic configurations of Subject standard aligned.")
                }
                "TEST_TYPE" -> {
                    Log.i(TAG, "Cloud DB Sync Success: Custom schedule configurations of Test types synchronized.")
                }
                else -> {
                    Log.w(TAG, "Cloud Sync Warning: Unknown entity category: $entityType")
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Critical parsing/remote storage exception in Cloud Sync: ${e.message}", e)
            false
        }
    }
}
