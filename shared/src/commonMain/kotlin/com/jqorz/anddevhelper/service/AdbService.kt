package com.jqorz.anddevhelper.service

import com.jqorz.anddevhelper.model.AdbDevice
import com.jqorz.anddevhelper.model.CommandResult
import com.jqorz.anddevhelper.model.DeviceInfo
import com.jqorz.anddevhelper.model.DeviceStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class AdbService {

    /**
     * 从系统 PATH 环境变量中查找 adb 可执行文件
     */
    fun findAdbFromPath(): String? {
        val pathEnv = System.getenv("PATH") ?: return null
        val separator = if (System.getProperty("os.name").lowercase().contains("win")) ";" else ":"
        val adbName = if (System.getProperty("os.name").lowercase().contains("win")) "adb.exe" else "adb"

        for (dir in pathEnv.split(separator)) {
            val adbFile = java.io.File(dir, adbName)
            if (adbFile.exists() && adbFile.canExecute()) {
                return adbFile.absolutePath
            }
        }
        return null
    }

    /**
     * 同步验证 adb 路径是否有效（用于初始化）
     */
    fun validateAdbPathSync(path: String): Boolean {
        if (path.isBlank()) return false
        val result = executeRaw(path, listOf("version"))
        return result.isSuccess
    }

    /**
     * 异步验证 adb 路径是否有效
     */
    suspend fun validateAdbPath(path: String): Boolean {
        return withContext(Dispatchers.IO) {
            validateAdbPathSync(path)
        }
    }

    /**
     * 获取已连接的设备列表
     */
    suspend fun getDevices(adbPath: String): List<AdbDevice> {
        if (adbPath.isBlank()) return emptyList()
        val result = executeRaw(adbPath, listOf("devices"))
        if (!result.isSuccess) return emptyList()

        return result.output.lines()
            .drop(1) // 跳过 "List of devices attached" 标题行
            .filter { it.isNotBlank() }
            .mapNotNull { line ->
                val parts = line.trim().split(Regex("""\s+"""))
                if (parts.size >= 2) {
                    val serial = parts[0]
                    val status = when (parts[1]) {
                        "device" -> DeviceStatus.ONLINE
                        "offline" -> DeviceStatus.OFFLINE
                        "unauthorized" -> DeviceStatus.UNAUTHORIZED
                        else -> DeviceStatus.OFFLINE
                    }
                    AdbDevice(serial = serial, status = status)
                } else null
            }
    }

    /**
     * 获取设备详细信息
     */
    suspend fun getDeviceInfo(adbPath: String, device: String): DeviceInfo {
        if (adbPath.isBlank()) return DeviceInfo(errorMessage = "ADB 路径未配置")
        return withContext(Dispatchers.IO) {
            try {
                // 并行获取各项信息
                val props = getProp(adbPath, device)
                val screen = getScreenSize(adbPath, device)
                val density = getScreenDensity(adbPath, device)
                val memTotal = getMemTotal(adbPath, device)
                val serialno = getSerialNo(adbPath, device)
                val cpuAbi = props["ro.product.cpu.abi"] ?: ""
                val cpuHardware = props["ro.hardware"] ?: ""

                DeviceInfo(
                    model = props["ro.product.model"] ?: "",
                    manufacturer = props["ro.product.manufacturer"] ?: "",
                    androidVersion = props["ro.build.version.release"] ?: "",
                    sdkVersion = props["ro.build.version.sdk"] ?: "",
                    screenSize = screen,
                    screenDensity = density,
                    cpuAbi = cpuAbi,
                    cpuHardware = cpuHardware,
                    totalMemory = memTotal,
                    serialno = serialno,
                    buildId = props["ro.build.display.id"] ?: "",
                    securityPatch = props["ro.build.version.security_patch"] ?: "",
                    fingerprint = props["ro.build.fingerprint"] ?: "",
                    board = props["ro.product.board"] ?: "",
                    brand = props["ro.product.brand"] ?: "",
                    isLoaded = true,
                )
            } catch (e: Exception) {
                DeviceInfo(errorMessage = e.message ?: "获取设备信息失败")
            }
        }
    }

    private fun getProp(adbPath: String, device: String): Map<String, String> {
        val result = executeRaw(adbPath, listOf("-s", device, "shell", "getprop"))
        if (!result.isSuccess) return emptyMap()
        val map = mutableMapOf<String, String>()
        for (line in result.output.lines()) {
            val match = Regex("^\\[([^]]+)]\\s*:\\s*\\[([^]]*)]$").find(line.trim())
            if (match != null) {
                map[match.groupValues[1]] = match.groupValues[2]
            }
        }
        return map
    }

    private fun getScreenSize(adbPath: String, device: String): String {
        val result = executeRaw(adbPath, listOf("-s", device, "shell", "wm", "size"))
        if (!result.isSuccess) return ""
        // 输出格式: "Physical size: 1080x2340" 或 "Override size: 1080x2340"
        val lines = result.output.lines()
        val physical = lines.firstOrNull { it.contains("Physical size") }
            ?.substringAfter(":")?.trim() ?: ""
        val override = lines.firstOrNull { it.contains("Override size") }
            ?.substringAfter(":")?.trim() ?: ""
        return if (override.isNotEmpty()) "$physical (Override: $override)" else physical
    }

    private fun getScreenDensity(adbPath: String, device: String): String {
        val result = executeRaw(adbPath, listOf("-s", device, "shell", "wm", "density"))
        if (!result.isSuccess) return ""
        val lines = result.output.lines()
        val physical = lines.firstOrNull { it.contains("Physical density") }
            ?.substringAfter(":")?.trim() ?: ""
        val override = lines.firstOrNull { it.contains("Override density") }
            ?.substringAfter(":")?.trim() ?: ""
        return if (override.isNotEmpty()) "$physical (Override: $override)" else physical
    }

    private fun getMemTotal(adbPath: String, device: String): String {
        val result = executeRaw(adbPath, listOf("-s", device, "shell", "cat", "/proc/meminfo"))
        if (!result.isSuccess) return ""
        val line = result.output.lines().firstOrNull { it.startsWith("MemTotal") } ?: ""
        val kb = Regex("MemTotal:\\s+(\\d+)").find(line)?.groupValues?.get(1)?.toLongOrNull()
        return if (kb != null) {
            val gb = kb / 1024.0 / 1024.0
            if (gb >= 1) String.format("%.1f GB", gb) else "${kb / 1024} MB"
        } else ""
    }

    private fun getSerialNo(adbPath: String, device: String): String {
        val result = executeRaw(adbPath, listOf("-s", device, "get-serialno"))
        return if (result.isSuccess) result.output.trim() else ""
    }

    /**
     * 安装 APK 到指定设备
     */
    suspend fun installApk(adbPath: String, device: String, apkPath: String): CommandResult {
        if (adbPath.isBlank()) return CommandResult(output = "", exitCode = -1, errorMessage = "ADB 路径未配置")
        return executeCommand(adbPath, listOf("-s", device, "install", "-r", apkPath), timeoutMs = 120_000)
    }

    /**
     * 执行 ADB 命令模板（已解析变量）
     */
    suspend fun executeCommand(adbPath: String, args: List<String>, timeoutMs: Long = 30_000): CommandResult {
        return withContext(Dispatchers.IO) {
            try {
                val result = withTimeoutOrNull(timeoutMs) {
                    executeRaw(adbPath, args)
                }
                result ?: CommandResult(
                    output = "",
                    exitCode = -1,
                    errorMessage = "命令执行超时（${timeoutMs / 1000}秒）"
                )
            } catch (e: Exception) {
                CommandResult(
                    output = "",
                    exitCode = -1,
                    errorMessage = e.message ?: "未知错误"
                )
            }
        }
    }

    /**
     * 执行命令模板字符串（如 "adb -s xxx shell getprop"），自动拆分为 args
     */
    suspend fun executeTemplate(adbPath: String, resolvedCommand: String, timeoutMs: Long = 30_000): CommandResult {
        // 如果模板以 "adb" 开头，去掉前缀，因为我们用 adbPath 替代
        val commandBody = resolvedCommand.trim().let { cmd ->
            when {
                cmd.startsWith("adb.exe ") -> cmd.removePrefix("adb.exe ")
                cmd.startsWith("adb ") -> cmd.removePrefix("adb ")
                else -> cmd
            }
        }
        val args = splitCommand(commandBody)
        return executeCommand(adbPath, args, timeoutMs)
    }

    private fun splitCommand(command: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inSingleQuote = false
        var inDoubleQuote = false

        for (ch in command) {
            when {
                ch == '\'' && !inDoubleQuote -> inSingleQuote = !inSingleQuote
                ch == '"' && !inSingleQuote -> inDoubleQuote = !inDoubleQuote
                ch == ' ' && !inSingleQuote && !inDoubleQuote -> {
                    if (current.isNotEmpty()) {
                        result.add(current.toString())
                        current.clear()
                    }
                }
                else -> current.append(ch)
            }
        }
        if (current.isNotEmpty()) result.add(current.toString())
        return result
    }

    private fun executeRaw(adbPath: String, args: List<String>): CommandResult {
        return try {
            val processBuilder = ProcessBuilder(mutableListOf(adbPath) + args)
            processBuilder.redirectErrorStream(true)
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            CommandResult(
                output = output.trim(),
                exitCode = exitCode,
                isSuccess = exitCode == 0,
            )
        } catch (e: Exception) {
            CommandResult(
                output = "",
                exitCode = -1,
                errorMessage = e.message ?: "执行失败"
            )
        }
    }
}
