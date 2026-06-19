package com.example.data.sync

import android.util.Log
import com.example.data.repository.MarksRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

enum class SyncState {
    IDLE,
    SYNCING,
    ERROR,
    OFFLINE
}

/**
 * Service orchestrator that consumes local SyncQueueEntry records when online,
 * sending items sequentially to ensuring remote consistency.
 */
object SyncService {
    private const val TAG = "SyncService"
    private var serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null
    
    private var repository: MarksRepository? = null

    private val _syncState = MutableStateFlow(SyncState.IDLE)
    val syncState = _syncState.asStateFlow()

    private val _pendingCount = MutableStateFlow(0)
    val pendingCount = _pendingCount.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp = _lastSyncTimestamp.asStateFlow()

    fun initialize(repo: MarksRepository) {
        this.repository = repo
        Log.i(TAG, "Initializing background consistency sync service loop.")
        
        // Start continuous sync trigger monitoring
        monitorConnectivityAndBacklog()
    }

    private fun monitorConnectivityAndBacklog() {
        serviceScope.launch {
            // Monitor RemoteStorageService.isOnline to update state immediately
            launch {
                RemoteStorageService.isOnline.collect { online ->
                    if (!online) {
                        _syncState.value = SyncState.OFFLINE
                    } else {
                        if (_syncState.value == SyncState.OFFLINE) {
                            _syncState.value = SyncState.IDLE
                            // Auto trigger sync since connectivity returned
                            triggerSync()
                        }
                    }
                }
            }

            // Monitor backlog size to keep state flows fresh
            launch {
                val repo = repository ?: return@launch
                while (isActive) {
                    try {
                        val count = repo.syncQueueDao.getAllQueueEntries().size
                        _pendingCount.value = count
                    } catch (e: Exception) {
                        Log.e(TAG, "Error fetching queue size: ${e.message}")
                    }
                    delay(2000)
                }
            }

            // Perpetual low-frequency sync trigger (every 10 seconds checking if online for safety)
            while (isActive) {
                if (RemoteStorageService.isOnline.value && _pendingCount.value > 0) {
                    triggerSync()
                }
                delay(10000)
            }
        }
    }

    /**
     * Executes the sync cycle by reading from database, uploading to cloud,
     * and clearing the local syncing entries.
     */
    fun triggerSync() {
        if (syncJob?.isActive == true) {
            Log.d(TAG, "Sync cycle is already execution active. Aggregation omitted.")
            return
        }

        val repo = repository
        if (repo == null) {
            Log.e(TAG, "Cannot start sync: MarksRepository has not been initialized.")
            return
        }

        if (!RemoteStorageService.isOnline.value) {
            Log.w(TAG, "Sync trigger skipped: Simulated internet is offfline.")
            _syncState.value = SyncState.OFFLINE
            return
        }

        syncJob = serviceScope.launch {
            _syncState.value = SyncState.SYNCING
            Log.i(TAG, "Starting secure sync cycle. Fetching backlog queue items...")
            
            try {
                var queue = repo.syncQueueDao.getAllQueueEntries()
                _pendingCount.value = queue.size

                while (queue.isNotEmpty() && RemoteStorageService.isOnline.value) {
                    val entry = queue.first()
                    Log.v(TAG, "Processing backlog: ${entry.entityType} ID: ${entry.entityId} OP: ${entry.operation}")

                    val success = RemoteStorageService.syncPayloadToServer(
                        entityType = entry.entityType,
                        operation = entry.operation,
                        entityId = entry.entityId,
                        payloadJsonStr = entry.payloadJson
                    )

                    if (success) {
                        repo.syncQueueDao.deleteQueueEntryById(entry.id)
                        Log.v(TAG, "Backlog item cleared: Entry index ID ${entry.id}")
                    } else {
                        throw java.lang.IllegalStateException("Payload sync rejected by remote peer.")
                    }

                    // Refresh queue list
                    queue = repo.syncQueueDao.getAllQueueEntries()
                    _pendingCount.value = queue.size
                }

                if (_pendingCount.value == 0) {
                    _lastSyncTimestamp.value = System.currentTimeMillis()
                    _syncState.value = SyncState.IDLE
                    Log.i(TAG, "All local encrypted database updates are now consistent with Remote Cloud!")
                } else {
                    _syncState.value = SyncState.ERROR
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception encountered during cloud database sync cycle: ${e.message}", e)
                _syncState.value = SyncState.ERROR
            }
        }
    }

    fun shutdown() {
        serviceScope.cancel("Sync service shutdown requested.")
        serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    }
}
