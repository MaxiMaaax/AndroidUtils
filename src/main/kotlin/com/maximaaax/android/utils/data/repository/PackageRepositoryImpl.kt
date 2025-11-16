package com.maximaaax.android.utils.data.repository

import com.maximaaax.android.utils.data.datasource.AdbDataSource
import com.maximaaax.android.utils.domain.model.PackageInfo
import com.maximaaax.android.utils.domain.repository.PackageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

class PackageRepositoryImpl(
    private val adbDataSource: AdbDataSource
) : PackageRepository {
    override suspend fun getPackages(deviceSerial: String): Result<List<String>> {
        return adbDataSource.listPackages(deviceSerial)
    }

    override suspend fun getPackageInfo(deviceSerial: String, packageName: String): Result<PackageInfo> {
        val appNameResult = adbDataSource.getApplicationName(deviceSerial, packageName)
        return appNameResult.map { appName ->
            PackageInfo(packageName = packageName, appName = appName)
        }
    }

    override suspend fun downloadApk(deviceSerial: String, packageName: String, outputDir: File): Result<File> {
        return withContext(Dispatchers.IO) {
            val apkPathResult = adbDataSource.resolveApkPath(deviceSerial, packageName)
            apkPathResult.fold(
                onSuccess = { apkPath ->
                    if (apkPath != null && outputDir.exists()) {
                        adbDataSource.pull(deviceSerial, apkPath, outputDir)
                    } else {
                        Result.failure(
                            RuntimeException("Impossible de trouver le chemin de l'APK ou le dossier de sortie n'est pas défini")
                        )
                    }
                },
                onFailure = { Result.failure(it) }
            )
        }
    }

    override suspend fun loadPackagesFast(deviceSerial: String): Result<List<PackageInfo>> {
        return withContext(Dispatchers.IO) {
            adbDataSource.listPackages(deviceSerial).map { packageNames ->
                packageNames.map { packageName ->
                    PackageInfo(packageName = packageName, appName = null)
                }
            }
        }
    }

    override suspend fun loadAppNamesProgressively(
        deviceSerial: String,
        initialPackages: List<PackageInfo>,
        onProgress: (List<PackageInfo>) -> Unit
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val updatedPackages = initialPackages.toMutableList()
                val packageIndexMap = initialPackages.mapIndexed { index, pkg -> pkg.packageName to index }.toMap()
                
                val batchSize = 20
                
                initialPackages.chunked(batchSize).forEachIndexed { batchIndex, batch ->
                    coroutineScope {
                        val results = batch.map { packageInfo ->
                            async(Dispatchers.IO) {
                                adbDataSource.getApplicationName(deviceSerial, packageInfo.packageName).getOrNull()
                            }
                        }
                        
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
                    
                    onProgress(updatedPackages.toList())
                    delay(10)
                }
            }
        }
    }
}

