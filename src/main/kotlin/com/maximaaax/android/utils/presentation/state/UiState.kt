package com.maximaaax.android.utils.presentation.state

import com.maximaaax.android.utils.domain.model.AdbDevice
import com.maximaaax.android.utils.domain.model.PackageInfo
import java.io.File

data class MainUiState(
    val devices: List<AdbDevice> = emptyList(),
    val selectedDevice: AdbDevice? = null,
    val packages: List<PackageInfo> = emptyList(),
    val packageFilter: String = "",
    val outputDir: File? = null,
    val isLoading: Boolean = false,
    val isLoadingNames: Boolean = false,
    val error: String? = null
)

