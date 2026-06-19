package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationSeverity
import com.example.data.model.UserNotification
import com.example.ui.viewmodel.MarksViewModel

@Composable
fun UserNotificationOverlay(
    viewModel: MarksViewModel,
    modifier: Modifier = Modifier
) {
    val notifications = viewModel.uiNotifications

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(if (notifications.isNotEmpty()) 1.1f else 1f)
        ) {
            items(notifications, key = { it.id }) { alert ->
                NotificationToast(
                    alert = alert,
                    onDismiss = { viewModel.dismissNotification(alert.id) }
                )
            }
        }
    }
}

@Composable
fun NotificationToast(
    alert: UserNotification,
    onDismiss: () -> Unit
) {
    val containerColor = when (alert.severity) {
        NotificationSeverity.ERROR -> Color(0xFFFDE8E8)
        NotificationSeverity.WARNING -> Color(0xFFFEF3C7)
        NotificationSeverity.INFO -> Color(0xFFE1EFFE)
    }

    val contentColor = when (alert.severity) {
        NotificationSeverity.ERROR -> Color(0xFF9B1C1C)
        NotificationSeverity.WARNING -> Color(0xFF92400E)
        NotificationSeverity.INFO -> Color(0xFF1E429F)
    }

    val icon = when (alert.severity) {
        NotificationSeverity.ERROR -> Icons.Default.Warning
        NotificationSeverity.WARNING -> Icons.Default.Warning
        NotificationSeverity.INFO -> Icons.Default.Info
    }

    val label = when (alert.severity) {
        NotificationSeverity.ERROR -> "SYSTEM ERROR"
        NotificationSeverity.WARNING -> "SYSTEM ALERT"
        NotificationSeverity.INFO -> "SYSTEM INFO"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "${alert.title} [$label]",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = contentColor,
                        letterSpacing = 0.5.sp
                    )
                )
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = contentColor.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium
                    )
                )
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss notification",
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
