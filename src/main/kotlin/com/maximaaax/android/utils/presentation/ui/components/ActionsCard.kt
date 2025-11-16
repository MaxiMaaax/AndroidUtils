package com.maximaaax.android.utils.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximaaax.android.utils.presentation.ui.theme.*

@Composable
fun ActionsCard(
    selectedDevice: com.maximaaax.android.utils.domain.model.AdbDevice?,
    isLoading: Boolean,
    onLaunchScrcpy: () -> Unit,
    onLoadPackages: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Actions",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = MacTextPrimary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onLaunchScrcpy,
                    enabled = selectedDevice != null,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MacAccentGreen,
                        contentColor = Color.White,
                        disabledBackgroundColor = MacDarkSurfaceElevated.copy(alpha = 0.3f),
                        disabledContentColor = MacTextSecondary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Lancer scrcpy", fontSize = 14.sp)
                }
                
                Button(
                    onClick = onLoadPackages,
                    enabled = selectedDevice != null && !isLoading,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MacAccentBlue,
                        contentColor = Color.White,
                        disabledBackgroundColor = MacDarkSurfaceElevated.copy(alpha = 0.3f),
                        disabledContentColor = MacTextSecondary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Lister les APK", fontSize = 14.sp)
                }
            }
        }
    }
}

