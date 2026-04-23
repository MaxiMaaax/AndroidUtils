package com.maximaaax.androidutils.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maximaaax.androidutils.presentation.theme.AndroidUtilsPalette
import com.maximaaax.androidutils.presentation.theme.olaniTokens

@Composable
fun NoticeBanner(
    message: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    val accent = if (isError) olaniTokens().glowPink else AndroidUtilsPalette.GlowViolet
    val fill = accent.copy(alpha = 0.08f)
    val bord = accent.copy(alpha = 0.45f)
    val dot = if (isError) AndroidUtilsPalette.Danger else AndroidUtilsPalette.GlowCyan
    Row(
        modifier = modifier
            .background(
                color = fill,
                shape = RoundedCornerShape(10.dp),
            )
            .border(1.dp, bord, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = dot, shape = CircleShape),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (isError) AndroidUtilsPalette.Danger else AndroidUtilsPalette.OnBackground,
        )
    }
}

@Composable
fun olaniTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AndroidUtilsPalette.OnBackground,
    unfocusedTextColor = AndroidUtilsPalette.OnBackground,
    disabledTextColor = olaniTokens().textDim,
    errorTextColor = AndroidUtilsPalette.Danger,
    focusedLabelColor = olaniTokens().textDim,
    unfocusedLabelColor = olaniTokens().textDim,
    errorLabelColor = AndroidUtilsPalette.Danger,
    focusedBorderColor = AndroidUtilsPalette.GlowViolet,
    unfocusedBorderColor = Color(0x2EFFFFFF),
    errorBorderColor = AndroidUtilsPalette.Danger,
    cursorColor = AndroidUtilsPalette.Accent,
    errorCursorColor = AndroidUtilsPalette.Danger,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color(0x14FFFFFF),
)

@Composable
fun StatusPill(
    label: String,
    color: Color = AndroidUtilsPalette.GlowCyan,
    modifier: Modifier = Modifier,
) {
    val background = color.copy(alpha = 0.12f)
    val borderC = color.copy(alpha = 0.35f)
    Row(
        modifier = modifier
            .background(background, RoundedCornerShape(50.dp))
            .border(1.dp, borderC, RoundedCornerShape(50.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier
                .size(6.dp)
                .background(color, CircleShape),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

@Composable
fun SelectionDot(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val clickIs = remember { MutableInteractionSource() }
    val borderC = Color(0x40FFFFFF)
    val fill = if (selected) {
        Brush.linearGradient(
            listOf(AndroidUtilsPalette.GlowViolet, AndroidUtilsPalette.GlowPink),
        )
    } else {
        Brush.linearGradient(
            listOf(Color.Transparent, Color.Transparent),
        )
    }
    Box(
        modifier = modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(fill)
            .border(1.dp, borderC, CircleShape)
            .clickable(
                interactionSource = clickIs,
                indication = null,
                onClick = onClick,
            )
            .then(
                if (selected) {
                    Modifier
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Box(
                Modifier
                    .size(5.dp)
                    .background(Color.White, CircleShape),
            )
        }
    }
}
