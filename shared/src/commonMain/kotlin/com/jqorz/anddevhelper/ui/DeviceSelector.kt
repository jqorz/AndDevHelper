package com.jqorz.anddevhelper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.jqorz.anddevhelper.model.AdbDevice
import com.jqorz.anddevhelper.model.DeviceStatus

@Composable
fun DeviceSelector(
    devices: List<AdbDevice>,
    selectedDevice: AdbDevice?,
    isRefreshing: Boolean,
    adbValid: Boolean,
    onSelectDevice: (AdbDevice) -> Unit,
    onRefresh: () -> Unit,
    onShowInfo: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "设备:",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(end = 8.dp),
            )

            if (!adbValid) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(
                        text = "ADB 未配置",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            } else if (devices.isEmpty() && !isRefreshing) {
                Text(
                    text = "未检测到设备",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    devices.forEach { device ->
                        val isSelected = device.serial == selectedDevice?.serial
                        val bgColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            device.status == DeviceStatus.ONLINE -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        }
                        val textColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            device.status == DeviceStatus.ONLINE -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.onErrorContainer
                        }
                        val statusIcon = when (device.status) {
                            DeviceStatus.ONLINE -> "●"
                            DeviceStatus.OFFLINE -> "○"
                            DeviceStatus.UNAUTHORIZED -> "◐"
                        }
                        val statusColor = when (device.status) {
                            DeviceStatus.ONLINE -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                            DeviceStatus.OFFLINE -> MaterialTheme.colorScheme.error
                            DeviceStatus.UNAUTHORIZED -> androidx.compose.ui.graphics.Color(0xFFFFC107)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(bgColor)
                                .clickable { onSelectDevice(device) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(statusIcon, style = MaterialTheme.typography.bodySmall, color = statusColor)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = device.displayName,
                                    color = textColor,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            // 刷新按钮
            IconButton(onClick = onRefresh, enabled = !isRefreshing && adbValid) {
                if (isRefreshing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Refresh, contentDescription = "刷新设备")
                }
            }

            // 设备信息按钮
            IconButton(onClick = onShowInfo, enabled = selectedDevice != null && adbValid) {
                Icon(Icons.Default.Info, contentDescription = "设备信息")
            }

            // 设置按钮
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "设置")
            }
        }
    }
}
