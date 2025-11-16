package com.maximaaax.android.utils.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximaaax.android.utils.presentation.state.MainUiState
import com.maximaaax.android.utils.presentation.ui.components.*
import com.maximaaax.android.utils.presentation.ui.theme.MacTextPrimary
import com.maximaaax.android.utils.presentation.ui.theme.MacTextSecondary
import com.maximaaax.android.utils.presentation.viewmodel.MainViewModel
import org.koin.compose.koinInject
import java.io.File
import javax.swing.JFileChooser

@Composable
fun MainScreen(
    viewModel: MainViewModel = koinInject(),
    onChooseDirectory: (File?) -> File? = { current ->
        chooseDirectory(current)
    }
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .verticalScroll(scrollState)
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
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
                onClick = { viewModel.refreshDevices() },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir", tint = MacTextSecondary)
            }
        }
        
        // Device Selector
        DeviceSelector(
            devices = uiState.devices,
            selectedDevice = uiState.selectedDevice,
            onDeviceSelected = { viewModel.selectDevice(it) },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Output Directory Selector
        OutputDirectorySelector(
            outputDir = uiState.outputDir,
            onChooseDirectory = {
                val newDir = onChooseDirectory(uiState.outputDir)
                viewModel.setOutputDir(newDir)
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Actions Card
        ActionsCard(
            selectedDevice = uiState.selectedDevice,
            isLoading = uiState.isLoading,
            onLaunchScrcpy = { viewModel.launchScrcpy() },
            onLoadPackages = {
                uiState.selectedDevice?.let { viewModel.loadPackages(it) }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Packages List
        if (uiState.selectedDevice != null) {
            PackageList(
                packages = uiState.packages,
                filter = uiState.packageFilter,
                onFilterChange = { viewModel.setPackageFilter(it) },
                isLoadingNames = uiState.isLoadingNames,
                enabled = uiState.outputDir != null,
                onPackageClick = { packageName ->
                    viewModel.downloadApk(packageName)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Error Message
        uiState.error?.let { error ->
            ErrorCard(
                error = error,
                onDismiss = { viewModel.clearError() },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
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

