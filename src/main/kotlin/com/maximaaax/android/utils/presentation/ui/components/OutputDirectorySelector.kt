package com.maximaaax.android.utils.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximaaax.android.utils.presentation.ui.theme.*
import java.io.File

@Composable
fun OutputDirectorySelector(
    outputDir: File?,
    onChooseDirectory: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Build,
                    contentDescription = null,
                    tint = MacAccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Dossier de sortie",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MacTextPrimary
                )
            }
            OutlinedButton(
                onClick = onChooseDirectory,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    backgroundColor = MacDarkBackground,
                    contentColor = MacTextPrimary
                ),
                border = BorderStroke(0.5.dp, MacBorderSubtle.copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MacTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    outputDir?.absolutePath ?: "Choisir un dossier…",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (outputDir != null) MacTextPrimary else MacTextSecondary
                )
            }
        }
    }
}

