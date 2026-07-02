package com.jqorz.anddevhelper.data

import com.jqorz.anddevhelper.model.AdbCommand
import com.jqorz.anddevhelper.model.CommandTab

object PresetCommands {

    fun getAll(): List<AdbCommand> = listOf(
        // 设备信息
        cmd("dev_model", "查看设备型号", "adb -s {device} shell getprop ro.product.model", CommandTab.DEVICE_INFO),
        cmd("dev_android_ver", "查看 Android 版本", "adb -s {device} shell getprop ro.build.version.release", CommandTab.DEVICE_INFO),
        cmd("dev_screen", "查看屏幕分辨率", "adb -s {device} shell wm size", CommandTab.DEVICE_INFO),
        cmd("dev_serial", "查看设备序列号", "adb -s {device} get-serialno", CommandTab.DEVICE_INFO),
        cmd("dev_battery", "查看电池信息", "adb -s {device} shell dumpsys battery", CommandTab.DEVICE_INFO),
        cmd("dev_ip", "查看设备 IP", "adb -s {device} shell ip addr show wlan0", CommandTab.DEVICE_INFO),

        // 应用管理
        cmd("app_top_activity", "查看顶层 Activity", "adb -s {device} shell dumpsys activity activities | grep mResumedActivity", CommandTab.APP_MANAGE),
        cmd("app_list_all", "查看已安装应用", "adb -s {device} shell pm list packages", CommandTab.APP_MANAGE),
        cmd("app_list_3rd", "查看第三方应用", "adb -s {device} shell pm list packages -3", CommandTab.APP_MANAGE),
        cmd("app_clear", "清除应用数据", "adb -s {device} shell pm clear {package}", CommandTab.APP_MANAGE, listOf("package")),
        cmd("app_force_stop", "强制停止应用", "adb -s {device} shell am force-stop {package}", CommandTab.APP_MANAGE, listOf("package")),
        cmd("app_start", "启动应用", "adb -s {device} shell am start -n {package}/{activity}", CommandTab.APP_MANAGE, listOf("package", "activity")),
        cmd("app_path", "查看应用路径", "adb -s {device} shell pm path {package}", CommandTab.APP_MANAGE, listOf("package")),

        // 文件操作
        cmd("file_push", "推送文件到设备", "adb -s {device} push {local} {remote}", CommandTab.FILE_OPS, listOf("local", "remote")),
        cmd("file_pull", "从设备拉取文件", "adb -s {device} pull {remote} {local}", CommandTab.FILE_OPS, listOf("remote", "local")),
        cmd("file_screenshot", "截图保存到电脑", "adb -s {device} shell screencap -p /sdcard/screenshot.png && adb -s {device} pull /sdcard/screenshot.png", CommandTab.FILE_OPS),
        cmd("file_record", "录屏", "adb -s {device} shell screenrecord /sdcard/record.mp4", CommandTab.FILE_OPS),

        // 系统操作
        cmd("sys_reboot", "重启设备", "adb -s {device} reboot", CommandTab.SYSTEM),
        cmd("sys_recovery", "重启到 Recovery", "adb -s {device} reboot recovery", CommandTab.SYSTEM),
        cmd("sys_bootloader", "重启到 Bootloader", "adb -s {device} reboot bootloader", CommandTab.SYSTEM),
        cmd("sys_props", "查看系统属性", "adb -s {device} shell getprop", CommandTab.SYSTEM),
        cmd("sys_ps", "查看进程列表", "adb -s {device} shell ps", CommandTab.SYSTEM),
        cmd("sys_mem", "查看内存信息", "adb -s {device} shell cat /proc/meminfo", CommandTab.SYSTEM),

        // 网络调试
        cmd("net_tcpip", "无线连接设备", "adb -s {device} tcpip 5555 && adb connect {ip}:5555", CommandTab.NETWORK, listOf("ip")),
        cmd("net_disconnect", "断开无线连接", "adb disconnect {ip}:5555", CommandTab.NETWORK, listOf("ip")),
        cmd("netstat", "查看端口占用", "adb -s {device} shell netstat", CommandTab.NETWORK),
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
