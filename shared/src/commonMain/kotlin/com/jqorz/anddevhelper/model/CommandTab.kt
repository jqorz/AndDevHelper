package com.jqorz.anddevhelper.model

enum class CommandTab(val id: String, val displayName: String) {
    DEVICE_INFO("device_info", "设备信息"),
    APP_MANAGE("app_manage", "应用管理"),
    FILE_OPS("file_ops", "文件操作"),
    SYSTEM("system", "系统操作"),
    NETWORK("network", "网络调试");

    companion object {
        fun fromId(id: String): CommandTab =
            entries.find { it.id == id } ?: DEVICE_INFO
    }
}
