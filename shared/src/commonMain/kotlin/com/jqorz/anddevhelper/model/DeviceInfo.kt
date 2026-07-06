package com.jqorz.anddevhelper.model

/**
 * 设备详细信息，通过多个 ADB 命令获取
 */
data class DeviceInfo(
    val model: String = "",              // 设备型号
    val manufacturer: String = "",       // 制造商
    val androidVersion: String = "",     // Android 版本
    val sdkVersion: String = "",         // SDK 版本
    val screenSize: String = "",         // 屏幕分辨率
    val screenDensity: String = "",      // 屏幕密度 (dpi)
    val cpuAbi: String = "",             // CPU 架构
    val cpuHardware: String = "",        // CPU 硬件
    val totalMemory: String = "",        // 总内存
    val serialno: String = "",           // 序列号
    val buildId: String = "",            // Build ID
    val securityPatch: String = "",      // 安全补丁级别
    val fingerprint: String = "",        // 系统指纹
    val board: String = "",              // 主板
    val brand: String = "",              // 品牌
    val isLoaded: Boolean = false,       // 是否已加载
    val isLoading: Boolean = false,      // 是否正在加载
    val errorMessage: String = "",       // 错误信息
)
