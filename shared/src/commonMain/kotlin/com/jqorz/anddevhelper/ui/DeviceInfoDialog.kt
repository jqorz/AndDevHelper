package com.jqorz.anddevhelper.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jqorz.anddevhelper.model.DeviceInfo

@Composable
fun DeviceInfoDialog(
    deviceInfo: DeviceInfo,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("设备信息", fontWeight = FontWeight.Bold)
        },
        text = {
            if (deviceInfo.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("正在获取设备信息...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else if (deviceInfo.errorMessage.isNotEmpty()) {
                Text(
                    text = deviceInfo.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(min = 320.dp, max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    InfoSection("基本信息") {
                        InfoRow("品牌", deviceInfo.brand)
                        InfoRow("制造商", deviceInfo.manufacturer)
                        InfoRow("设备型号", deviceInfo.model)
                        InfoRow("主板", deviceInfo.board)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    InfoSection("系统信息") {
                        InfoRow("Android 版本", deviceInfo.androidVersion)
                        InfoRow("SDK 版本", deviceInfo.sdkVersion)
                        InfoRow("Build ID", deviceInfo.buildId)
                        InfoRow("安全补丁", deviceInfo.securityPatch)
                        InfoRow("系统指纹", deviceInfo.fingerprint, selectable = true)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    InfoSection("硬件信息") {
                        InfoRow("CPU 架构", deviceInfo.cpuAbi)
                        InfoRow("CPU 硬件", deviceInfo.cpuHardware)
                        InfoRow("总内存", deviceInfo.totalMemory)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    InfoSection("屏幕信息") {
                        InfoRow("分辨率", deviceInfo.screenSize)
                        InfoRow("密度", if (deviceInfo.screenDensity.isNotEmpty()) "${deviceInfo.screenDensity} dpi" else "")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    InfoSection("其他") {
                        InfoRow("序列号", deviceInfo.serialno, selectable = true)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun InfoSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Column(
            modifier = Modifier.padding(start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    selectable: Boolean = false,
) {
    if (value.isNotEmpty()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
    }
}
