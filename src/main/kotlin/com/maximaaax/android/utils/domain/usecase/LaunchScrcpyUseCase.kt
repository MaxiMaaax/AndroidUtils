package com.maximaaax.android.utils.domain.usecase

import com.maximaaax.android.utils.data.datasource.ScrcpyDataSource

class LaunchScrcpyUseCase(
    private val scrcpyDataSource: ScrcpyDataSource
) {
    suspend operator fun invoke(deviceSerial: String): Result<Unit> {
        return scrcpyDataSource.launch(deviceSerial)
    }
}

