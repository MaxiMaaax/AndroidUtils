package com.maximaaax.android.utils

import com.maximaaax.android.utils.domain.model.AdbDevice
import com.maximaaax.android.utils.domain.model.PackageInfo
import com.maximaaax.android.utils.infrastructure.ResourceExtractor
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

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

    /**
     * Récupère le nom de l'application pour un package donné via ADB.
     * Utilise `pm dump` qui est plus rapide que `dumpsys package`.
     * 
     * @param serial Le numéro de série de l'appareil
     * @param packageName Le nom du package (ex: com.example.app)
     * @return Le nom de l'application ou null si non trouvé
     */
    fun getApplicationName(serial: String, packageName: String): String? {
        // Utiliser pm dump qui est généralement plus rapide que dumpsys
        val process = ProcessBuilder(
            adbExecutable().absolutePath,
            "-s", serial,
            "shell", "pm", "dump", packageName
        ).redirectErrorStream(true).start()
        
        // Lire avec un timeout pour éviter les blocages
        val output = try {
            val reader = process.inputStream.bufferedReader()
            val result = StringBuilder()
            val buffer = CharArray(8192)
            var read: Int
            var totalRead = 0
            val maxRead = 50000 // Limiter la lecture à ~50KB pour éviter les blocages
            
            while (reader.ready() && totalRead < maxRead) {
                read = reader.read(buffer, 0, minOf(buffer.size, maxRead - totalRead))
                if (read <= 0) break
                result.append(buffer, 0, read)
                totalRead += read
            }
            result.toString()
        } catch (e: Exception) {
            process.destroyForcibly()
            return null
        }
        
        // Attendre avec un timeout (2 secondes max)
        val completed = try {
            process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)
        } catch (e: Exception) {
            false
        }
        
        if (!completed) {
            process.destroyForcibly()
            return null
        }
        
        if (process.exitValue() != 0) {
            return null
        }
        
        // Chercher le label de l'application dans la sortie
        // Format typique: "applicationLabel=Nom de l'application" ou "applicationLabel='Nom'"
        val lines = output.lines().toList()
        
        // Chercher applicationLabel= dans les lignes (format le plus courant)
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("applicationLabel=")) {
                val label = trimmed.removePrefix("applicationLabel=")
                    .trim()
                    .removeSurrounding("\"")
                    .removeSurrounding("'")
                    .trim()
                if (label.isNotEmpty() && label != "null") {
                    return label
                }
            }
        }
        
        // Fallback 1: chercher "label=" dans la section ApplicationInfo (format différent)
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("label=") && !trimmed.startsWith("labelRes=")) {
                val label = trimmed.removePrefix("label=")
                    .trim()
                    .removeSurrounding("\"")
                    .removeSurrounding("'")
                    .trim()
                if (label.isNotEmpty() && label != "null") {
                    return label
                }
            }
        }
        
        // Fallback: essayer dumpsys package si pm dump n'a pas fonctionné
        try {
            // Utiliser sh -c pour exécuter la commande avec pipe
            val dumpsysProcess = ProcessBuilder(
                adbExecutable().absolutePath,
                "-s", serial,
                "shell", "sh", "-c", "dumpsys package $packageName | grep -A 1 applicationLabel"
            ).redirectErrorStream(true).start()
            
            val dumpsysOutput = try {
                val reader = dumpsysProcess.inputStream.bufferedReader()
                val result = StringBuilder()
                val buffer = CharArray(4096)
                var read: Int
                var totalRead = 0
                val maxRead = 20000 // Limiter à 20KB
                
                while (reader.ready() && totalRead < maxRead) {
                    read = reader.read(buffer, 0, minOf(buffer.size, maxRead - totalRead))
                    if (read <= 0) break
                    result.append(buffer, 0, read)
                    totalRead += read
                }
                result.toString()
            } catch (e: Exception) {
                dumpsysProcess.destroyForcibly()
                return null
            }
            
            val completed = try {
                dumpsysProcess.waitFor(1, java.util.concurrent.TimeUnit.SECONDS)
            } catch (e: Exception) {
                false
            }
            
            if (!completed) {
                dumpsysProcess.destroyForcibly()
                return null
            }
            
            if (dumpsysProcess.exitValue() == 0) {
                val dumpsysLines = dumpsysOutput.lines().toList()
                for (line in dumpsysLines) {
                    val trimmed = line.trim()
                    if (trimmed.startsWith("applicationLabel=")) {
                        val label = trimmed.removePrefix("applicationLabel=")
                            .trim()
                            .removeSurrounding("\"")
                            .removeSurrounding("'")
                            .trim()
                        if (label.isNotEmpty() && label != "null") {
                            return label
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignorer les erreurs du fallback
        }
        
        return null
    }

    /**
     * Liste les packages avec leurs noms d'applications.
     * Attention: cette fonction peut être lente car elle fait un appel ADB par package.
     * 
     * @param serial Le numéro de série de l'appareil
     * @return Liste des packages avec leurs noms d'applications
     */
    fun listPackagesWithNames(serial: String): List<PackageInfo> {
        val packages = listPackages(serial)
        return packages.map { packageName ->
            PackageInfo(
                packageName = packageName,
                appName = getApplicationName(serial, packageName)
            )
        }
    }
}


