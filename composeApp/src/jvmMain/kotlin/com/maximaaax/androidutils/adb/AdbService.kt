package com.maximaaax.androidutils.adb

import java.io.File
import java.util.concurrent.TimeUnit

class AdbService(
    private val adbPath: File = BundledPaths.adbExecutable(),
) {

    fun runCapture(vararg command: String): Result<String> =
        runCapture(command.toList())

    fun runCapture(command: List<String>): Result<String> {
        val pb = ProcessBuilder(command)
            .redirectErrorStream(true)
        return try {
            val process = pb.start()
            val text = process.inputStream.bufferedReader().readText()
            val finished = process.waitFor(120, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return Result.failure(IllegalStateException("Commande expirée : ${command.joinToString(" ")}"))
            }
            val code = process.exitValue()
            if (code == 0) Result.success(text)
            else Result.failure(IllegalStateException("Code $code : ${command.joinToString(" ")}\n$text"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun adbForDevice(serial: String?, vararg adbArgs: String): List<String> {
        val list = ArrayList<String>(adbArgs.size + 4)
        list.add(adbPath.absolutePath)
        if (serial != null) {
            list.add("-s")
            list.add(serial)
        }
        adbArgs.forEach { list.add(it) }
        return list
    }

    fun listDevices(): Result<List<AdbDevice>> =
        runCapture(adbPath.absolutePath, "devices", "-l").map(::parseDevices)

    private fun parseDevices(output: String): List<AdbDevice> {
        val lines = output.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        if (lines.isEmpty()) return emptyList()
        val dataLines = lines.dropWhile { it.startsWith("List of devices") }
        return dataLines.mapNotNull { line ->
            val parts = line.split(Regex("\\s+"), limit = 3)
            if (parts.size < 2) return@mapNotNull null
            val serial = parts[0]
            val state = parts[1]
            if (serial == "List") return@mapNotNull null
            val rest = parts.getOrNull(2).orEmpty()
            AdbDevice(serial = serial, state = state, model = parseModel(rest))
        }
    }

    private fun parseModel(rest: String): String? {
        val m = Regex("model:(\\S+)").find(rest) ?: return null
        return m.groupValues[1]
    }

    /**
     * `pm list packages -f` renvoie `package:CHEMIN=identifiant`.
     * Le [CHEMIN] peut contenir `=` (ex. `==` dans le nom de répertoire), le séparateur
     * entre chemin et identifiant de package est donc le **dernier** `=`.
     */
    private fun parsePackageListLineToPackageName(line: String): String {
        val rest = line.removePrefix("package:")
        val lastEq = rest.lastIndexOf('=')
        if (lastEq == -1) {
            return rest
        }
        return rest.substring(lastEq + 1)
    }

    fun listPackages(serial: String?): Result<List<String>> {
        val cmd = adbForDevice(serial, "shell", "pm", "list", "packages", "-f")
        return runCapture(cmd).map { text ->
            text.lineSequence()
                .map { it.trim() }
                .filter { it.startsWith("package:") }
                .map { line -> parsePackageListLineToPackageName(line) }
                .distinct()
                .sorted()
                .toList()
        }
    }

    fun packageApkPath(serial: String?, packageName: String): Result<String> {
        val cmd = adbForDevice(serial, "shell", "pm", "path", packageName)
        return runCapture(cmd).map { text ->
            val lines = text.lines().map { it.trim() }.filter { it.startsWith("package:") }
            val base = lines.firstOrNull { it.contains("base.apk") }
            val chosen = base ?: lines.firstOrNull()
                ?: error("Aucun chemin APK pour $packageName")
            chosen.removePrefix("package:")
        }
    }

    fun pullApk(serial: String?, remoteApkPath: String, localFile: File): Result<Unit> {
        localFile.parentFile?.mkdirs()
        val cmd = adbForDevice(serial, "pull", remoteApkPath, localFile.absolutePath)
        return runCapture(cmd).map { }
    }

    fun dumpsysPackage(serial: String?, packageName: String): Result<String> {
        val cmd = adbForDevice(serial, "shell", "dumpsys", "package", packageName)
        return runCapture(cmd)
    }

    companion object {
        fun parseVersionName(dumpsys: String): String? =
            Regex("""versionName=([^\s\n\]]+)""").find(dumpsys)?.groupValues?.get(1)

        fun parseApplicationLabel(dumpsys: String): String? {
            val appLabel = Regex("""applicationLabel=([^\n]+)""").find(dumpsys)?.groupValues?.get(1)?.trim()
            if (!appLabel.isNullOrBlank() && !appLabel.startsWith("@")) return appLabel
            val nonLoc = Regex("""nonLocalizedLabel=([^\s\n]+)""").find(dumpsys)?.groupValues?.get(1)?.trim()
            if (!nonLoc.isNullOrBlank() && !nonLoc.startsWith("0x")) return nonLoc
            return null
        }

        fun sanitizeFileComponent(name: String): String =
            name.replace(Regex("""[\\/:*?"<>|]"""), "_").trim().ifBlank { "app" }.take(120)
    }
}

data class AdbDevice(
    val serial: String,
    val state: String,
    val model: String?,
) {
    val isUsable: Boolean get() = state == "device"

    fun displayLabel(): String =
        buildString {
            append(serial)
            if (!model.isNullOrBlank()) {
                append(" — ")
                append(model)
            }
            if (!isUsable) {
                append(" (")
                append(state)
                append(")")
            }
        }
}
