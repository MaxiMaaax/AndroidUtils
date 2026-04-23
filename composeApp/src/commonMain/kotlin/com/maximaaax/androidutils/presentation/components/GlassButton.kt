package com.maximaaax.androidutils.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maximaaax.androidutils.presentation.theme.GlassSurface
import com.maximaaax.androidutils.presentation.theme.AndroidUtilsPalette
import com.maximaaax.androidutils.presentation.theme.olaniTokens

enum class GlassButtonVariant {
    Primary,
    Secondary,
    Ghost,
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: GlassButtonVariant = GlassButtonVariant.Secondary,
    leading: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val clickIs = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val tokens = olaniTokens()
    val shape = RoundedCornerShape(12.dp)
    val corner = 12.dp
    when (variant) {
        GlassButtonVariant.Primary -> {
            val v = AndroidUtilsPalette.GlowViolet
            val i = AndroidUtilsPalette.GlowIndigo
            val p = AndroidUtilsPalette.GlowPink
            val brush = if (hovered) {
                Brush.linearGradient(listOf(v, i, p))
            } else {
                Brush.linearGradient(listOf(v, i))
            }
            val border = Color(0x66FFFFFF)
            Box(
                modifier = modifier
                    .heightIn(min = 40.dp)
                    .clip(shape)
                    .hoverable(interactionSource = interaction, enabled = enabled)
                    .clickable(
                        interactionSource = clickIs,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick,
                    )
                    .drawBehind {
                        val w = size.width
                        val h = size.height
                        if (w <= 0f || h <= 0f) return@drawBehind
                        val cp = 12.dp.toPx()
                        drawRoundRect(
                            brush = brush,
                            size = this.size,
                            topLeft = Offset.Zero,
                            cornerRadius = CornerRadius(cp, cp),
                        )
                        drawRoundRect(
                            color = border,
                            style = Stroke(1.dp.toPx()),
                            size = this.size,
                            topLeft = Offset.Zero,
                            cornerRadius = CornerRadius(cp, cp),
                        )
                    }
                    .padding(horizontal = 18.dp, vertical = 11.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (leading != null) {
                        leading()
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (trailing != null) {
                        trailing()
                    }
                }
            }
        }
        GlassButtonVariant.Secondary -> {
            Box(
                modifier = modifier
                    .heightIn(min = 40.dp)
                    .hoverable(interactionSource = interaction, enabled = enabled)
                    .clickable(
                        interactionSource = clickIs,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick,
                    ),
            ) {
                GlassSurface(
                    shape = shape,
                    borderCorner = corner,
                    padding = 0.dp,
                    modifier = Modifier,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (leading != null) {
                            leading()
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelLarge,
                            color = AndroidUtilsPalette.OnBackground,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (trailing != null) {
                            trailing()
                        }
                    }
                }
            }
        }
        GlassButtonVariant.Ghost -> {
            val bgTop = if (hovered) tokens.glassHover else Color.Transparent
            val bgBottom = if (hovered) tokens.glassFillBottom else Color.Transparent
            Box(
                modifier = modifier
                    .heightIn(min = 40.dp)
                    .clip(shape)
                    .hoverable(interactionSource = interaction, enabled = enabled)
                    .clickable(
                        interactionSource = clickIs,
                        indication = null,
                        enabled = enabled,
                        onClick = onClick,
                    )
                    .drawBehind {
                        if (hovered) {
                            val w = size.width
                            val h = size.height
                            val cp = 12.dp.toPx()
                            drawRoundRect(
                                brush = Brush.verticalGradient(listOf(bgTop, bgBottom)),
                                cornerRadius = CornerRadius(cp, cp),
                            )
                        }
                    }
                    .padding(horizontal = 18.dp, vertical = 11.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (leading != null) {
                        leading()
                    }
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        color = tokens.textMuted,
                        fontWeight = FontWeight.Medium,
                    )
                    if (trailing != null) {
                        trailing()
                    }
                }
            }
        }
    }
}
