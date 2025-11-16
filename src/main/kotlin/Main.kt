package com.maximaaax.android.utils

import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.maximaaax.android.utils.di.appModule
import com.maximaaax.android.utils.presentation.ui.MainScreen
import com.maximaaax.android.utils.presentation.ui.theme.*
import org.koin.core.context.startKoin

fun main() {
    // Initialiser Koin
    startKoin {
        modules(appModule)
    }
    
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Android Utils",
            resizable = true
        ) {
            MaterialTheme(
                colors = darkColors(
                    primary = MacAccentGreen,
                    primaryVariant = MacAccentGreen.copy(alpha = 0.8f),
                    secondary = MacAccentBlue,
                    background = MacDarkBackground,
                    surface = MacDarkSurface,
                    onSurface = MacTextPrimary,
                    onBackground = MacTextPrimary,
                    onPrimary = Color.White,
                    error = Color(0xFFFF3B30)
                )
            ) {
                MainScreen()
            }
        }
    }
}
