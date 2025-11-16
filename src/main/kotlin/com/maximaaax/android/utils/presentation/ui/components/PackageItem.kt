package com.maximaaax.android.utils.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximaaax.android.utils.domain.model.PackageInfo
import com.maximaaax.android.utils.presentation.ui.theme.*

@Composable
fun PackageItem(
    packageInfo: PackageInfo,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier.fillMaxWidth(),
        backgroundColor = MacDarkSurfaceElevated.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                if (packageInfo.appName != null) {
                    Text(
                        text = packageInfo.appName,
                        fontWeight = FontWeight.SemiBold,
                        color = MacTextPrimary,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = packageInfo.packageName,
                        fontWeight = FontWeight.Normal,
                        color = MacTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = packageInfo.packageName,
                        fontWeight = FontWeight.Medium,
                        color = MacTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = onClick,
                enabled = enabled,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MacAccentBlue,
                    contentColor = Color.White,
                    disabledBackgroundColor = MacDarkSurfaceElevated.copy(alpha = 0.3f),
                    disabledContentColor = MacTextSecondary
                ),
                modifier = Modifier.padding(end = 0.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Télécharger", fontSize = 13.sp)
            }
        }
    }
}

