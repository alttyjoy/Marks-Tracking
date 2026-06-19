package com.example.data.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    fun getAllQueueEntriesFlow(): Flow<List<SyncQueueEntry>>

    @Query("SELECT * FROM sync_queue ORDER BY timestamp ASC")
    suspend fun getAllQueueEntries(): List<SyncQueueEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueEntry(entry: SyncQueueEntry): Long

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteQueueEntryById(id: Long)

    @Query("DELETE FROM sync_queue")
    suspend fun clearQueue()
}
