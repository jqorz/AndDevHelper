package com.jqorz.anddevhelper.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jqorz.anddevhelper.model.AppSettings
import com.jqorz.anddevhelper.model.ThemeMode

@Composable
fun SettingsDialog(
    settings: AppSettings,
    adbValid: Boolean,
    onDismiss: () -> Unit,
    onSave: (AppSettings) -> Unit,
    onBrowseAdb: () -> Unit,
) {
    var edited by remember(settings) { mutableStateOf(settings) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("设置", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(20.dp))

                // ADB 路径
                Text("ADB 路径", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = edited.adbPath,
                        onValueChange = { edited = edited.copy(adbPath = it) },
                        placeholder = { Text("从环境变量自动查找...") },
                        singleLine = true,
                        isError = edited.adbPath.isNotBlank() && !adbValid,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = onBrowseAdb) {
                        Text("浏览")
                    }
                }
                if (edited.adbPath.isNotBlank()) {
                    Text(
                        text = if (adbValid) "✓ ADB 路径有效" else "✗ ADB 路径无效",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (adbValid)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 命令超时
                Text("命令超时时间（秒）", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = edited.commandTimeout.toString(),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let { edited = edited.copy(commandTimeout = it) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(16.dp))

                // 自动刷新
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("自动刷新设备", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Switch(
                        checked = edited.autoRefreshDevices,
                        onCheckedChange = { edited = edited.copy(autoRefreshDevices = it) },
                    )
                }
                if (edited.autoRefreshDevices) {
                    OutlinedTextField(
                        value = edited.refreshInterval.toString(),
                        onValueChange = { v ->
                            v.toIntOrNull()?.let { edited = edited.copy(refreshInterval = it) }
                        },
                        label = { Text("刷新间隔（秒）") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 主题
                Text("主题模式", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = edited.themeMode == ThemeMode.LIGHT,
                        onClick = { edited = edited.copy(themeMode = ThemeMode.LIGHT) },
                        label = { Text("亮色") },
                    )
                    FilterChip(
                        selected = edited.themeMode == ThemeMode.DARK,
                        onClick = { edited = edited.copy(themeMode = ThemeMode.DARK) },
                        label = { Text("暗色") },
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 历史记录数量
                Text("命令历史记录数量", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = edited.historyLimit.toString(),
                    onValueChange = { v ->
                        v.toIntOrNull()?.let { edited = edited.copy(historyLimit = it) }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(24.dp))

                // 按钮
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("取消") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onSave(edited) }) { Text("保存") }
                }
            }
        }
    }
}
