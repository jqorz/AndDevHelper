package com.jqorz.anddevhelper.viewmodel

import androidx.compose.runtime.*
import com.jqorz.anddevhelper.data.PresetCommands
import com.jqorz.anddevhelper.model.*
import com.jqorz.anddevhelper.service.AdbService
import com.jqorz.anddevhelper.service.ConfigManager
import kotlinx.coroutines.*
import java.util.UUID

class MainViewModel {

    private val adbService = AdbService()
    private val configManager = ConfigManager()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 设备相关
    var devices = mutableStateListOf<AdbDevice>()
        private set
    var selectedDevice by mutableStateOf<AdbDevice?>(null)
        private set
    var isRefreshingDevices by mutableStateOf(false)
        private set

    // Tab 相关
    var allTabs = mutableStateListOf<CommandTab>()
        private set
    var selectedTab by mutableStateOf(CommandTab.DEFAULTS[0])
        private set

    // 命令相关
    var allCommands = mutableStateListOf<AdbCommand>()
        private set
    var commandResults = mutableStateMapOf<String, CommandResult>()
        private set
    var executingCommandId by mutableStateOf<String?>(null)
        private set

    // 设置
    var settings by mutableStateOf(AppSettings())
        private set
    var adbValid by mutableStateOf(false)
        private set

    // 设备详情
    var deviceInfo by mutableStateOf(DeviceInfo())
        private set

    // UI 状态
    var showSettingsDialog by mutableStateOf(false)
        private set
    var showAddCommandDialog by mutableStateOf(false)
        private set
    var editingCommand by mutableStateOf<AdbCommand?>(null)
        private set
    var addCommandDefaultTab by mutableStateOf<CommandTab?>(null)
        private set
    var showCommandResultDialog by mutableStateOf<CommandResult?>(null)
        private set
    var showVariableInputDialog by mutableStateOf<AdbCommand?>(null)
        private set
    var showDeviceInfoDialog by mutableStateOf(false)
        private set

    // APK 安装状态
    var isInstallingApk by mutableStateOf(false)
        private set
    var installResult by mutableStateOf<CommandResult?>(null)
        private set

    private var refreshJob: Job? = null

    fun init() {
        settings = configManager.loadSettings()
        val customCommands = configManager.loadCustomCommands()
        val modifiedPresets = configManager.loadModifiedPresets()

        // 加载 Tab（默认 + 自定义）
        val customTabs = configManager.loadCustomTabs()
        allTabs.clear()
        allTabs.addAll(CommandTab.DEFAULTS)
        allTabs.addAll(customTabs)
        selectedTab = allTabs.first()

        // 加载预置命令，应用已修改的模板
        val presets = PresetCommands.getAll().map { cmd ->
            modifiedPresets[cmd.id]?.let { newTemplate ->
                cmd.copy(template = newTemplate, isModified = true)
            } ?: cmd
        }

        allCommands.clear()
        allCommands.addAll(presets)
        allCommands.addAll(customCommands)

        // 自动查找 adb
        if (settings.adbPath.isBlank()) {
            val found = adbService.findAdbFromPath()
            if (found != null) {
                settings = settings.copy(adbPath = found)
                configManager.saveSettings(settings)
            }
        }

        // 同步验证 adb 路径，避免 UI 首次渲染时显示错误状态
        if (settings.adbPath.isNotBlank()) {
            adbValid = adbService.validateAdbPathSync(settings.adbPath)
        }

        // 异步刷新设备列表
        scope.launch {
            if (adbValid) doRefreshDevices()
        }
        startAutoRefresh()
    }

    fun selectTab(tab: CommandTab) {
        selectedTab = tab
    }

    fun selectDevice(device: AdbDevice) {
        selectedDevice = device
    }

    fun refreshDevices() {
        scope.launch { doRefreshDevices() }
    }

    private suspend fun doRefreshDevices() {
        if (settings.adbPath.isBlank()) return
        isRefreshingDevices = true
        try {
            val newDevices = adbService.getDevices(settings.adbPath)
            devices.clear()
            devices.addAll(newDevices)

            // 自动选择第一个在线设备
            if (selectedDevice == null || devices.none { it.serial == selectedDevice?.serial }) {
                selectedDevice = devices.firstOrNull { it.status == DeviceStatus.ONLINE }
            }
        } finally {
            isRefreshingDevices = false
        }
    }

