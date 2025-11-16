package com.maximaaax.android.utils.di

import com.maximaaax.android.utils.data.datasource.AdbDataSource
import com.maximaaax.android.utils.data.datasource.ScrcpyDataSource
import com.maximaaax.android.utils.data.repository.DeviceRepositoryImpl
import com.maximaaax.android.utils.data.repository.PackageRepositoryImpl
import com.maximaaax.android.utils.domain.repository.DeviceRepository
import com.maximaaax.android.utils.domain.repository.PackageRepository
import com.maximaaax.android.utils.domain.usecase.DownloadApkUseCase
import com.maximaaax.android.utils.domain.usecase.GetDevicesUseCase
import com.maximaaax.android.utils.domain.usecase.GetPackagesUseCase
import com.maximaaax.android.utils.domain.usecase.LaunchScrcpyUseCase
import com.maximaaax.android.utils.presentation.viewmodel.MainViewModel
import org.koin.dsl.module

val appModule = module {
    // Data Sources
    single { AdbDataSource() }
    single { ScrcpyDataSource() }
    
    // Repositories
    single<DeviceRepository> { DeviceRepositoryImpl(get()) }
    single<PackageRepository> { PackageRepositoryImpl(get()) }
    
    // Use Cases
    single { GetDevicesUseCase(get()) }
    single { GetPackagesUseCase(get()) }
    single { DownloadApkUseCase(get()) }
    single { LaunchScrcpyUseCase(get()) }
    
    // ViewModels
    single { MainViewModel(get(), get(), get(), get()) }
}

