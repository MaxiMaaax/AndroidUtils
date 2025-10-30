plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.compose") version "1.7.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
}

group = "com.maximaaax.android.utils"

val appVersion = "0.0.1"
version = appVersion

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
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
            }
        }
    }
}