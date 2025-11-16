package com.maximaaax.android.utils.data.repository

import com.maximaaax.android.utils.data.datasource.AdbDataSource
import com.maximaaax.android.utils.domain.model.AdbDevice
import com.maximaaax.android.utils.domain.repository.DeviceRepository

class DeviceRepositoryImpl(
    private val adbDataSource: AdbDataSource
) : DeviceRepository {
    override suspend fun getDevices(): Result<List<AdbDevice>> {
        return adbDataSource.listDevices()
    }
}

