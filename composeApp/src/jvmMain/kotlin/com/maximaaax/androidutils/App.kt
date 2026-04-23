package com.maximaaax.androidutils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.awt.ComposeWindow
import com.maximaaax.androidutils.adb.AdbDevice
import com.maximaaax.androidutils.adb.AdbService
import com.maximaaax.androidutils.adb.ScrcpyLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(hostWindow: ComposeWindow) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val adb = remember { AdbService() }

    var devices by remember { mutableStateOf<List<AdbDevice>>(emptyList()) }
    var selectedSerial by remember { mutableStateOf<String?>(null) }
    var deviceMenuExpanded by remember { mutableStateOf(false) }

    var packages by remember { mutableStateOf<List<String>>(emptyList()) }
    var packageFilter by remember { mutableStateOf("") }
    var loadingDevices by remember { mutableStateOf(false) }
    var loadingPackages by remember { mutableStateOf(false) }
    var pullingPackage by remember { mutableStateOf<String?>(null) }

    fun showError(msg: String) {
        scope.launch { snackbar.showSnackbar(msg) }
    }

    fun refreshDevices() {
        scope.launch {
            loadingDevices = true
            val list = withContext(Dispatchers.IO) { adb.listDevices().getOrElse { e ->
                showError("Appareils : ${e.message}")
                emptyList()
            } }
            devices = list
            val usable = list.filter { it.isUsable }
            if (selectedSerial != null && usable.none { it.serial == selectedSerial }) {
                selectedSerial = null
            }
            if (selectedSerial == null) {
                selectedSerial = usable.firstOrNull()?.serial
            }
            loadingDevices = false
        }
    }

    fun refreshPackages() {
        val serial = selectedSerial
        if (serial == null) {
            showError("Sélectionnez un appareil connecté (état « device »).")
            return
        }
        scope.launch {
            loadingPackages = true
            packages = withContext(Dispatchers.IO) {
                adb.listPackages(serial).getOrElse { e ->
                    showError("Packages : ${e.message}")
                    emptyList()
                }
            }
            loadingPackages = false
        }
    }

    LaunchedEffect(Unit) {
        refreshDevices()
    }

    MaterialTheme {
        Scaffold(
            modifier = Modifier.safeContentPadding(),
            topBar = {
                TopAppBar(
                    title = { Text("AndroidUtils") },
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Outils adb et scrcpy embarqués (macOS). Branchez l’appareil avec le débogage USB activé.",
                    style = MaterialTheme.typography.bodyMedium,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(onClick = { refreshDevices() }, enabled = !loadingDevices) {
                        Text("Actualiser les appareils")
                    }
                    if (loadingDevices) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp), strokeWidth = 2.dp)
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = deviceMenuExpanded,
                    onExpandedChange = { deviceMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true),
                        readOnly = true,
                        value = devices.firstOrNull { it.serial == selectedSerial }?.displayLabel()
                            ?: if (devices.isEmpty()) "Aucun appareil" else "Choisir un appareil…",
                        onValueChange = {},
                        label = { Text("Appareil Android") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = deviceMenuExpanded) },
                    )
                    ExposedDropdownMenu(
                        expanded = deviceMenuExpanded,
                        onDismissRequest = { deviceMenuExpanded = false },
                    ) {
                        devices.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d.displayLabel()) },
                                onClick = {
                                    selectedSerial = d.serial
                                    deviceMenuExpanded = false
                                    packages = emptyList()
                                },
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val serial = selectedSerial
                            if (serial == null || devices.none { it.serial == serial && it.isUsable }) {
                                showError("Choisissez un appareil prêt (état « device »).")
                                return@Button
                            }
                            scope.launch {
                                val r = withContext(Dispatchers.IO) {
                                    ScrcpyLauncher.startMirror(serial)
                                }
                                if (r.isFailure) {
                                    snackbar.showSnackbar("scrcpy : ${r.exceptionOrNull()?.message}")
                                }
                            }
                        },
                    ) {
                        Text("Lancer scrcpy")
                    }
                    Button(
                        onClick = { refreshPackages() },
                        enabled = !loadingPackages && selectedSerial != null &&
                            devices.any { it.serial == selectedSerial && it.isUsable },
                    ) {
                        Text("Lister les applications")
                    }
                    if (loadingPackages) {
                        CircularProgressIndicator(modifier = Modifier.padding(4.dp), strokeWidth = 2.dp)
                    }
                }

                HorizontalDivider()

                OutlinedTextField(
                    value = packageFilter,
                    onValueChange = { packageFilter = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Filtrer par nom de package") },
                    singleLine = true,
                )

                val filtered = remember(packages, packageFilter) {
                    if (packageFilter.isBlank()) packages
                    else packages.filter { it.contains(packageFilter.trim(), ignoreCase = true) }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(filtered, key = { it }) { pkg ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(pkg, modifier = Modifier.weight(1f))
                            Button(
                                onClick = {
                                    val serial = selectedSerial
                                    if (serial == null) return@Button
                                    scope.launch {
                                        pullingPackage = pkg
                                        val destDir = withContext(Dispatchers.IO) {
                                            chooseDirectoryOnEdt(hostWindow)
                                        }
                                        if (destDir == null) {
                                            pullingPackage = null
                                            return@launch
                                        }
                                        val err = withContext(Dispatchers.IO) {
                                            extractApkToDir(adb, serial, pkg, destDir)
                                        }
                                        pullingPackage = null
                                        if (err != null) showError(err)
                                        else snackbar.showSnackbar("APK enregistré dans ${destDir.absolutePath}")
                                    }
                                },
                                enabled = pullingPackage == null,
                            ) {
                                Text(if (pullingPackage == pkg) "Patientez…" else "Extraire APK")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun chooseDirectoryOnEdt(owner: ComposeWindow): File? {
    var result: File? = null
    SwingUtilities.invokeAndWait {
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "Dossier de destination pour l’APK"
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
