package com.jqorz.anddevhelper.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.jqorz.anddevhelper.model.AdbCommand
import com.jqorz.anddevhelper.model.CommandTab

@Composable
fun CommandTabPanel(
    tabs: List<CommandTab>,
    selectedTab: CommandTab,
    commands: List<AdbCommand>,
    executingCommandId: String?,
    onSelectTab: (CommandTab) -> Unit,
    onExecuteCommand: (AdbCommand) -> Unit,
    onEditCommand: (AdbCommand) -> Unit,
    onDeleteCommand: (AdbCommand) -> Unit,
    onAddCommand: (CommandTab) -> Unit,
    onAddTab: (String) -> Unit,
    onRenameTab: (CommandTab, String) -> Unit,
    onDeleteTab: (CommandTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddTabDialog by remember { mutableStateOf(false) }
    var tabToRename by remember { mutableStateOf<CommandTab?>(null) }
    var tabMenuTarget by remember { mutableStateOf<CommandTab?>(null) }

    Column(modifier = modifier) {
        // Tab 栏
        TabRow(selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)) {
            tabs.forEach { tab ->
                if (tab.isPreset) {
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { onSelectTab(tab) },
                        text = { Text(tab.displayName) },
                    )
                } else {
                    Box {
                        Tab(
                            selected = selectedTab == tab,
                            onClick = { onSelectTab(tab) },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(tab.displayName)
                                    Spacer(Modifier.width(2.dp))
                                    IconButton(
                                        onClick = { tabMenuTarget = tab },
                                        modifier = Modifier.size(18.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "更多",
                                            modifier = Modifier.size(14.dp),
                                        )
                                    }
                                }
                            },
                        )
                        DropdownMenu(
                            expanded = tabMenuTarget == tab,
                            onDismissRequest = { tabMenuTarget = null },
                        ) {
                            DropdownMenuItem(
                                text = { Text("重命名") },
                                onClick = {
                                    tabToRename = tab
                                    tabMenuTarget = null
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    onDeleteTab(tab)
                                    tabMenuTarget = null
                                },
                            )
                        }
                    }
                }
            }
            // 添加 Tab 按钮
            Tab(
                selected = false,
                onClick = { showAddTabDialog = true },
                text = { Icon(Icons.Default.Add, contentDescription = "添加分类", modifier = Modifier.size(18.dp)) },
            )
        }

        // 命令按钮网格
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 180.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                ) {
                    items(commands, key = { it.id }) { command ->
                        CommandButton(
                            command = command,
                            isExecuting = executingCommandId == command.id,
                            onClick = { onExecuteCommand(command) },
                            onEdit = { onEditCommand(command) },
                            onDelete = { onDeleteCommand(command) },
                        )
                    }
                }

                // 添加命令按钮
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onAddCommand(selectedTab) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("添加自定义命令")
                }
            }
        }
    }

    // 添加 Tab 弹窗
    if (showAddTabDialog) {
        TabNameDialog(
            title = "添加分类",
            initialName = "",
            onDismiss = { showAddTabDialog = false },
            onConfirm = { name ->
                onAddTab(name)
                showAddTabDialog = false
            },
        )
    }

    // 重命名 Tab 弹窗
    tabToRename?.let { tab ->
        TabNameDialog(
            title = "重命名分类",
            initialName = tab.displayName,
            onDismiss = { tabToRename = null },
            onConfirm = { name ->
                onRenameTab(tab, name)
                tabToRename = null
            },
        )
    }
}

@Composable
private fun TabNameDialog(
    title: String,
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier.widthIn(min = 300.dp, max = 400.dp),
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; isError = false },
                    label = { Text("分类名称") },
                    isError = isError,
                    supportingText = if (isError) {{ Text("请输入名称") }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        isError = name.isBlank()
                        if (!isError) onConfirm(name.trim())
                    }) {
                        Text("确定")
                    }
                }
            }
        }
    }
}
