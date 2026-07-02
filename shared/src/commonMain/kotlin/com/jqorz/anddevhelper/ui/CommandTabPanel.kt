package com.jqorz.anddevhelper.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jqorz.anddevhelper.model.AdbCommand
import com.jqorz.anddevhelper.model.CommandTab

@Composable
fun CommandTabPanel(
    tabs: Array<CommandTab>,
    selectedTab: CommandTab,
    commands: List<AdbCommand>,
    executingCommandId: String?,
    onSelectTab: (CommandTab) -> Unit,
    onExecuteCommand: (AdbCommand) -> Unit,
    onEditCommand: (AdbCommand) -> Unit,
    onDeleteCommand: (AdbCommand) -> Unit,
    onAddCommand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // Tab 栏
        TabRow(selectedTabIndex = tabs.indexOf(selectedTab)) {
            tabs.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { onSelectTab(tab) },
                    text = { Text(tab.displayName) },
                )
            }
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
                    onClick = onAddCommand,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("添加自定义命令")
                }
            }
        }
    }
}
