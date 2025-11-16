package com.maximaaax.android.utils.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maximaaax.android.utils.domain.model.PackageInfo
import com.maximaaax.android.utils.presentation.ui.theme.*

@Composable
fun PackageList(
    packages: List<PackageInfo>,
    filter: String,
    onFilterChange: (String) -> Unit,
    isLoadingNames: Boolean,
    enabled: Boolean,
    onPackageClick: (String) -> Unit,
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
                    Icons.Default.Menu,
                    contentDescription = null,
                    tint = MacAccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    "Packages disponibles",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MacTextPrimary
                )
                Spacer(Modifier.weight(1f))
                if (isLoadingNames) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            color = MacAccentBlue,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Chargement des noms...",
                            fontSize = 12.sp,
                            color = MacTextSecondary
                        )
                    }
                } else {
                    Text(
                        text = "${packages.size} packages",
                        fontSize = 13.sp,
                        color = MacTextSecondary
                    )
                }
            }
            
            OutlinedTextField(
                value = filter,
                onValueChange = onFilterChange,
                label = { Text("Filtrer les packages", color = MacTextSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MacTextSecondary)
                },
                trailingIcon = if (filter.isNotEmpty()) {
                    {
                        IconButton(onClick = { onFilterChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = MacTextSecondary)
                        }
                    }
                } else null,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MacTextPrimary,
                    focusedBorderColor = MacAccentBlue.copy(alpha = 0.6f),
                    unfocusedBorderColor = MacBorderSubtle.copy(alpha = 0.2f),
                    backgroundColor = MacDarkBackground,
                    focusedLabelColor = MacTextSecondary,
                    unfocusedLabelColor = MacTextSecondary
                )
            )
            
            val filtered = remember(packages, filter) {
                packages.filter { packageInfo ->
                    packageInfo.packageName.contains(filter, ignoreCase = true) ||
                    (packageInfo.appName?.contains(filter, ignoreCase = true) == true)
                }
            }
            
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(
                    items = filtered.take(500),
                    key = { pkg -> "${pkg.packageName}-${pkg.appName ?: "null"}" }
                ) { packageInfo ->
                    PackageItem(
                        packageInfo = packageInfo,
                        enabled = enabled,
                        onClick = { onPackageClick(packageInfo.packageName) }
                    )
                }
                if (filtered.size > 500) {
                    item {
                        Text(
                            text = "... et ${filtered.size - 500} autres",
                            modifier = Modifier.padding(16.dp),
                            color = MacTextSecondary
                        )
                    }
                }
            }
            
            if (filtered.isEmpty()) {
                Text(
                    text = if (filter.isNotEmpty()) "Aucun package ne correspond au filtre" else "Aucun package disponible",
                    color = MacTextSecondary,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

