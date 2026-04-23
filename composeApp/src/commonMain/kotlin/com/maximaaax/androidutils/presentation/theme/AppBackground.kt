package com.maximaaax.androidutils.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.max as maxFloat

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val top = AndroidUtilsPalette.BackgroundTop
    val bottom = AndroidUtilsPalette.BackgroundBottom
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(top, bottom),
                ),
            )
            .drawBehind {
                val w = size.width
                val h = size.height
                val maxDim = maxFloat(w, h)
                val r1 = maxDim * 0.55f
                val r2 = maxDim * 0.50f
                val r3 = maxDim * 0.35f
                // Violet
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AndroidUtilsPalette.GlowViolet.copy(alpha = 0.35f),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.12f, h * 0.08f),
                        radius = r1,
                    ),
                    radius = r1,
                    center = Offset(w * 0.12f, h * 0.08f),
                )
                // Cyan
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AndroidUtilsPalette.GlowCyan.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.95f, h * 0.95f),
                        radius = r2,
                    ),
                    radius = r2,
                    center = Offset(w * 0.95f, h * 0.95f),
                )
                // Rose
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AndroidUtilsPalette.GlowPink.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                        center = Offset(w * 0.75f, h * 0.18f),
                        radius = r3,
                    ),
                    radius = r3,
                    center = Offset(w * 0.75f, h * 0.18f),
                )
            },
        content = content,
    )
}
