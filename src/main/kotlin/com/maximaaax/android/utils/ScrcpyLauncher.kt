package com.maximaaax.android.utils

import java.io.File

object ScrcpyLauncher {
    private fun scrcpyExecutable(): File = ResourceExtractor.ensureBinaryAvailable("scrcpy")

    fun launch(serial: String, extraArgs: List<String> = emptyList()) {
        val cmd = mutableListOf(scrcpyExecutable().absolutePath, "-s", serial)
        cmd.addAll(extraArgs)
        ProcessBuilder(cmd)
            .inheritIO()
            .start()
        // Do not waitFor: let scrcpy run independently
    }
}


