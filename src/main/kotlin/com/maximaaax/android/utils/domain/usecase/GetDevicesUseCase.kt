package com.maximaaax.android.utils.domain.usecase

import com.maximaaax.android.utils.domain.model.AdbDevice
import com.maximaaax.android.utils.domain.repository.DeviceRepository

class GetDevicesUseCase(
    private val deviceRepository: DeviceRepository
) {
    suspend operator fun invoke(): Result<List<AdbDevice>> {
        return deviceRepository.getDevices()
    }
}

