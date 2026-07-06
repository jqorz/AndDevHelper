package com.jqorz.anddevhelper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.jqorz.anddevhelper.model.CommandTab
import com.jqorz.anddevhelper.model.ThemeMode
import com.jqorz.anddevhelper.ui.*
import com.jqorz.anddevhelper.ui.theme.AndDevHelperTheme
import com.jqorz.anddevhelper.viewmodel.MainViewModel
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

@Composable
fun App(viewModel: MainViewModel = remember { MainViewModel().also { it.init() } }) {
    val isDark = when (viewModel.settings.themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    AndDevHelperTheme(isDark = isDark) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶部设备选择栏
                DeviceSelector(
                    devices = viewModel.devices,
                    selectedDevice = viewModel.selectedDevice,
                    isRefreshing = viewModel.isRefreshingDevices,
                    adbValid = viewModel.adbValid,
                    onSelectDevice = { viewModel.selectDevice(it) },
                    onRefresh = { viewModel.refreshDevices() },
                    onShowInfo = { viewModel.showDeviceInfo() },
                    onOpenSettings = { viewModel.showSettings() },
                )

                // 中部 Tab 面板
                CommandTabPanel(
                    tabs = viewModel.allTabs,
                    selectedTab = viewModel.selectedTab,
                    commands = viewModel.commandsForCurrentTab(),
                    executingCommandId = viewModel.executingCommandId,
                    onSelectTab = { viewModel.selectTab(it) },
                    onExecuteCommand = { viewModel.executeCommand(it) },
                    onEditCommand = { viewModel.showEditCommand(it) },
                    onDeleteCommand = { viewModel.deleteCommand(it) },
                    onAddCommand = { viewModel.showAddCommand(it) },
                    onAddTab = { viewModel.addTab(it) },
                    onRenameTab = { tab, name -> viewModel.renameTab(tab, name) },
                    onDeleteTab = { viewModel.deleteTab(it) },
                    modifier = Modifier.weight(1f),
                )

                // 底部 APK 安装区
                ApkInstaller(
                    isInstalling = viewModel.isInstallingApk,
                    installResult = viewModel.installResult,
                    hasDevice = viewModel.selectedDevice != null,
                    onInstall = { viewModel.installApk(it) },
                    onClearResult = { viewModel.clearInstallResult() },
                    onBrowseFile = {
                        val path = openFileDialog("选择 APK 文件", listOf("apk"))
                        if (path != null) viewModel.installApk(path)
                    },
                )
            }
        }

        // 弹窗们
        if (viewModel.showSettingsDialog) {
            SettingsDialog(
                settings = viewModel.settings,
                adbValid = viewModel.adbValid,
                onDismiss = { viewModel.dismissSettings() },
                onSave = { viewModel.updateSettings(it) },
                onBrowseAdb = {
                    val path = openFileDialog("选择 ADB 可执行文件", listOf("exe", ""))
                    if (path != null) {
                        viewModel.updateSettings(viewModel.settings.copy(adbPath = path))
                    }
                },
            )
        }

        if (viewModel.showAddCommandDialog) {
            AddCommandDialog(
                editingCommand = viewModel.editingCommand,
                tabs = viewModel.allTabs,
                defaultTab = viewModel.addCommandDefaultTab,
                onDismiss = { viewModel.dismissAddCommand() },
                onConfirm = { name, template, tabId ->
                    viewModel.addCustomCommand(name, template, tabId)
                },
                onUpdate = { command, name, template ->
                    viewModel.updateCommand(command, name, template)
                },
            )
        }

        viewModel.showCommandResultDialog?.let { result ->
            CommandResultDialog(
                result = result,
                onDismiss = { viewModel.dismissCommandResult() },
            )
        }

        viewModel.showVariableInputDialog?.let { command ->
            VariableInputDialog(
                command = command,
                onDismiss = { viewModel.dismissVariableInput() },
                onConfirm = { variables ->
                    viewModel.dismissVariableInput()
                    viewModel.executeCommandWithVariables(command, variables)
                },
            )
        }

        if (viewModel.showDeviceInfoDialog) {
            DeviceInfoDialog(
                deviceInfo = viewModel.deviceInfo,
                onDismiss = { viewModel.dismissDeviceInfo() },
            )
        }
    }
}

private fun openFileDialog(title: String, extensions: List<String>): String? {
    val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
    dialog.isMultipleMode = false
    dialog.filenameFilter = FilenameFilter { _, name ->
        extensions.any { ext -> name.endsWith(".$ext", ignoreCase = true) }
    }
    dialog.isVisible = true
    val file = dialog.file ?: return null
    val dir = dialog.directory ?: ""
    return File(dir, file).absolutePath
}
