package com.example.ui.screens

import android.text.format.DateFormat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sync.RemoteStorageService
import com.example.data.sync.SyncService
import com.example.data.sync.SyncState
import java.util.Date

@Composable
fun CloudSyncStatusPanel(
    modifier: Modifier = Modifier
) {
    val isOnline by RemoteStorageService.isOnline.collectAsState()
    val syncState by SyncService.syncState.collectAsState()
    val pendingCount by SyncService.pendingCount.collectAsState()
    val lastSync by SyncService.lastSyncTimestamp.collectAsState()

    // Determine color schema based on sync state
    val statusColor by animateColorAsState(
        targetValue = when {
            !isOnline -> Color(0xFF9E9E9E) // Gray
            syncState == SyncState.SYNCING -> MaterialTheme.colorScheme.primary // Blue
            syncState == SyncState.ERROR -> MaterialTheme.colorScheme.error // Red
            pendingCount > 0 -> Color(0xFFF57C00) // Orange (Alert pending)
            else -> Color(0xFF4CAF50) // Green (Synced)
        },
        label = "statusColor"
    )

    val labelText = when {
        !isOnline -> "Simulated OFFLINE Mode"
        syncState == SyncState.SYNCING -> "Cloud Synchronizing..."
        syncState == SyncState.ERROR -> "Remote Sync Conflict!"
        pendingCount > 0 -> "Local Modifications Queued"
        else -> "Fully Synced to Remote Storage"
    }

    val icon = when {
        !isOnline -> Icons.Default.CloudOff
        syncState == SyncState.SYNCING -> Icons.Default.Sync
        syncState == SyncState.ERROR -> Icons.Default.CloudSync
        pendingCount > 0 -> Icons.Default.HourglassEmpty
        else -> Icons.Default.CloudDone
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = statusColor.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(statusColor)
                    )
                    Text(
                        text = "Remote Cloud Sync Engine",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Interactive Connection Simulator Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (isOnline) "ONLINE" else "OFFLINE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    Switch(
                        checked = isOnline,
                        onCheckedChange = { RemoteStorageService.setOnline(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFE0E0E0)
                        ),
                        modifier = Modifier.scale(0.85f)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Large status icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = labelText,
                        tint = statusColor,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = labelText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when {
                            pendingCount > 0 -> "$pendingCount backlog items pending upload"
                            else -> "Eventually consistent & offline-first protected"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Sync button
                Button(
                    onClick = { SyncService.triggerSync() },
                    enabled = isOnline && pendingCount > 0 && syncState != SyncState.SYNCING,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Force sync now",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = lastSync > 0L) {
                Text(
                    text = "Last synced: " + DateFormat.format("hh:mm:ss a, dd MMM yyyy", Date(lastSync)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
