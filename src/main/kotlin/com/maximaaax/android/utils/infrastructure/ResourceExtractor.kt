package com.maximaaax.android.utils.infrastructure

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object ResourceExtractor {
    private const val BIN_DIR_IN_RESOURCES = "/bin/darwin-arm64"

    fun ensureBinaryAvailable(binaryName: String): File {
        val targetDir = File(getAppCacheDir(), "bin")
        if (!targetDir.exists()) targetDir.mkdirs()
        val targetFile = File(targetDir, binaryName)

        if (!targetFile.exists()) {
            extract(binaryName, targetFile)
        }
        makeExecutable(targetFile)
        
        // Si c'est scrcpy, s'assurer que scrcpy-server est aussi disponible
        if (binaryName == "scrcpy") {
            ensureBinaryAvailable("scrcpy-server")
        }
        
        return targetFile
    }

    private fun extract(binaryName: String, targetFile: File) {
        val resourcePath = "$BIN_DIR_IN_RESOURCES/$binaryName"
        val inputStream = this::class.java.getResourceAsStream(resourcePath)
            ?: throw IOException("Ressource binaire introuvable: $resourcePath")
        targetFile.outputStream().use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }
    }

    private fun makeExecutable(file: File) {
        try {
            file.setExecutable(true)
        } catch (e: SecurityException) {
            // fallback using chmod
            try {
                val path = file.toPath()
                Files.setPosixFilePermissions(
                    path,
                    setOf(
                        java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE,
                        java.nio.file.attribute.PosixFilePermission.OWNER_READ,
                        java.nio.file.attribute.PosixFilePermission.OWNER_WRITE,
                        java.nio.file.attribute.PosixFilePermission.GROUP_EXECUTE,
                        java.nio.file.attribute.PosixFilePermission.GROUP_READ,
                        java.nio.file.attribute.PosixFilePermission.OTHERS_EXECUTE,
                        java.nio.file.attribute.PosixFilePermission.OTHERS_READ,
                    )
                )
            } catch (_: Exception) {
                // ignore
            }
        }
    }

    private fun getAppCacheDir(): File {
        val home = System.getProperty("user.home")
        val dir = File(home, ".androidutils")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}

