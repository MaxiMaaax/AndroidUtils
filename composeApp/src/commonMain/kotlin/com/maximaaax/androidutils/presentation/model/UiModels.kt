package com.maximaaax.androidutils.presentation.model

data class DeviceListItem(
    val serial: String,
    val displayLabel: String,
    val isUsable: Boolean,
)

enum class AppSection {
    Home,
    Workshop,
    Credits,
}

data class AndroidUtilsUiState(
    val currentSection: AppSection = AppSection.Home,
    val devices: List<DeviceListItem> = emptyList(),
    val selectedSerial: String? = null,
    val deviceMenuExpanded: Boolean = false,
    val packages: List<String> = emptyList(),
    val packageFilter: String = "",
    val loadingDevices: Boolean = false,
    val loadingPackages: Boolean = false,
    val pullingPackage: String? = null,
    val adbToolsAvailable: Boolean = true,
)
