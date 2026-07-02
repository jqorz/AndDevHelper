package com.jqorz.anddevhelper.model

import kotlinx.serialization.Serializable

@Serializable
data class CommandResult(
    val output: String,
    val exitCode: Int = 0,
    val isSuccess: Boolean = exitCode == 0,
    val errorMessage: String = "",
)
