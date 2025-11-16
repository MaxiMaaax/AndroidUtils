package com.maximaaax.android.utils.presentation.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Phone
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximaaax.android.utils.domain.model.AdbDevice
import com.maximaaax.android.utils.presentation.ui.theme.*

@Composable
fun DeviceSelector(
    devices: List<AdbDevice>,
    selectedDevice: AdbDevice?,
    onDeviceSelected: (AdbDevice) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    ModernCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Phone,
                    contentDescription = null,
                    tint = MacAccentGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Appareil connecté",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MacTextPrimary
                )
            }
            
            OutlinedButton(
                onClick = { expanded = true },
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
                Text(
                    selectedDevice?.let {
                        "${it.model ?: it.deviceName ?: "Device"} (${it.serial})"
                    } ?: "Aucun appareil sélectionné",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (selectedDevice != null) MacTextPrimary else MacTextSecondary
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MacTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                devices.forEach { device ->
                    DropdownMenuItem(onClick = {
                        onDeviceSelected(device)
                        expanded = false
                    }) {
                        Column {
                            Text(
                                device.model ?: device.deviceName ?: "Device",
                                fontWeight = FontWeight.Medium,
                                color = MacTextPrimary
                            )
                            Text(
                                device.serial,
                                fontSize = 12.sp,
                                color = MacTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

