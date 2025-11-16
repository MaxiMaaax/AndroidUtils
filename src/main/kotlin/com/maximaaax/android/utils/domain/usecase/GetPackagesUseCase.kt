package com.maximaaax.android.utils.domain.usecase

import com.maximaaax.android.utils.domain.model.PackageInfo
import com.maximaaax.android.utils.domain.repository.PackageRepository

class GetPackagesUseCase(
    private val packageRepository: PackageRepository
) {
    suspend fun loadPackagesFast(deviceSerial: String): Result<List<PackageInfo>> {
        return packageRepository.loadPackagesFast(deviceSerial)
    }
    
    suspend fun loadAppNamesProgressively(
        deviceSerial: String,
        initialPackages: List<PackageInfo>,
        onProgress: (List<PackageInfo>) -> Unit
    ): Result<Unit> {
        return packageRepository.loadAppNamesProgressively(
            deviceSerial,
            initialPackages,
            onProgress
        )
    }
}

