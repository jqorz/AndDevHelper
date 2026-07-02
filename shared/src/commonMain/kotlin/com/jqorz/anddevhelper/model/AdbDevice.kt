package com.jqorz.anddevhelper.model

import kotlinx.serialization.Serializable

@Serializable
data class AdbDevice(
    val serial: String,
    val name: String = "",
    val status: DeviceStatus = DeviceStatus.ONLINE,
) {
    val displayName: String
        get() = if (name.isNotBlank()) "$name ($serial)" else serial
}

enum class DeviceStatus {
    ONLINE, OFFLINE, UNAUTHORIZED
}
