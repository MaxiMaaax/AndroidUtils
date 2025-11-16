package com.maximaaax.android.utils.data.datasource

import com.maximaaax.android.utils.ScrcpyLauncher

class ScrcpyDataSource {
    fun launch(serial: String): Result<Unit> {
        return runCatching {
            ScrcpyLauncher.launch(serial)
        }
    }
}

