package com.maximaaax.android.utils.domain.usecase

import com.maximaaax.android.utils.domain.repository.PackageRepository
import java.io.File

class DownloadApkUseCase(
    private val packageRepository: PackageRepository
) {
    suspend operator fun invoke(
        deviceSerial: String,
        packageName: String,
        outputDir: File
    ): Result<File> {
        return packageRepository.downloadApk(deviceSerial, packageName, outputDir)
    }
}

