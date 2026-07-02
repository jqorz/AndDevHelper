package com.jqorz.anddevhelper.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jqorz.anddevhelper.model.CommandResult

@Composable
fun CommandResultDialog(
    result: CommandResult,
    onDismiss: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(0.7f),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // 标题栏
                Row {
                    Text(
                        text = if (result.isSuccess) "执行成功" else "执行失败",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (result.isSuccess)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = {
                        clipboardManager.setText(AnnotatedString(result.output))
                    }) {
                        Text("复制")
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 结果内容
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    val displayText = when {
                        result.errorMessage.isNotBlank() -> "错误: ${result.errorMessage}"
                        result.output.isBlank() -> "(无输出)"
                        else -> result.output
                    }
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState()),
                    )
                }

                Spacer(Modifier.height(16.dp))

                // 底部信息
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text(
                        text = "退出码: ${result.exitCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(16.dp))
                    Button(onClick = onDismiss) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}
