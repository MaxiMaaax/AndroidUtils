package com.maximaaax.android.utils.presentation.viewmodel

import com.maximaaax.android.utils.domain.model.AdbDevice
import com.maximaaax.android.utils.domain.usecase.DownloadApkUseCase
import com.maximaaax.android.utils.domain.usecase.GetDevicesUseCase
import com.maximaaax.android.utils.domain.usecase.GetPackagesUseCase
import com.maximaaax.android.utils.domain.usecase.LaunchScrcpyUseCase
import com.maximaaax.android.utils.presentation.state.MainUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(
    private val getDevicesUseCase: GetDevicesUseCase,
    private val getPackagesUseCase: GetPackagesUseCase,
    private val downloadApkUseCase: DownloadApkUseCase,
    private val launchScrcpyUseCase: LaunchScrcpyUseCase
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _uiState = MutableStateFlow(
        MainUiState(
            outputDir = File(System.getProperty("user.home"), "Downloads")
                .takeIf { it.exists() && it.isDirectory() }
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        refreshDevices()
    }

    fun refreshDevices() {
        viewModelScope.launch {
            getDevicesUseCase().fold(
                onSuccess = { devices ->
                    _uiState.update { state ->
                        val newSelectedDevice = if (!devices.contains(state.selectedDevice)) {
                            devices.firstOrNull()
                        } else {
                            state.selectedDevice
                        }
                        state.copy(
                            devices = devices,
                            selectedDevice = newSelectedDevice ?: state.selectedDevice
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = "Erreur lors du rafraîchissement des appareils: ${error.message}") }
                }
            )
        }
    }

    fun selectDevice(device: AdbDevice) {
        _uiState.update { it.copy(selectedDevice = device, isLoading = true) }
        loadPackages(device)
    }

    fun loadPackages(device: AdbDevice) {
        viewModelScope.launch(Dispatchers.IO) {
            getPackagesUseCase.loadPackagesFast(device.serial).fold(
                onSuccess = { initialPackages ->
                    viewModelScope.launch(Dispatchers.Main) {
                        _uiState.update { it.copy(packages = initialPackages, isLoading = false, isLoadingNames = true) }
                    }
                    
                    getPackagesUseCase.loadAppNamesProgressively(device.serial, initialPackages) { updatedPackages ->
                        viewModelScope.launch(Dispatchers.Main) {
                            _uiState.update { it.copy(packages = updatedPackages) }
                        }
                    }
                    
                    viewModelScope.launch(Dispatchers.Main) {
                        _uiState.update { it.copy(isLoadingNames = false) }
                    }
                },
                onFailure = { error ->
                    viewModelScope.launch(Dispatchers.Main) {
                        _uiState.update { it.copy(isLoading = false, error = "Erreur lors du chargement des packages: ${error.message}") }
                    }
                }
            )
        }
    }

    fun setPackageFilter(filter: String) {
        _uiState.update { it.copy(packageFilter = filter) }
    }

    fun setOutputDir(dir: File?) {
        _uiState.update { it.copy(outputDir = dir) }
    }

    fun downloadApk(packageName: String) {
        val state = _uiState.value
        val device = state.selectedDevice
        val outputDir = state.outputDir
        
        if (device == null || outputDir == null) {
            _uiState.update { it.copy(error = "Appareil ou dossier de sortie non sélectionné") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            downloadApkUseCase(device.serial, packageName, outputDir).fold(
                onSuccess = {
                    viewModelScope.launch(Dispatchers.Main) {
                        _uiState.update { it.copy(error = null) }
                    }
                },
                onFailure = { error ->
                    viewModelScope.launch(Dispatchers.Main) {
                        _uiState.update { it.copy(error = "Erreur lors du téléchargement de l'APK: ${error.message}") }
                    }
                }
            )
        }
    }

    fun launchScrcpy() {
        val device = _uiState.value.selectedDevice
        if (device == null) {
            _uiState.update { it.copy(error = "Aucun appareil sélectionné") }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            launchScrcpyUseCase(device.serial).fold(
                onSuccess = {
                    viewModelScope.launch(Dispatchers.Main) {
                        _uiState.update { it.copy(error = null) }
                    }
                },
                onFailure = { error ->
                    viewModelScope.launch(Dispatchers.Main) {
                        _uiState.update { it.copy(error = "Erreur lors du lancement de scrcpy: ${error.message}") }
                    }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

