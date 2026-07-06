package com.jqorz.anddevhelper.service

import com.jqorz.anddevhelper.model.AdbCommand
import com.jqorz.anddevhelper.model.AppSettings
import com.jqorz.anddevhelper.model.CommandTab
import com.jqorz.anddevhelper.model.ThemeMode
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class ConfigData(
    val settings: AppSettings = AppSettings(),
    val customCommands: List<AdbCommand> = emptyList(),
    val modifiedPresetIds: Map<String, String> = emptyMap(), // id -> modified template
    val customTabs: List<CommandTab> = emptyList(),
)

class ConfigManager {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val configDir: File
        get() {
            val userHome = System.getProperty("user.home")
            return File(userHome, ".anddevhelper").also { it.mkdirs() }
        }

    private val configFile: File
        get() = File(configDir, "config.json")

    private var cached: ConfigData? = null

    private fun load(): ConfigData {
        cached?.let { return it }
        return try {
            if (configFile.exists()) {
                json.decodeFromString<ConfigData>(configFile.readText()).also { cached = it }
            } else {
                ConfigData().also { cached = it }
            }
        } catch (_: Exception) {
            ConfigData().also { cached = it }
        }
    }

    private fun save(data: ConfigData) {
        cached = data
        try {
            configFile.writeText(json.encodeToString(data))
        } catch (_: Exception) {
            // 写入失败时静默忽略
        }
    }

    fun loadSettings(): AppSettings = load().settings

    fun saveSettings(settings: AppSettings) {
        val data = load()
        save(data.copy(settings = settings))
    }

    fun loadCustomCommands(): List<AdbCommand> = load().customCommands

    fun saveCustomCommands(commands: List<AdbCommand>) {
        val data = load()
        save(data.copy(customCommands = commands))
    }

    fun loadModifiedPresets(): Map<String, String> = load().modifiedPresetIds

    fun saveModifiedPresets(modified: Map<String, String>) {
        val data = load()
        save(data.copy(modifiedPresetIds = modified))
    }

    fun loadCustomTabs(): List<CommandTab> = load().customTabs

    fun saveCustomTabs(tabs: List<CommandTab>) {
        val data = load()
        save(data.copy(customTabs = tabs))
    }
}
