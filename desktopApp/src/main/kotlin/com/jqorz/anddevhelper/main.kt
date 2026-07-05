package com.jqorz.anddevhelper

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import anddevhelper.desktopapp.generated.resources.Res
import anddevhelper.desktopapp.generated.resources.icon_256x256
import org.jetbrains.compose.resources.painterResource

fun main() {
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "AndDevHelper",
            icon = painterResource(Res.drawable.icon_256x256),
            state = WindowState(
                size = DpSize(1280.dp, 800.dp),
            ),
        ) {
            App()
        }
    }
}