    private suspend fun validateAndRefresh() {
        if (settings.adbPath.isNotBlank()) {
            adbValid = adbService.validateAdbPath(settings.adbPath)
            if (adbValid) doRefreshDevices()
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        if (settings.autoRefreshDevices) {
            refreshJob = scope.launch {
                while (isActive) {
                    delay(settings.refreshInterval * 1000L)
                    if (settings.adbPath.isNotBlank() && adbValid) {
                        doRefreshDevices()
                    }
                }
            }
        }
    }

    // --- 命令执行 ---

    fun executeCommand(command: AdbCommand) {
        val device = selectedDevice
        if (device == null) {
            val errorResult = CommandResult(output = "", exitCode = -1, errorMessage = "未选择设备")
            commandResults[command.id] = errorResult
            showCommandResultDialog = errorResult
            return
        }

        // 检查是否需要用户输入变量
        val userVars = command.requiredVariables
        if (userVars.isNotEmpty()) {
            showVariableInputDialog = command
            return
        }

        doExecuteCommand(command, emptyMap())
    }

    fun executeCommandWithVariables(command: AdbCommand, variables: Map<String, String>) {
        doExecuteCommand(command, variables)
    }

    private fun doExecuteCommand(command: AdbCommand, variables: Map<String, String>) {
        val device = selectedDevice ?: return
        scope.launch {
            executingCommandId = command.id
            try {
                val resolved = command.resolve(device.serial, variables)
                val result = adbService.executeTemplate(
                    settings.adbPath, resolved,
                    timeoutMs = settings.commandTimeout * 1000L
                )
                commandResults[command.id] = result
                showCommandResultDialog = result
            } finally {
                executingCommandId = null
            }
        }
    }

    // --- 自定义命令管理 ---

    fun addCustomCommand(name: String, template: String, tabId: String) {
        val id = "custom_${UUID.randomUUID().toString().take(8)}"
        val variables = AdbCommand.extractVariables(template)
        val command = AdbCommand(
            id = id,
            name = name,
            template = template,
            tabId = tabId,
            isPreset = false,
            requiredVariables = variables,
        )
        allCommands.add(command)
        saveCustomCommands()
        showAddCommandDialog = false
    }

    fun updateCommand(command: AdbCommand, newName: String, newTemplate: String) {
        val index = allCommands.indexOfFirst { it.id == command.id }
        if (index < 0) return

        val variables = AdbCommand.extractVariables(newTemplate)
        if (command.isPreset) {
            // 修改预置命令
            val updated = command.copy(template = newTemplate, isModified = true, requiredVariables = variables)
            allCommands[index] = updated
            val modified = configManager.loadModifiedPresets().toMutableMap()
            modified[command.id] = newTemplate
            configManager.saveModifiedPresets(modified)
        } else {
            // 修改自定义命令
            val updated = command.copy(name = newName, template = newTemplate, requiredVariables = variables)
            allCommands[index] = updated
            saveCustomCommands()
        }
        editingCommand = null
        showAddCommandDialog = false
    }

    fun deleteCommand(command: AdbCommand) {
        if (command.isPreset) {
            // 恢复预置命令默认
            val index = allCommands.indexOfFirst { it.id == command.id }
            if (index >= 0) {
                val original = PresetCommands.getAll().first { it.id == command.id }
                allCommands[index] = original
                val modified = configManager.loadModifiedPresets().toMutableMap()
                modified.remove(command.id)
                configManager.saveModifiedPresets(modified)
            }
        } else {
            allCommands.removeAll { it.id == command.id }
            saveCustomCommands()
        }
    }

    private fun saveCustomCommands() {
        val custom = allCommands.filter { !it.isPreset }
        configManager.saveCustomCommands(custom)
    }

    // --- Tab 管理 ---

    fun addTab(name: String) {
        val id = "tab_${UUID.randomUUID().toString().take(8)}"
        val tab = CommandTab(id = id, displayName = name, isPreset = false)
        allTabs.add(tab)
        saveCustomTabs()
    }

    fun deleteTab(tab: CommandTab) {
        if (tab.isPreset) return
        allTabs.removeAll { it.id == tab.id }
        // 删除该 tab 下的自定义命令
        allCommands.removeAll { !it.isPreset && it.tabId == tab.id }
        saveCustomCommands()
        saveCustomTabs()
        // 如果删除的是当前选中的 tab，切换到第一个
        if (selectedTab.id == tab.id) {
            selectedTab = allTabs.first()
        }
    }

    fun renameTab(tab: CommandTab, newName: String) {
        if (tab.isPreset) return
        val index = allTabs.indexOfFirst { it.id == tab.id }
        if (index >= 0) {
            allTabs[index] = tab.copy(displayName = newName)
            saveCustomTabs()
        }
    }

    private fun saveCustomTabs() {
        val custom = allTabs.filter { !it.isPreset }
        configManager.saveCustomTabs(custom)
    }

    // --- 设置 ---

    fun updateSettings(newSettings: AppSettings) {
        settings = newSettings
        configManager.saveSettings(newSettings)
        scope.launch { validateAndRefresh() }
        startAutoRefresh()
    }

    fun showSettings() {
        showSettingsDialog = true
    }

    fun dismissSettings() {
        showSettingsDialog = false
    }

    fun showAddCommand(defaultTab: CommandTab? = null) {
        editingCommand = null
        addCommandDefaultTab = defaultTab
        showAddCommandDialog = true
    }

    fun showEditCommand(command: AdbCommand) {
        editingCommand = command
        showAddCommandDialog = true
    }

    fun dismissAddCommand() {
        showAddCommandDialog = false
        editingCommand = null
        addCommandDefaultTab = null
    }

    fun dismissCommandResult() {
        showCommandResultDialog = null
    }

    fun dismissVariableInput() {
        showVariableInputDialog = null
    }

    // --- 设备详情 ---

    fun showDeviceInfo() {
        val device = selectedDevice
        if (device == null) {
            deviceInfo = DeviceInfo(errorMessage = "未选择设备")
            showDeviceInfoDialog = true
            return
        }
        showDeviceInfoDialog = true
        deviceInfo = DeviceInfo(isLoading = true)
        scope.launch {
            val info = adbService.getDeviceInfo(settings.adbPath, device.serial)
            deviceInfo = info
        }
    }

    fun dismissDeviceInfo() {
        showDeviceInfoDialog = false
    }

    // --- APK 安装 ---

    fun installApk(apkPath: String) {
        val device = selectedDevice
        if (device == null) {
            installResult = CommandResult(output = "", exitCode = -1, errorMessage = "未选择设备")
            return
        }
        scope.launch {
            isInstallingApk = true
            try {
                val result = adbService.installApk(settings.adbPath, device.serial, apkPath)
                installResult = result
            } finally {
                isInstallingApk = false
            }
        }
    }

    fun clearInstallResult() {
        installResult = null
    }

    // 获取当前 Tab 的命令列表
    fun commandsForCurrentTab(): List<AdbCommand> =
        allCommands.filter { it.tabId == selectedTab.id }

    fun destroy() {
        scope.cancel()
    }
}
