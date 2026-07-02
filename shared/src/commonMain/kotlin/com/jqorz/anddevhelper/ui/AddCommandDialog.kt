package com.jqorz.anddevhelper.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jqorz.anddevhelper.model.AdbCommand
import com.jqorz.anddevhelper.model.CommandTab

@Composable
fun AddCommandDialog(
    editingCommand: AdbCommand?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, template: String, tabId: String) -> Unit,
    onUpdate: (command: AdbCommand, name: String, template: String) -> Unit,
) {
    var name by remember(editingCommand) { mutableStateOf(editingCommand?.name ?: "") }
    var template by remember(editingCommand) { mutableStateOf(editingCommand?.template ?: "") }
    var selectedTab by remember(editingCommand) {
        mutableStateOf(CommandTab.fromId(editingCommand?.tabId ?: CommandTab.DEVICE_INFO.id))
    }
    var nameError by remember { mutableStateOf(false) }
    var templateError by remember { mutableStateOf(false) }

    val isEditing = editingCommand != null

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.7f),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = if (isEditing) "编辑命令" else "添加自定义命令",
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(Modifier.height(20.dp))

                // 命令名称
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; nameError = false },
                    label = { Text("命令名称") },
                    isError = nameError,
                    supportingText = if (nameError) {{ Text("请输入命令名称") }} else null,
                    singleLine = true,
                    enabled = !isEditing || editingCommand?.isPreset != true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                // ADB 命令模板
                OutlinedTextField(
                    value = template,
                    onValueChange = { template = it; templateError = false },
                    label = { Text("ADB 命令") },
                    placeholder = { Text("adb -s {device} shell ...") },
                    isError = templateError,
                    supportingText = if (templateError) {
                        { Text("请输入命令模板") }
                    } else {
                        { Text("支持变量: {device} {package} {ip} {local} {remote}") }
                    },
                    minLines = 2,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(12.dp))

                // 所属 Tab 选择（仅新建时可选）
                if (!isEditing) {
                    Text("所属分类", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        CommandTab.entries.forEach { tab ->
                            FilterChip(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                label = { Text(tab.displayName, style = MaterialTheme.typography.labelSmall) },
                            )
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 按钮
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        nameError = name.isBlank()
                        templateError = template.isBlank()
                        if (!nameError && !templateError) {
                            if (isEditing) {
                                onUpdate(editingCommand!!, name, template)
                            } else {
                                onConfirm(name, template, selectedTab.id)
                            }
                        }
                    }) {
                        Text(if (isEditing) "保存" else "添加")
                    }
                }
            }
        }
    }
}
