package com.jqorz.anddevhelper.model

import kotlinx.serialization.Serializable

@Serializable
data class AppSettings(
    val adbPath: String = "",
    val commandTimeout: Int = 30,
    val autoRefreshDevices: Boolean = true,
    val refreshInterval: Int = 5,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val historyLimit: Int = 100,
)

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
