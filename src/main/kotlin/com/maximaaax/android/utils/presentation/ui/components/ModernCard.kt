package com.maximaaax.android.utils.presentation.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.maximaaax.android.utils.presentation.ui.theme.MacBorderSubtle
import com.maximaaax.android.utils.presentation.ui.theme.MacDarkSurface

@Composable
fun ModernCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MacDarkSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(0.5.dp, MacBorderSubtle.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        backgroundColor = backgroundColor,
        elevation = 0.dp
    ) {
        Column(content = content)
    }
}

