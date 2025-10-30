package com.maximaaax.android.utils

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.swing.JFileChooser

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Android Utils") {
        MaterialTheme {
            App()
        }
    }
}

@Composable
private fun App() {
    val scope = rememberCoroutineScope()
    var devices by remember { mutableStateOf(emptyList<AdbDevice>()) }
    var selectedDevice by remember { mutableStateOf<AdbDevice?>(null) }
    var packages by remember { mutableStateOf(emptyList<String>()) }
    var packageFilter by remember { mutableStateOf("") }
    var outputDir by remember { mutableStateOf<File?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { refreshDevices(onLoaded = { devices = it; if (selectedDevice == null) selectedDevice = it.firstOrNull() }) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Appareil:")
            var expanded by remember { mutableStateOf(false) }
            TextButton(onClick = { expanded = true }) {
                Text(selectedDevice?.let { it.model ?: it.deviceName ?: it.serial } ?: "Aucun")
            }
            IconButton(onClick = {
                scope.launch { refreshDevices(onLoaded = { devices = it; if (!devices.contains(selectedDevice)) selectedDevice = it.firstOrNull() }) }
            }) { Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir") }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                devices.forEach { d ->
                    DropdownMenuItem(onClick = { selectedDevice = d; expanded = false; scope.launch { loadPackages(d) { packages = it } } }) {
                        Text((d.model ?: d.deviceName ?: d.serial) + " [" + d.serial + "]")
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Dossier de sortie:")
            TextButton(onClick = { outputDir = chooseDirectory(outputDir) }) {
                Text(outputDir?.absolutePath ?: "Choisir…")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = selectedDevice != null, onClick = {
                selectedDevice?.let { ScrcpyLauncher.launch(it.serial) }
            }) { Text("Lancer scrcpy") }

            Button(enabled = selectedDevice != null, onClick = {
                selectedDevice?.let { d -> scope.launch { loadPackages(d) { packages = it } } }
            }) { Text("Lister les APK") }
        }

        if (selectedDevice != null) {
            OutlinedTextField(
                value = packageFilter,
                onValueChange = { packageFilter = it },
                label = { Text("Filtrer packages (texte)") },
                modifier = Modifier.fillMaxWidth()
            )

            val filtered = packages.filter { it.contains(packageFilter, ignoreCase = true) }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                filtered.take(200).forEach { pkg ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(pkg)
                        Button(enabled = outputDir != null, onClick = {
                            scope.launch(Dispatchers.IO) {
                                runCatching {
                                    val serial = selectedDevice!!.serial
                                    val apk = AdbManager.resolveApkPath(serial, pkg)
                                        ?: error("APK introuvable pour $pkg")
                                    AdbManager.pull(serial, apk, outputDir!!)
                                }.onFailure { errorMsg = it.message }
                            }
                        }) { Text("Télécharger APK") }
                    }
                }
            }
        }

        errorMsg?.let { Text("Erreur: $it") }
    }
}

private suspend fun refreshDevices(onLoaded: (List<AdbDevice>) -> Unit) {
    runCatching { AdbManager.listDevices() }
        .onSuccess(onLoaded)
}

private suspend fun loadPackages(device: AdbDevice, onLoaded: (List<String>) -> Unit) {
    runCatching { AdbManager.listPackages(device.serial) }
        .onSuccess(onLoaded)
}

private fun chooseDirectory(current: File?): File? {
    val chooser = JFileChooser(current ?: File(System.getProperty("user.home")))
    chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
    val res = chooser.showOpenDialog(null)
    return if (res == JFileChooser.APPROVE_OPTION) chooser.selectedFile else current
}