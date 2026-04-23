package com.maximaaax.androidutils.presentation.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximaaax.androidutils.presentation.components.HomeIcon
import com.maximaaax.androidutils.presentation.components.InfoIcon
import com.maximaaax.androidutils.presentation.components.VideoIcon
import com.maximaaax.androidutils.presentation.model.AppSection
import com.maximaaax.androidutils.presentation.theme.AppBackground
import com.maximaaax.androidutils.presentation.theme.AndroidUtilsPalette
import com.maximaaax.androidutils.presentation.theme.GlassSurface
import com.maximaaax.androidutils.presentation.theme.eyebrowTextStyle
import com.maximaaax.androidutils.presentation.theme.descriptionTextStyle
import com.maximaaax.androidutils.presentation.theme.olaniTokens

const val appDisplayName = "AndroidUtils"

@Composable
fun AppShell(
    currentSection: AppSection,
    onSelect: (AppSection) -> Unit,
    snackbarHostState: SnackbarHostState? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        AppBackground(Modifier.fillMaxSize()) {
            Row(
                modifier = modifier
                    .fillMaxSize()
                    .padding(
                        end = 28.dp,
                        top = 24.dp,
                        bottom = 24.dp,
                    ),
            ) {
                NavigationSidebar(
                    current = currentSection,
                    onSelect = onSelect,
                )
                Box(Modifier.weight(1f)) {
                    content()
                }
            }
        }
        if (snackbarHostState != null) {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
            )
        }
    }
}

@Composable
fun PageHeader(
    eyebrow: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = eyebrow.uppercase(),
                style = eyebrowTextStyle().copy(
                    color = AndroidUtilsPalette.Accent,
                ),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = AndroidUtilsPalette.OnBackground,
            )
            Text(
                text = description,
                style = descriptionTextStyle(),
            )
        }
        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun GlassSection(
    title: String? = null,
    description: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassSurface(
        modifier = modifier.fillMaxWidth(),
        borderCorner = 20.dp,
        padding = 20.dp,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = AndroidUtilsPalette.OnBackground,
                )
            }
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = olaniTokens().textMuted,
                )
            }
            if (title != null || description != null) {
                Spacer(Modifier.height(2.dp))
            }
            content()
        }
    }
}

@Composable
fun NavigationSidebar(
    current: AppSection,
    onSelect: (AppSection) -> Unit,
) {
    val tokens = olaniTokens()
    Column(
        modifier = Modifier
            .width(248.dp)
            .fillMaxHeight()
            .padding(start = 4.dp, end = 16.dp),
    ) {
        AppBrand()
        Spacer(Modifier.height(24.dp))
        SidebarSectionLabel("NAVIGATION")
        Spacer(Modifier.height(8.dp))
        SidebarItem(
            section = AppSection.Home,
            label = "Accueil",
            current = current,
            onSelect = onSelect,
            leading = { c -> HomeIcon(c) },
        )
        SidebarItem(
            section = AppSection.Workshop,
            label = "Atelier ADB / APK",
            current = current,
            onSelect = onSelect,
            leading = { c -> VideoIcon(c) },
        )
        Spacer(Modifier.weight(1f))
        SidebarSectionLabel("AIDE")
        Spacer(Modifier.height(8.dp))
        SidebarItem(
            section = AppSection.Credits,
            label = "Crédits",
            current = current,
            onSelect = onSelect,
            leading = { c -> InfoIcon(c) },
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "AndroidUtils · v1.0.0",
            style = MaterialTheme.typography.labelSmall,
            color = tokens.textDim,
        )
    }
}

@Composable
fun SidebarSectionLabel(text: String) {
    Text(
        text = text,
        style = eyebrowTextStyle().copy(
            color = olaniTokens().textDim,
        ),
    )
}

@Composable
fun SidebarItem(
    section: AppSection,
    label: String,
    current: AppSection,
    onSelect: (AppSection) -> Unit,
    leading: @Composable (Color) -> Unit,
) {
    val isSelected = current == section
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val bgAlpha = animateFloatAsState(
        targetValue = when {
            isSelected -> 0.14f
            hovered -> 0.08f
            else -> 0f
        },
        label = "sidebarBg",
    )
    val click = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interaction, enabled = true)
            .background(
                color = Color.White.copy(alpha = bgAlpha.value),
                shape = RoundedShape12,
            )
            .clickable(
                interactionSource = click,
                indication = null,
                onClick = { onSelect(section) },
            )
            .padding(vertical = 6.dp, horizontal = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .drawBehind {
                    if (isSelected) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(AndroidUtilsPalette.GlowViolet, AndroidUtilsPalette.GlowPink),
                            ),
                            topLeft = Offset.Zero,
                            size = this.size,
                            cornerRadius = CornerRadius(2f, 2f),
                        )
                    }
                },
        )
        val iconColor = if (isSelected) {
            Color.White
        } else {
            olaniTokens().textMuted
        }
        leading(iconColor)
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) {
                Color.White
            } else {
                olaniTokens().textMuted
            },
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        AnimatedVisibility(visible = hovered) {
            Text("›", color = olaniTokens().textMuted, style = MaterialTheme.typography.titleSmall)
        }
    }
}

private val RoundedShape12 = RoundedCornerShape(12.dp)

@Composable
fun AppBrand() {
    val border = Color(0x54FFFFFF)
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(
                    brush = Brush.linearGradient(
                        listOf(AndroidUtilsPalette.GlowIndigo, AndroidUtilsPalette.GlowViolet, AndroidUtilsPalette.GlowPink),
                    ),
                    shape = RoundedCornerShape(8.dp),
                )
                .drawBehind {
                    val s = this.size
                    if (s.width > 0f) {
                        drawRoundRect(
                            color = border,
                            topLeft = Offset.Zero,
                            size = Size(s.width, s.height),
                            cornerRadius = CornerRadius(8f, 8f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(1f),
                        )
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "A",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = appDisplayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = AndroidUtilsPalette.OnBackground,
            )
            Text(
                text = "Boîte à outils",
                style = MaterialTheme.typography.labelSmall,
                color = olaniTokens().textDim,
            )
        }
    }
}
