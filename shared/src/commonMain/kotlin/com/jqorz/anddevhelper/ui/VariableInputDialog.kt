package com.jqorz.anddevhelper.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jqorz.anddevhelper.model.AdbCommand

@Composable
fun VariableInputDialog(
    command: AdbCommand,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, String>) -> Unit,
) {
    val variables = remember(command) { command.requiredVariables }
    val values = remember { mutableStateMapOf<String, String>() }
    var errors by remember { mutableStateOf(setOf<String>()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.6f),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "输入参数 — ${command.name}",
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = command.template,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Spacer(Modifier.height(20.dp))

                variables.forEach { variable ->
                    val label = when (variable) {
                        "package" -> "包名 (package)"
                        "activity" -> "Activity 类名"
                        "ip" -> "IP 地址"
                        "local" -> "本地文件路径"
                        "remote" -> "设备文件路径"
                        else -> variable
                    }
                    OutlinedTextField(
                        value = values[variable] ?: "",
                        onValueChange = {
                            values[variable] = it
                            errors = errors - variable
                        },
                        label = { Text(label) },
                        isError = variable in errors,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val missing = variables.filter { values[it].isNullOrBlank() }.toSet()
                        if (missing.isNotEmpty()) {
                            errors = missing
                        } else {
                            onConfirm(variables.associateWith { values[it]!! })
                        }
                    }) {
                        Text("执行")
                    }
                }
            }
        }
    }
}
