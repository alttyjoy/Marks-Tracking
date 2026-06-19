package com.example.data.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String, // "STUDENT", "MARK", "SUBJECT", "TEST_TYPE"
    val operation: String, // "INSERT_OR_UPDATE", "DELETE"
    val entityId: Long, // local ID reference
    val payloadJson: String, // serialized entity content using native JSON
    val timestamp: Long = System.currentTimeMillis()
)
