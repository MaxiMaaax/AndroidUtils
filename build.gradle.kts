plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.compose") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
}

import java.io.File as JavaFile

group = "com.maximaaax.android.utils"

val appVersion = "1.0.0"
version = appVersion

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.9.0")
    implementation("io.insert-koin:koin-core:3.5.0")
    implementation("io.insert-koin:koin-compose:1.1.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}

compose.desktop {
    application {
        mainClass = "com.maximaaax.android.utils.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg)
            packageName = "AndroidUtils"
            packageVersion = appVersion
            macOS {
                bundleID = "com.maximaaax.android.utils"
                iconFile.set(project.file("src/main/resources/app/icon.icns").takeIf { it.exists() })
                dmgPackageVersion = appVersion
            }
        }
    }
}

// Tâche personnalisée pour ajouter une image de fond au DMG
tasks.register("customizeDmg") {
    dependsOn("packageDmg")
    group = "compose desktop"
    description = "Personnalise le DMG avec une image de fond si disponible"
    doLast {
        val dmgDir = file("build/compose/binaries/main/dmg")
        val dmgFiles = dmgDir.listFiles { _, name -> name.endsWith(".dmg") }
        
        if (dmgFiles != null && dmgFiles.isNotEmpty()) {
            val dmgFile = dmgFiles.first()
            val backgroundImage = file("src/main/resources/app/dmg_background.png")
            
            if (backgroundImage.exists()) {
                println("Personnalisation du DMG avec l'image de fond...")
                
                // Créer un DMG temporaire modifiable
                val tempDmg = JavaFile(dmgDir, "${dmgFile.nameWithoutExtension}_temp.dmg")
                val tempDir = JavaFile(dmgDir, "temp_content")
                tempDir.mkdirs()
                
                // Convertir le DMG en format modifiable
                ProcessBuilder("hdiutil", "convert", dmgFile.absolutePath, 
                    "-format", "UDZO", "-o", tempDmg.absolutePath)
                    .start().waitFor()
                
                // Monter le DMG temporaire en lecture/écriture
                val mountPoint = file("${System.getProperty("java.io.tmpdir")}/dmg_mount")
                mountPoint.mkdirs()
                
                val mountProcess = ProcessBuilder("hdiutil", "attach", tempDmg.absolutePath, 
                    "-mountpoint", mountPoint.absolutePath, "-quiet", "-nobrowse")
                    .start()
                mountProcess.waitFor()
                
                if (mountProcess.exitValue() == 0) {
                    try {
                        // Créer le dossier .background
                        val backgroundDir = JavaFile(mountPoint, ".background")
                        backgroundDir.mkdirs()
                        
                        // Copier l'image de fond
                        val targetImage = JavaFile(backgroundDir, "background.png")
                        backgroundImage.copyTo(targetImage, overwrite = true)
                        
                        // Configurer la vue Finder avec AppleScript
                        val appName = dmgFile.nameWithoutExtension.replace("-", " ")
                        val script = """
                            tell application "Finder"
                                tell disk "${appName}"
                                    open
                                    set current view of container window to icon view
                                    set toolbar visible of container window to false
                                    set statusbar visible of container window to false
                                    set bounds of container window to {400, 100, 920, 420}
                                    set viewOptions to the icon view options of container window
                                    set background picture of viewOptions to file ".background:background.png"
                                    set arrangement of viewOptions to not arranged
                                    set icon size of viewOptions to 72
                                    update without registering applications
                                    close
                                end tell
                            end tell
                        """.trimIndent()
                        
                        // Attendre que Finder soit prêt
                        Thread.sleep(1000)
                        ProcessBuilder("osascript", "-e", script).start().waitFor()
                        
                        // Démontage pour sauvegarder les changements
                        ProcessBuilder("hdiutil", "detach", mountPoint.absolutePath, "-quiet", "-force")
                            .start().waitFor()
                        
                        // Convertir en DMG final (read-only)
                        val finalDmg = JavaFile(dmgDir, "${dmgFile.nameWithoutExtension}_final.dmg")
                        ProcessBuilder("hdiutil", "convert", tempDmg.absolutePath,
                            "-format", "UDZO", "-o", finalDmg.absolutePath, "-imagekey", "zlib-level=9")
                            .start().waitFor()
                        
                        // Remplacer l'ancien DMG
                        dmgFile.delete()
                        finalDmg.renameTo(dmgFile)
                        tempDmg.delete()
                        
                        println("✓ DMG personnalisé avec succès!")
                        
                    } catch (e: Exception) {
                        println("Erreur lors de la personnalisation: ${e.message}")
                        // Nettoyer
                        ProcessBuilder("hdiutil", "detach", mountPoint.absolutePath, "-quiet", "-force")
                            .start().waitFor()
                    }
                }
            } else {
                println("Image de fond non trouvée: ${backgroundImage.absolutePath}")
                println("Créez une image PNG 600x400 pixels et placez-la dans src/main/resources/app/dmg_background.png")
            }
        }
    }
}