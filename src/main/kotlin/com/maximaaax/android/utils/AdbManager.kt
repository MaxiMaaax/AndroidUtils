package com.maximaaax.android.utils

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

data class AdbDevice(
    val serial: String,
    val model: String?,
    val deviceName: String?
)

object AdbManager {
    private fun adbExecutable(): File = ResourceExtractor.ensureBinaryAvailable("adb")

    fun listDevices(): List<AdbDevice> {
        val process = ProcessBuilder(adbExecutable().absolutePath, "devices", "-l")
            .redirectErrorStream(true)
            .start()
        val lines = process.inputStream.bufferedReader().useLines { it.toList() }
        process.waitFor()
        return lines
            .drop(1)
            .mapNotNull { parseDeviceLine(it) }
    }

    private fun parseDeviceLine(line: String): AdbDevice? {
        if (line.isBlank()) return null
        if (!line.contains("device") || line.contains("offline")) return null
        val parts = line.trim().split(" ")
            .filter { it.isNotBlank() }
        if (parts.isEmpty()) return null
        val serial = parts.first()
        val model = parts.find { it.startsWith("model:") }?.substringAfter(":")
        val deviceName = parts.find { it.startsWith("device:") }?.substringAfter(":")
        return AdbDevice(serial = serial, model = model, deviceName = deviceName)
    }

    fun listPackages(serial: String): List<String> {
        val process = ProcessBuilder(adbExecutable().absolutePath, "-s", serial, "shell", "pm", "list", "packages")
            .redirectErrorStream(true)
            .start()
        val lines = process.inputStream.bufferedReader().useLines { it.toList() }
        process.waitFor()
        return lines.mapNotNull { line ->
            if (line.startsWith("package:")) line.removePrefix("package:").trim() else null
        }
    }

    fun resolveApkPath(serial: String, packageName: String): String? {
        val process = ProcessBuilder(adbExecutable().absolutePath, "-s", serial, "shell", "pm", "path", packageName)
            .redirectErrorStream(true)
            .start()
        val out = process.inputStream.bufferedReader().use(BufferedReader::readText)
        process.waitFor()
        return out.lineSequence()
            .map { it.trim() }
            .firstOrNull { it.startsWith("package:") }
            ?.removePrefix("package:")
            ?.trim()
    }

    fun pull(serial: String, remotePath: String, localDir: File): File {
        if (!localDir.exists()) localDir.mkdirs()
        val fileName = remotePath.substringAfterLast('/')
        val target = File(localDir, fileName)
        val process = ProcessBuilder(
            adbExecutable().absolutePath,
            "-s", serial,
            "pull",
            remotePath,
            target.absolutePath
        ).redirectErrorStream(true).start()
        // Consume output to avoid blocking
        InputStreamReader(process.inputStream).use { it.readText() }
        val exit = process.waitFor()
        if (exit != 0) throw RuntimeException("Echec du pull ADB (code $exit)")
        return target
    }
}


