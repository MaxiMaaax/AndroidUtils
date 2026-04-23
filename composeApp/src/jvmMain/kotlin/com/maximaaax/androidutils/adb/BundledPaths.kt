package com.maximaaax.androidutils.adb

import java.io.File

/**
 * Résout les chemins des binaires empaquetés.
 *
 * Ordre de résolution :
 * 1. Propriété système **`compose.application.resources.dir`** (Gradle `:composeApp:run`, distribution packagée).
 * 2. **`androidutils.bundled.tools`** ou la variable d’environnement **`ANDROID_UTILS_BUNDLED_TOOLS`** (override manuel).
 * 3. Découverte locale : remonte depuis **`user.dir`** pour trouver **`composeApp/desktop-native-resources/macos-*`**
 *    (lancement depuis Android Studio / `main()` sans tâche Gradle `run`).
 */
object BundledPaths {

    private const val COMPOSE_APP_RESOURCES = "compose.application.resources.dir"
    private const val OVERRIDE_PROP = "androidutils.bundled.tools"
    private const val OVERRIDE_ENV = "ANDROID_UTILS_BUNDLED_TOOLS"

    fun bundledRuntimeRoot(): File {
        fromComposeProperty()?.let { return it }
        fromOverride()?.let { return it }
        discoverDevTree()?.let { return it }
        error(
            "Répertoire des binaires embarqués introuvable.\n" +
                "• Lancez une fois : ./gradlew :composeApp:prepareBundledTools\n" +
                "• Ou lancez via : ./gradlew :composeApp:run\n" +
                "• Ou définissez -D$OVERRIDE_PROP=/chemin/vers/macos-arm64 (dossier contenant « adb » et « scrcpy-bundle »).",
        )
    }

    private fun fromComposeProperty(): File? =
        System.getProperty(COMPOSE_APP_RESOURCES)
            ?.let(::File)
            ?.takeIf { it.isDirectory && File(it, adbFileName()).isFile }

    private fun fromOverride(): File? {
        val path = System.getProperty(OVERRIDE_PROP)
            ?: System.getenv(OVERRIDE_ENV)
            ?: return null
        return File(path).takeIf { it.isDirectory && File(it, adbFileName()).isFile }
    }

    private fun discoverDevTree(): File? {
        val start = File(System.getProperty("user.dir", ".")).absoluteFile
        val leaf = macosResourceLeaf() ?: return null
        var dir: File? = start
        repeat(12) {
            val current = dir ?: return null
            val candidates = listOf(
                File(current, "composeApp/desktop-native-resources/$leaf"),
                File(current, "desktop-native-resources/$leaf"),
            )
            for (c in candidates) {
                if (File(c, adbFileName()).isFile) return c
            }
            dir = current.parentFile
        }
        return null
    }

    /**
     * Sous-dossier `macos-arm64` / `macos-x64` sous `desktop-native-resources`, uniquement pour une détection dev macOS.
     */
    private fun macosResourceLeaf(): String? {
        val os = System.getProperty("os.name") ?: return null
        if (!os.contains("Mac", ignoreCase = true)) return null
        return when (System.getProperty("os.arch")) {
            "aarch64" -> "macos-arm64"
            "x86_64", "amd64" -> "macos-x64"
            else -> "macos-arm64"
        }
    }

    private fun adbFileName(): String =
        if ((System.getProperty("os.name") ?: "").startsWith("Windows", ignoreCase = true)) {
            "adb.exe"
        } else {
            "adb"
        }

    fun adbExecutable(): File {
        val root = bundledRuntimeRoot()
        val name = adbFileName()
        return File(root, name).takeIf { it.isFile }
            ?: error("adb embarqué introuvable : ${File(root, name).absolutePath}")
    }

    fun scrcpyLauncher(): File {
        val root = bundledRuntimeRoot()
        val scrcpy = File(root, "scrcpy-bundle/scrcpy")
        return scrcpy.takeIf { it.isFile }
            ?: error("scrcpy embarqué introuvable : ${scrcpy.absolutePath}")
    }

    fun scrcpyWorkingDirectory(): File = File(bundledRuntimeRoot(), "scrcpy-bundle")
}
