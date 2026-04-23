package com.maximaaax.androidutils.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    borderCorner: Dp = 20.dp,
    padding: Dp = 0.dp,
    tonalTintTop: Color = olaniTokens().glassFillTop,
    tonalTintBottom: Color = olaniTokens().glassFillBottom,
    borderTop: Color = olaniTokens().glassBorderTop,
    borderBottom: Color = olaniTokens().glassBorderBottom,
    highlight: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    glassLayer(
        modifier = modifier,
        shape = shape,
        borderCorner = borderCorner,
        padding = padding,
        tonalTintTop = tonalTintTop,
        tonalTintBottom = tonalTintBottom,
        borderTop = borderTop,
        borderBottom = borderBottom,
        highlight = highlight,
        preInteraction = Modifier,
        content = content,
    )
}

@Composable
fun InteractiveGlassSurface(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    borderCorner: Dp = 20.dp,
    padding: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val clickIs = remember { MutableInteractionSource() }
    val tokens = olaniTokens()
    val top = if (hovered) tokens.glassHover else tokens.glassFillTop
    glassLayer(
        modifier = modifier,
        shape = shape,
        borderCorner = borderCorner,
        padding = padding,
        tonalTintTop = top,
        tonalTintBottom = tokens.glassFillBottom,
        borderTop = tokens.glassBorderTop,
        borderBottom = tokens.glassBorderBottom,
        preInteraction = Modifier
            .hoverable(interactionSource = interaction, enabled = enabled)
            .clickable(
                interactionSource = clickIs,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            ),
        highlight = true,
        content = content,
    )
}

@Composable
private fun glassLayer(
    modifier: Modifier,
    shape: RoundedCornerShape,
    borderCorner: Dp,
    padding: Dp,
    tonalTintTop: Color,
    tonalTintBottom: Color,
    borderTop: Color,
    borderBottom: Color,
    highlight: Boolean,
    preInteraction: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val cornerPx = with(density) { borderCorner.toPx() }
    val rr = CornerRadius(cornerPx, cornerPx)

    BoxWithConstraints(
        modifier = preInteraction
            .then(modifier)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(tonalTintTop, tonalTintBottom)))
            .drawBehind {
                val w = size.width
                val h = size.height
                if (w <= 0f || h <= 0f) return@drawBehind
                drawRoundRect(
                    brush = Brush.verticalGradient(listOf(borderTop, borderBottom)),
                    topLeft = Offset.Zero,
                    size = Size(w, h),
                    cornerRadius = rr,
                    style = Stroke(width = 1.dp.toPx()),
                )
            },
    ) {
        if (highlight) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .height(maxHeight * 0.12f)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0x14FFFFFF),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
        Box(Modifier.fillMaxSize().padding(padding), content = content)
    }
}
