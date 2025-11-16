package com.maximaaax.android.utils.data.datasource

import com.maximaaax.android.utils.AdbManager
import com.maximaaax.android.utils.domain.model.AdbDevice
import com.maximaaax.android.utils.domain.model.PackageInfo
import java.io.File

class AdbDataSource {
    fun listDevices(): Result<List<AdbDevice>> {
        return runCatching {
            AdbManager.listDevices()
        }
    }

    fun listPackages(serial: String): Result<List<String>> {
        return runCatching {
            AdbManager.listPackages(serial)
        }
    }

    fun resolveApkPath(serial: String, packageName: String): Result<String?> {
        return runCatching {
            AdbManager.resolveApkPath(serial, packageName)
        }
    }

    fun pull(serial: String, remotePath: String, localDir: File): Result<File> {
        return runCatching {
            AdbManager.pull(serial, remotePath, localDir)
        }
    }

    fun getApplicationName(serial: String, packageName: String): Result<String?> {
        return runCatching {
            AdbManager.getApplicationName(serial, packageName)
        }
    }
}

