package com.maximaaax.android.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser

// Couleurs modernes inspirées de macOS Ventura/Sonoma
private val MacDarkBackground = Color(0xFF1C1C1E) // Fond macOS dark moderne
private val MacDarkSurface = Color(0xFF2C2C2E) // Surface avec subtilité
private val MacDarkSurfaceElevated = Color(0xFF3A3A3C) // Surface élevée
private val MacAccentBlue = Color(0xFF007AFF) // Bleu accent macOS
private val MacAccentGreen = Color(0xFF34C759) // Vert accent macOS (pour Android)
private val MacTextPrimary = Color(0xFFFFFFFF)
private val MacTextSecondary = Color(0xFFAEAEB2)
private val MacBorderSubtle = Color(0x40FFFFFF) // Bordure subtile

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Android Utils",
        resizable = true
    ) {
        MaterialTheme(
            colors = darkColors(
                primary = MacAccentGreen,
                primaryVariant = MacAccentGreen.copy(alpha = 0.8f),
                secondary = MacAccentBlue,
                background = MacDarkBackground,
                surface = MacDarkSurface,
                onSurface = MacTextPrimary,
                onBackground = MacTextPrimary,
                onPrimary = Color.White,
                error = Color(0xFFFF3B30)
            )
        ) {
            App()
        }
    }
}

