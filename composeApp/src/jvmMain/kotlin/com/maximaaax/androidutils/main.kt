package com.maximaaax.androidutils

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AndroidUtils",
        state = rememberWindowState(width = 1000.dp, height = 760.dp),
    ) {
        App(hostWindow = window)
    }
}
