package com.jqorz.anddevhelper.model

import kotlinx.serialization.Serializable

@Serializable
data class CommandTab(
    val id: String,
    val displayName: String,
    val isPreset: Boolean = false,
) {
    companion object {
        val DEFAULTS = listOf(
            CommandTab("device_info", "设备信息", isPreset = true),
            CommandTab("app_manage", "应用管理", isPreset = true),
            CommandTab("file_ops", "文件操作", isPreset = true),
            CommandTab("system", "系统操作", isPreset = true),
            CommandTab("network", "网络调试", isPreset = true),
        )

        fun fromId(id: String): CommandTab =
            DEFAULTS.find { it.id == id } ?: CommandTab(id, id)
    }
}