@Composable
private fun App() {
    val scope = rememberCoroutineScope()
    var devices by remember { mutableStateOf(emptyList<AdbDevice>()) }
    var selectedDevice by remember { mutableStateOf<AdbDevice?>(null) }
    var packages by remember { mutableStateOf(emptyList<PackageInfo>()) }
    var packageFilter by remember { mutableStateOf("") }
    var outputDir by remember { mutableStateOf<File?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var loadingNames by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        refreshDevices(onLoaded = { devices = it; if (selectedDevice == null) selectedDevice = it.firstOrNull() })
    }

    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .verticalScroll(scrollState)
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header moderne
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Android Utils",
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = MacTextPrimary
            )
            IconButton(
                onClick = {
                    scope.launch {
                        refreshDevices(onLoaded = { devices = it; if (!devices.contains(selectedDevice)) selectedDevice = it.firstOrNull() })
                    }
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir", tint = MacTextSecondary)
            }
        }

        // Device Selection Card - Style moderne macOS
        ModernCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = MacAccentGreen, modifier = Modifier.size(20.dp))
                    Text("Appareil connecté", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MacTextPrimary)
                }
                
                var expanded by remember { mutableStateOf(false) }
                OutlinedButton(
                    onClick = { expanded = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = MacDarkBackground,
                        contentColor = MacTextPrimary
                    ),
                    border = BorderStroke(0.5.dp, MacBorderSubtle.copy(alpha = 0.2f))
                ) {
                    Text(
                        selectedDevice?.let { 
                            "${it.model ?: it.deviceName ?: "Device"} (${it.serial})" 
                        } ?: "Aucun appareil sélectionné",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (selectedDevice != null) MacTextPrimary else MacTextSecondary
                    )
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MacTextSecondary, modifier = Modifier.size(18.dp))
                }
                
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    devices.forEach { device ->
                        DropdownMenuItem(onClick = {
                            selectedDevice = device
                            expanded = false
                            scope.launch(Dispatchers.Main) {
                                loading = true
                            }
                            scope.launch(Dispatchers.IO) { 
                                // Charger d'abord rapidement la liste des packages
                                loadPackagesFast(device) { initialPackages ->
                                    // Mettre à jour l'état sur le thread principal
                                    scope.launch(Dispatchers.Main) {
                                        packages = initialPackages
                                        loading = false
                                    }
                                    // Puis charger les noms en arrière-plan dans une nouvelle coroutine
                                    scope.launch(Dispatchers.Main) {
                                        loadingNames = true
                                    }
                                    scope.launch(Dispatchers.IO) {
                                        loadAppNamesProgressively(device, initialPackages) { updatedPackages ->
                                            // Mettre à jour l'état sur le thread principal
                                            scope.launch(Dispatchers.Main) {
                                                packages = updatedPackages
                                            }
                                        }
                                        // Marquer le chargement comme terminé sur le thread principal
                                        scope.launch(Dispatchers.Main) {
                                            loadingNames = false
                                        }
                                    }
                                }
                            }
                        }) {
                            Column {
                                Text(
                                    device.model ?: device.deviceName ?: "Device",
                                    fontWeight = FontWeight.Medium,
                                    color = MacTextPrimary
                                )
                                Text(
                                    device.serial,
                                    fontSize = 12.sp,
                                    color = MacTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Output Directory Card - Style moderne macOS
        ModernCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Build, contentDescription = null, tint = MacAccentBlue, modifier = Modifier.size(20.dp))
                    Text("Dossier de sortie", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MacTextPrimary)
                }
                OutlinedButton(
                    onClick = { outputDir = chooseDirectory(outputDir) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = MacDarkBackground,
                        contentColor = MacTextPrimary
                    ),
                    border = BorderStroke(0.5.dp, MacBorderSubtle.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = MacTextSecondary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        outputDir?.absolutePath ?: "Choisir un dossier…",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (outputDir != null) MacTextPrimary else MacTextSecondary
                    )
                }
            }
        }

        // Actions Card - Style moderne macOS
        ModernCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Actions", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MacTextPrimary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            selectedDevice?.let { device ->
                                scope.launch(Dispatchers.IO) {
                                    runCatching {
                                        ScrcpyLauncher.launch(device.serial)
                                        errorMsg = null
                                    }.onFailure { e ->
                                        errorMsg = "Erreur lors du lancement de scrcpy: ${e.message}"
                                    }
                                }
                            }
                        },
                        enabled = selectedDevice != null,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MacAccentGreen,
                            contentColor = Color.White,
                            disabledBackgroundColor = MacDarkSurfaceElevated.copy(alpha = 0.3f),
                            disabledContentColor = MacTextSecondary
                        )
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Lancer scrcpy", fontSize = 14.sp)
                    }
                    
                    Button(
                        onClick = {
                            selectedDevice?.let { d ->
                                scope.launch(Dispatchers.Main) {
                                    loading = true
                                }
                                scope.launch(Dispatchers.IO) { 
                                    // Charger d'abord rapidement la liste des packages
                                    loadPackagesFast(d) { initialPackages ->
                                        // Mettre à jour l'état sur le thread principal
                                        scope.launch(Dispatchers.Main) {
                                            packages = initialPackages
                                            loading = false
                                        }
                                        // Puis charger les noms en arrière-plan dans une nouvelle coroutine
                                        scope.launch(Dispatchers.Main) {
                                            loadingNames = true
                                        }
                                        scope.launch(Dispatchers.IO) {
                                            loadAppNamesProgressively(d, initialPackages) { updatedPackages ->
                                                // Mettre à jour l'état sur le thread principal
                                                scope.launch(Dispatchers.Main) {
                                                    packages = updatedPackages
                                                }
                                            }
                                            // Marquer le chargement comme terminé sur le thread principal
                                            scope.launch(Dispatchers.Main) {
                                                loadingNames = false
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        enabled = selectedDevice != null && !loading,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = MacAccentBlue,
                            contentColor = Color.White,
                            disabledBackgroundColor = MacDarkSurfaceElevated.copy(alpha = 0.3f),
                            disabledContentColor = MacTextSecondary
                        )
                    ) {
                        if (loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            Icon(Icons.Filled.List, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Lister les APK", fontSize = 14.sp)
                    }
                }
            }
        }

        // Packages List Card - Style moderne macOS
        if (selectedDevice != null) {
            ModernCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        @Suppress("DEPRECATION")
                        Icon(Icons.Filled.List, contentDescription = null, tint = MacAccentBlue, modifier = Modifier.size(20.dp))
                        Text("Packages disponibles", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = MacTextPrimary)
                        Spacer(Modifier.weight(1f))
                        if (loadingNames) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    color = MacAccentBlue,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Chargement des noms...",
                                    fontSize = 12.sp,
                                    color = MacTextSecondary
                                )
                            }
                        } else {
                            Text(
                                text = "${packages.size} packages",
                                fontSize = 13.sp,
                                color = MacTextSecondary
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = packageFilter,
                        onValueChange = { packageFilter = it },
                        label = { Text("Filtrer les packages", color = MacTextSecondary) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MacTextSecondary)
                        },
                        trailingIcon = if (packageFilter.isNotEmpty()) {
                            {
                                IconButton(onClick = { packageFilter = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = MacTextSecondary)
                                }
                            }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            textColor = MacTextPrimary,
                            focusedBorderColor = MacAccentBlue.copy(alpha = 0.6f),
                            unfocusedBorderColor = MacBorderSubtle.copy(alpha = 0.2f),
                            backgroundColor = MacDarkBackground,
                            focusedLabelColor = MacTextSecondary,
                            unfocusedLabelColor = MacTextSecondary
                        )
                    )

                    // Créer une liste mémorisée qui se met à jour quand les packages changent
                    val filtered = remember(packages, packageFilter) {
                        packages.filter { packageInfo ->
                            packageInfo.packageName.contains(packageFilter, ignoreCase = true) ||
                            (packageInfo.appName?.contains(packageFilter, ignoreCase = true) == true)
                        }
                    }
                    
                    // Utiliser LazyColumn au lieu de Column avec verticalScroll pour éviter l'imbrication de scrollables
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(
                            items = filtered.take(500),
                            key = { pkg -> "${pkg.packageName}-${pkg.appName ?: "null"}" } // Clé incluant appName pour détecter les changements
                        ) { packageInfo ->
                            PackageItem(
                                packageInfo = packageInfo,
                                enabled = outputDir != null,
                                onClick = {
                                    selectedDevice?.let { device ->
                                        scope.launch(Dispatchers.IO) {
                                            runCatching {
                                                val apkPath = AdbManager.resolveApkPath(device.serial, packageInfo.packageName)
                                                if (apkPath != null && outputDir != null) {
                                                    AdbManager.pull(device.serial, apkPath, outputDir!!)
                                                    errorMsg = null
                                                } else {
                                                    errorMsg = "Impossible de trouver le chemin de l'APK ou le dossier de sortie n'est pas défini"
                                                }
                                            }.onFailure { e ->
                                                errorMsg = "Erreur lors du téléchargement de l'APK: ${e.message}"
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        if (filtered.size > 500) {
                            item {
                                Text(
                                    text = "... et ${filtered.size - 500} autres",
                                    modifier = Modifier.padding(16.dp),
                                    color = MacTextSecondary
                                )
                            }
                        }
                    }
                    
                    if (filtered.isEmpty()) {
                        Text(
                            text = if (packageFilter.isNotEmpty()) "Aucun package ne correspond au filtre" else "Aucun package disponible",
                            color = MacTextSecondary,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }

        // Error Message - Style moderne
        errorMsg?.let {
            ModernCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colors.error.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                backgroundColor = MaterialTheme.colors.error.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colors.error)
                    Text("Erreur: $it", color = MaterialTheme.colors.error)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { errorMsg = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = MaterialTheme.colors.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MacDarkSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(0.5.dp, MacBorderSubtle.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        backgroundColor = backgroundColor,
        elevation = 0.dp
    ) {
        Column(content = content)
    }
}

@Composable
private fun PackageItem(packageInfo: PackageInfo, enabled: Boolean, onClick: () -> Unit) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MacDarkSurfaceElevated.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Afficher le nom de l'application en premier s'il est disponible
                if (packageInfo.appName != null) {
                    Text(
                        text = packageInfo.appName,
                        fontWeight = FontWeight.SemiBold,
                        color = MacTextPrimary,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = packageInfo.packageName,
                        fontWeight = FontWeight.Normal,
                        color = MacTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    // Si pas de nom d'app, afficher juste le package
                    Text(
                        text = packageInfo.packageName,
                        fontWeight = FontWeight.Medium,
                        color = MacTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MacAccentBlue,
                    contentColor = Color.White,
                    disabledBackgroundColor = MacDarkSurfaceElevated.copy(alpha = 0.3f),
                    disabledContentColor = MacTextSecondary
                ),
                modifier = Modifier.padding(end = 0.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Télécharger", fontSize = 13.sp)
            }
        }
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

private suspend fun loadPackagesFast(device: AdbDevice, onLoaded: (List<PackageInfo>) -> Unit) {
    runCatching { 
        val packageNames = AdbManager.listPackages(device.serial)
        // Créer des PackageInfo avec appName = null pour affichage immédiat
        packageNames.map { PackageInfo(packageName = it, appName = null) }
    }.onSuccess(onLoaded)
}

private suspend fun loadAppNamesProgressively(
    device: AdbDevice, 
    initialPackages: List<PackageInfo>,
    onProgress: (List<PackageInfo>) -> Unit
) {
    // Créer une copie mutable pour mettre à jour progressivement
    val updatedPackages = initialPackages.toMutableList()
    
    // Créer un map pour un accès rapide par packageName
    val packageIndexMap = initialPackages.mapIndexed { index, pkg -> pkg.packageName to index }.toMap()
    
    // Charger les noms en parallèle par lots pour accélérer
    val batchSize = 20 // Augmenter la taille des lots
    
    initialPackages.chunked(batchSize).forEachIndexed { batchIndex, batch ->
        // Traiter chaque lot en parallèle avec async dans un coroutineScope
        coroutineScope {
            val results = batch.map { packageInfo ->
                async(Dispatchers.IO) {
                    runCatching {
                        AdbManager.getApplicationName(device.serial, packageInfo.packageName)
                    }.getOrNull()
                }
            }
            
            // Attendre les résultats du lot
            results.forEachIndexed { index, deferred ->
                val appName = deferred.await()
                val packageInfo = batch[index]
                val globalIndex = packageIndexMap[packageInfo.packageName]
                if (globalIndex != null) {
                    updatedPackages[globalIndex] = PackageInfo(
                        packageName = packageInfo.packageName,
                        appName = appName
                    )
                }
            }
        }
        
        // Mettre à jour l'UI après chaque lot
        onProgress(updatedPackages.toList())
        
        // Petite pause pour ne pas surcharger le système
        delay(10) // Réduire la pause car on traite en parallèle
    }
}

private suspend fun loadPackagesWithNames(device: AdbDevice, onLoaded: (List<PackageInfo>) -> Unit) {
    runCatching { AdbManager.listPackagesWithNames(device.serial) }
        .onSuccess(onLoaded)
}

private fun chooseDirectory(current: File?): File? {
    return try {
        // Utilise le dialogue natif macOS via AppleScript
        val initialPath = (current?.takeIf { it.exists() && it.isDirectory } 
            ?: File(System.getProperty("user.home"))).absolutePath
        
        val script = "tell application \"System Events\"\n" +
                "activate\n" +
                "set folderPath to POSIX path of (choose folder with prompt \"Choisir un dossier de sortie\" default location POSIX file \"$initialPath\")\n" +
                "return folderPath\n" +
                "end tell"
        
        val process = ProcessBuilder("osascript", "-e", script)
            .redirectErrorStream(true)
            .start()
        
        val result = process.inputStream.bufferedReader().use { it.readText().trim() }
        process.waitFor()
        
        if (result.isNotEmpty() && process.exitValue() == 0 && result != "false") {
            File(result)
        } else {
            current
        }
    } catch (e: Exception) {
        // Fallback vers JFileChooser si AppleScript ne fonctionne pas
        val chooser = JFileChooser(current ?: File(System.getProperty("user.home")))
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        val res = chooser.showOpenDialog(null)
        if (res == JFileChooser.APPROVE_OPTION) chooser.selectedFile else current
    }
}