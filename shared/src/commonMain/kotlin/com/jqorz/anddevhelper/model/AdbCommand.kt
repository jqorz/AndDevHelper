package com.jqorz.anddevhelper.model

import kotlinx.serialization.Serializable

@Serializable
data class AdbCommand(
    val id: String,
    val name: String,
    val template: String,
    val tabId: String,
    val isPreset: Boolean = false,
    val isModified: Boolean = false,
    val requiredVariables: List<String> = emptyList(),
) {
    companion object {
        /** 从模板中提取所需的变量占位符，如 {device}, {package} */
        fun extractVariables(template: String): List<String> {
            val regex = Regex("""\{(\w+)}""")
            return regex.findAll(template)
                .map { it.groupValues[1] }
                .filter { it != "device" } // device 自动填充，不算用户输入
                .distinct()
                .toList()
        }
    }

    /** 将模板中的变量替换为实际值 */
    fun resolve(device: String, variables: Map<String, String> = emptyMap()): String {
        var result = template.replace("{device}", device)
        for ((key, value) in variables) {
            result = result.replace("{$key}", value)
        }
        return result
    }
}
