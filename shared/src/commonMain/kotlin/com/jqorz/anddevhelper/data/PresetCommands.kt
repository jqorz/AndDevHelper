package com.jqorz.anddevhelper.data

import com.jqorz.anddevhelper.model.AdbCommand
import com.jqorz.anddevhelper.model.CommandTab

object PresetCommands {

    private val DEVICE_INFO = CommandTab.DEFAULTS[0]
    private val APP_MANAGE = CommandTab.DEFAULTS[1]
    private val FILE_OPS = CommandTab.DEFAULTS[2]
    private val SYSTEM = CommandTab.DEFAULTS[3]
    private val NETWORK = CommandTab.DEFAULTS[4]

    fun getAll(): List<AdbCommand> = listOf(
        // 设备信息
        cmd("dev_battery", "查看电池信息", "adb -s {device} shell dumpsys battery", DEVICE_INFO),
        cmd("dev_ip", "查看设备 IP", "adb -s {device} shell ip addr show wlan0", DEVICE_INFO),
        cmd("dev_cpu_info", "查看 CPU 信息", "adb -s {device} shell cat /proc/cpuinfo", DEVICE_INFO),
        cmd("dev_storage", "查看存储信息", "adb -s {device} shell df -h", DEVICE_INFO),

        // 应用管理
        cmd("app_top_activity", "查看顶层 Activity", "adb -s {device} shell dumpsys activity activities | grep mResumedActivity", APP_MANAGE),
        cmd("app_list_all", "查看已安装应用", "adb -s {device} shell pm list packages", APP_MANAGE),
        cmd("app_list_3rd", "查看第三方应用", "adb -s {device} shell pm list packages -3", APP_MANAGE),
        cmd("app_clear", "清除应用数据", "adb -s {device} shell pm clear {package}", APP_MANAGE, listOf("package")),
        cmd("app_force_stop", "强制停止应用", "adb -s {device} shell am force-stop {package}", APP_MANAGE, listOf("package")),
        cmd("app_start", "启动应用", "adb -s {device} shell am start -n {package}/{activity}", APP_MANAGE, listOf("package", "activity")),
        cmd("app_path", "查看应用路径", "adb -s {device} shell pm path {package}", APP_MANAGE, listOf("package")),

        // 文件操作
        cmd("file_push", "推送文件到设备", "adb -s {device} push {local} {remote}", FILE_OPS, listOf("local", "remote")),
        cmd("file_pull", "从设备拉取文件", "adb -s {device} pull {remote} {local}", FILE_OPS, listOf("remote", "local")),
        cmd("file_screenshot", "截图保存到电脑", "adb -s {device} shell screencap -p /sdcard/screenshot.png && adb -s {device} pull /sdcard/screenshot.png", FILE_OPS),
        cmd("file_record", "录屏", "adb -s {device} shell screenrecord /sdcard/record.mp4", FILE_OPS),

        // 系统操作
        cmd("sys_reboot", "重启设备", "adb -s {device} reboot", SYSTEM),
        cmd("sys_recovery", "重启到 Recovery", "adb -s {device} reboot recovery", SYSTEM),
        cmd("sys_bootloader", "重启到 Bootloader", "adb -s {device} reboot bootloader", SYSTEM),
        cmd("sys_props", "查看系统属性", "adb -s {device} shell getprop", SYSTEM),
        cmd("sys_ps", "查看进程列表", "adb -s {device} shell ps", SYSTEM),
        cmd("sys_mem", "查看内存信息", "adb -s {device} shell cat /proc/meminfo", SYSTEM),

        // 网络调试
        cmd("net_tcpip", "无线连接设备", "adb -s {device} tcpip 5555 && adb connect {ip}:5555", NETWORK, listOf("ip")),
        cmd("net_disconnect", "断开无线连接", "adb disconnect {ip}:5555", NETWORK, listOf("ip")),
        cmd("netstat", "查看端口占用", "adb -s {device} shell netstat", NETWORK),
    )

    private fun cmd(
        id: String,
        name: String,
        template: String,
        tab: CommandTab,
        variables: List<String> = emptyList(),
    ) = AdbCommand(
        id = id,
        name = name,
        template = template,
        tabId = tab.id,
        isPreset = true,
        requiredVariables = variables,
    )
}
