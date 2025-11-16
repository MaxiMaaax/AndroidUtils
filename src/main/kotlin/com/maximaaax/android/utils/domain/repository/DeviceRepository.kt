package com.maximaaax.android.utils.domain.repository

import com.maximaaax.android.utils.domain.model.AdbDevice

interface DeviceRepository {
    suspend fun getDevices(): Result<List<AdbDevice>>
}

