package com.maximaaax.androidutils.adb

import java.io.File

object ScrcpyLauncher {

    /**
     * Démarre scrcpy dans un processus détaché (fenêtre séparée).
     * Utilise l’adb embarqué via la variable d’environnement `ADB`.
     */
    fun startMirror(serial: String): Result<Process> = runCatching {
        val launcher = BundledPaths.scrcpyLauncher()
        val cwd: File = BundledPaths.scrcpyWorkingDirectory()
        val pb = ProcessBuilder(
            launcher.absolutePath,
            "-s",
            serial,
        )
        pb.directory(cwd)
        pb.environment()["ADB"] = BundledPaths.adbExecutable().absolutePath
        // DISCARD n’est valide que pour stdout/stderr, pas pour stdin.
        pb.redirectInput(nullInputSource())
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD)
        pb.redirectError(ProcessBuilder.Redirect.DISCARD)
        pb.start()
    }

    private fun nullInputSource(): ProcessBuilder.Redirect {
        val f = if (System.getProperty("os.name", "").startsWith("Windows", ignoreCase = true)) {
            File("NUL")
        } else {
            File("/dev/null")
        }
        return ProcessBuilder.Redirect.from(f)
    }
}
