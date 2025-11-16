package com.maximaaax.android.utils.domain.repository

import com.maximaaax.android.utils.domain.model.PackageInfo
import java.io.File

interface PackageRepository {
    suspend fun getPackages(deviceSerial: String): Result<List<String>>
    suspend fun getPackageInfo(deviceSerial: String, packageName: String): Result<PackageInfo>
    suspend fun downloadApk(deviceSerial: String, packageName: String, outputDir: File): Result<File>
    suspend fun loadPackagesFast(deviceSerial: String): Result<List<PackageInfo>>
    suspend fun loadAppNamesProgressively(
        deviceSerial: String,
        initialPackages: List<PackageInfo>,
        onProgress: (List<PackageInfo>) -> Unit
    ): Result<Unit>
}

