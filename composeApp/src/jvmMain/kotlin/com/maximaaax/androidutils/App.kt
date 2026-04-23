package com.maximaaax.androidutils

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.awt.ComposeWindow
import com.maximaaax.androidutils.adb.AdbDevice
import com.maximaaax.androidutils.adb.AdbService
import com.maximaaax.androidutils.adb.ScrcpyLauncher
import com.maximaaax.androidutils.presentation.AndroidUtilsApp
import com.maximaaax.androidutils.presentation.model.AndroidUtilsUiState
import com.maximaaax.androidutils.presentation.model.DeviceListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities
import androidx.compose.runtime.Composable

@Composable
fun App(hostWindow: ComposeWindow) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val adb = remember { AdbService() }

    var uiState by remember { mutableStateOf(AndroidUtilsUiState(adbToolsAvailable = true)) }

    fun showError(msg: String) {
        scope.launch { snackbar.showSnackbar(msg) }
    }

    fun mapDevices(list: List<AdbDevice>) = list.map { d ->
        DeviceListItem(
            serial = d.serial,
            displayLabel = d.displayLabel(),
            isUsable = d.isUsable,
        )
    }

    fun refreshDevices() {
        scope.launch {
            uiState = uiState.copy(loadingDevices = true)
            val list = withContext(Dispatchers.IO) { adb.listDevices().getOrElse { e ->
                showError("Appareils : ${e.message}")
                emptyList()
            } }
            val devices = mapDevices(list)
            var sel = uiState.selectedSerial
            if (sel != null && list.filter { it.isUsable }.none { it.serial == sel }) {
                sel = null
            }
            if (sel == null) {
                sel = list.filter { it.isUsable }.firstOrNull()?.serial
            }
            uiState = uiState.copy(
                devices = devices,
                selectedSerial = sel,
                loadingDevices = false,
            )
        }
    }

    fun refreshPackages() {
        val serial = uiState.selectedSerial
        if (serial == null) {
            showError("Sélectionnez un appareil connecté (état « device »).")
            return
        }
        scope.launch {
            uiState = uiState.copy(loadingPackages = true)
            val pk = withContext(Dispatchers.IO) {
                adb.listPackages(serial).getOrElse { e ->
                    showError("Packages : ${e.message}")
                    emptyList()
                }
            }
            uiState = uiState.copy(
                packages = pk,
                loadingPackages = false,
            )
        }
    }

    LaunchedEffect(Unit) {
        refreshDevices()
    }

    AndroidUtilsApp(
        state = uiState,
        onSetSection = { s -> uiState = uiState.copy(currentSection = s) },
        onSetDeviceMenuExpanded = { e -> uiState = uiState.copy(deviceMenuExpanded = e) },
        onSelectDevice = { serial -> uiState = uiState.copy(selectedSerial = serial, deviceMenuExpanded = false, packages = emptyList()) },
        onRefreshDevices = { refreshDevices() },
        onRefreshPackages = { refreshPackages() },
        onPackageFilter = { f -> uiState = uiState.copy(packageFilter = f) },
        onExtract = { pkg ->
            val serial = uiState.selectedSerial
            if (serial == null) {
                showError("Sélectionnez un appareil prêt (état « device »).")
            } else {
                scope.launch {
                    uiState = uiState.copy(pullingPackage = pkg)
                    val destDir = withContext(Dispatchers.IO) {
                        chooseDirectoryOnEdt(hostWindow)
                    }
                    if (destDir == null) {
                        uiState = uiState.copy(pullingPackage = null)
                    } else {
                        val err = withContext(Dispatchers.IO) {
                            extractApkToDir(adb, serial, pkg, destDir)
                        }
                        uiState = uiState.copy(pullingPackage = null)
                        if (err != null) {
                            showError(err)
                        } else {
                            snackbar.showSnackbar("APK enregistré dans ${destDir.absolutePath}")
                        }
                    }
                }
            }
        },
        onStartScrcpy = {
            val serial = uiState.selectedSerial
            if (serial == null || !uiState.devices.any { it.serial == serial && it.isUsable }) {
                showError("Choisissez un appareil prêt (état « device »).")
            } else {
                scope.launch {
                    val r = withContext(Dispatchers.IO) {
                        ScrcpyLauncher.startMirror(serial)
                    }
                    if (r.isFailure) {
                        showError("scrcpy : ${r.exceptionOrNull()?.message}")
                    }
                }
            }
        },
        snackbarHost = snackbar,
    )
}

private fun chooseDirectoryOnEdt(owner: ComposeWindow): File? {
    var result: File? = null
    SwingUtilities.invokeAndWait {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Dossier de destination pour l'APK"
        }
        if (chooser.showOpenDialog(owner) == JFileChooser.APPROVE_OPTION) {
            result = chooser.selectedFile
        }
    }
    return result
}

private fun extractApkToDir(adb: AdbService, serial: String, packageName: String, destDir: File): String? {
    val dumpsys = adb.dumpsysPackage(serial, packageName).getOrElse { return "dumpsys : ${it.message}" }
    val label = AdbService.parseApplicationLabel(dumpsys)
        ?: packageName.substringAfterLast('.')
    val version = AdbService.parseVersionName(dumpsys) ?: "version_inconnue"
    val baseName = "${AdbService.sanitizeFileComponent(label)}_${AdbService.sanitizeFileComponent(version)}.apk"
    val local = File(destDir, baseName)
    val remote = adb.packageApkPath(serial, packageName).getOrElse { return "pm path : ${it.message}" }
    adb.pullApk(serial, remote, local).getOrElse { return "pull : ${it.message}" }
    return null
}
