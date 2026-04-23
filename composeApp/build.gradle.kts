import org.gradle.api.tasks.Sync
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.File
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

val desktopNativeResources = layout.projectDirectory.dir("desktop-native-resources")

// Chemins en String pour éviter les ambiguïtés d’extensions `.get()` du DSL Gradle sur les Provider.
val bundledResourcesRootPath: String = File(project.projectDir, "desktop-native-resources").absolutePath
val composeBuildDirPath: String = File(project.projectDir, "build").absolutePath

val prepareBundledTools = tasks.register("prepareBundledTools") {
    notCompatibleWithConfigurationCache("Tâche ad hoc : préparation des binaires adb/scrcpy (hôte Gradle).")
    outputs.dir(File(bundledResourcesRootPath))

    doLast {
        val outRoot = File(bundledResourcesRootPath)
        outRoot.mkdirs()
        val osName = System.getProperty("os.name").lowercase()
        val osArch = System.getProperty("os.arch").lowercase()
        if (!osName.contains("mac")) {
            println(
                "[AndroidUtils] Téléchargement adb/scrcpy embarqués : ignoré (hôte non macOS : $osName). " +
                    "Les builds macOS doivent être faits sur macOS pour ce POC.",
            )
            return@doLast
        }

        val (scrcpyArch, composeTargetSuffix) = when (osArch) {
            "aarch64" -> "aarch64" to "arm64"
            "x86_64", "amd64" -> "x86_64" to "x64"
            else -> throw GradleException("Architecture macOS non supportée pour les outils embarqués : $osArch")
        }

        val targetDir = File(outRoot, "macos-$composeTargetSuffix").apply {
            mkdirs()
        }

        val ptZip = File(composeBuildDirPath, "tmp/androidutils-platform-tools-darwin.zip")
        ptZip.parentFile.mkdirs()
        println("[AndroidUtils] Téléchargement platform-tools (adb)…")
        URI("https://dl.google.com/android/repository/platform-tools-latest-darwin.zip").toURL().openStream()
            .use { input ->
                ptZip.outputStream().use { out ->
                    input.copyTo(out)
                }
            }

        ZipFile(ptZip).use { zip ->
            val entry = zip.getEntry("platform-tools/adb")
                ?: error("Entrée platform-tools/adb absente de l’archive Google.")
            val adbOut = File(targetDir, "adb")
            zip.getInputStream(entry).use { ins ->
                adbOut.outputStream().use { out ->
                    ins.copyTo(out)
                }
            }
        }
        check(File(targetDir, "adb").setExecutable(true, false)) {
            "Impossible de rendre adb exécutable."
        }

        val scrcpyVersion = "3.3.4"
        val scrcpyTarName = "scrcpy-macos-${scrcpyArch}-v$scrcpyVersion.tar.gz"
        val tarball = File(composeBuildDirPath, "tmp/androidutils-$scrcpyTarName")
        println("[AndroidUtils] Téléchargement $scrcpyTarName…")
        URI("https://github.com/Genymobile/scrcpy/releases/download/v$scrcpyVersion/$scrcpyTarName").toURL()
            .openStream()
            .use { input ->
                tarball.outputStream().use { out ->
                    input.copyTo(out)
                }
            }

        val scrcpyBundle = File(targetDir, "scrcpy-bundle")
        if (scrcpyBundle.exists()) {
            scrcpyBundle.deleteRecursively()
        }
        scrcpyBundle.mkdirs()
        val tar = ProcessBuilder(
            "tar",
            "-xzf",
            tarball.absolutePath,
            "-C",
            scrcpyBundle.absolutePath,
            "--strip-components=1",
        ).redirectErrorStream(true)
            .start()
        check(tar.waitFor(300, TimeUnit.SECONDS)) {
            "Extraction scrcpy : délai dépassé."
        }
        check(tar.exitValue() == 0) {
            "tar a échoué (code ${tar.exitValue()})."
        }
        for (name in listOf("scrcpy", "scrcpy_bin")) {
            val bin = File(scrcpyBundle, name)
            if (bin.isFile) {
                check(bin.setExecutable(true, false)) {
                    "Impossible de rendre exécutable : ${bin.name}"
                }
            }
        }
        println("[AndroidUtils] Outils embarqués prêts dans ${targetDir.absolutePath}")
    }
}

compose.desktop {
    application {
        mainClass = "com.maximaaax.androidutils.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.maximaaax.androidutils"
            packageVersion = "1.0.0"
            appResourcesRootDir.set(desktopNativeResources)
        }
    }
}

afterEvaluate {
    tasks.named<Sync>("prepareAppResources") {
        dependsOn(prepareBundledTools)
    }
}
